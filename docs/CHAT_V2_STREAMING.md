# ChatV2 스트리밍 메소드 순서 및 동작 원리

## 1. 진입점 (Controller)

### `ChatV2Controller.chatStreaming()`

- **경로**: POST `/api/v2/chat/stream` 또는 GET `/api/v2/chat/stream?message=...&sessionId=...`
- **역할**: 요청 수신 후 `ChatV2Service.chatStreaming()`로 위임

```java
@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter chatStreaming(@Valid @RequestBody ChatV2Request request) {
    return chatV2Service.chatStreaming(request);
}
```

---

## 2. 메소드 실행 순서 (전체 흐름)

```
1. ChatV2Controller.chatStreaming(request)
       │
       ▼
2. ChatV2Service.chatStreaming(request)
       │
       ├─► 2-1. 세션 ID 생성/확인 (generateSessionId 또는 request.sessionId)
       ├─► 2-2. System Instruction 로드 (getSystemInstruction)
       ├─► 2-3. SseEmitter 생성 (타임아웃 Long.MAX_VALUE)
       │
       └─► 2-4. CompletableFuture.runAsync() 비동기 실행
                  │
                  ├─► 3. loadOrCreateSession(sessionId, systemInstruction)
                  │        - Redis에서 세션 로드 또는 새 AgentState 생성
                  │
                  ├─► 4. UserMessage 생성 및 initialState에 설정
                  │
                  ├─► 5. emitter.send("start", "채팅을 시작합니다...")
                  │
                  ├─► 6. (새 세션일 때) emitter.send("session", sessionId)
                  │
                  ├─► 7. agentGraph.executeStreaming(initialState, message, emitter)
                  │        │
                  │        ├─► 7-1. emitter.send("step", "입력 처리 중...")
                  │        ├─► 7-2. inputNode.process(state, userInput)
                  │        ├─► 7-3. emitter.send("step", "LLM 응답 생성 중...")
                  │        ├─► 7-4. llmNode.processStreaming(state, emitter)  ← chunk 스트리밍
                  │        ├─► 7-5. emitter.send("step", "다음 단계 결정 중...")
                  │        ├─► 7-6. conditionalNode.route(state)
                  │        └─► 7-7. emitter.send("streaming-complete", "스트리밍 완료")
                  │
                  ├─► 8. validationNode.validate(finalState)
                  ├─► 9. saveToHistory(sessionId, userMessage, aiMessage)
                  ├─► 10. sessionStore.saveSession(sessionId, finalState)
                  ├─► 11. emitter.send("validation", JSON)
                  ├─► 12. emitter.send("complete", "채팅이 완료되었습니다...")
                  │
                  └─► 13. emitter.complete()
```

---

## 3. 세부 동작 원리

### 3-1. ChatV2Service.chatStreaming()

| 단계 | 메소드/처리                      | 설명                                                      |
| ---- | -------------------------------- | --------------------------------------------------------- |
| 1    | `generateSessionId()`            | sessionId가 없으면 `session-{UUID}` 생성                  |
| 2    | `getSystemInstruction()`         | 요청값 또는 `prompts/default-system-instruction.txt` 로드 |
| 3    | `new SseEmitter(Long.MAX_VALUE)` | SSE 스트림 생성, 타임아웃 거의 없음                       |
| 4    | `CompletableFuture.runAsync()`   | 비동기 실행, 요청 스레드는 바로 SseEmitter 반환           |

### 3-2. AgentGraph.executeStreaming()

| 순서 | 호출                                 | SSE 이벤트                  | 설명                                                 |
| ---- | ------------------------------------ | --------------------------- | ---------------------------------------------------- |
| 1    | `emitter.send("step", ...)`          | `event: step`               | 입력 처리 시작                                       |
| 2    | `inputNode.process()`                | -                           | 입력 검증, UserMessage 생성 및 state.messages에 추가 |
| 3    | `emitter.send("step", ...)`          | `event: step`               | LLM 호출 시작                                        |
| 4    | `llmNode.processStreaming()`         | `event: chunk` (반복)       | Gemini 스트리밍 응답을 청크 단위로 전송              |
| 5    | `emitter.send("step", ...)`          | `event: step`               | 다음 단계 결정                                       |
| 6    | `conditionalNode.route()`            | -                           | "response" 또는 "error" 반환                         |
| 7    | `emitter.send("streaming-complete")` | `event: streaming-complete` | 스트리밍 종료 (정상 시)                              |

### 3-3. LlmNode.processStreaming()

- **StreamingChatModel** 사용 시:
  - `streamingChatModel.stream(prompt)`로 `Flux<ChatResponse>` 반환
  - `doOnNext`에서 델타만 추출해 `emitter.send("chunk", delta)`로 즉시 전송
  - `blockLast()`로 스트림 완료까지 대기
- **델타 계산**: 이전 텍스트와 비교해 새로 추가된 부분만 전송 (중복 방지)

---

## 4. SSE 이벤트 발생 순서

| 순서 | event                | data                                              | 발생 위치                        |
| ---- | -------------------- | ------------------------------------------------- | -------------------------------- |
| 1    | `start`              | "채팅을 시작합니다..."                            | ChatV2Service                    |
| 2    | `session`            | sessionId                                         | ChatV2Service (새 세션인 경우만) |
| 3    | `step`               | "입력 처리 중..."                                 | AgentGraph                       |
| 4    | `step`               | "LLM 응답 생성 중..."                             | AgentGraph                       |
| 5    | `chunk`              | 텍스트 델타                                       | LlmNode (여러 번)                |
| 6    | `step`               | "다음 단계 결정 중..."                            | AgentGraph                       |
| 7    | `streaming-complete` | "스트리밍 완료"                                   | AgentGraph                       |
| 8    | `validation`         | JSON (score, passed, feedback, needsRegeneration) | ChatV2Service                    |
| 9    | `complete`           | "채팅이 완료되었습니다. 실행 시간: X초"           | ChatV2Service                    |

**에러 발생 시**: 해당 지점에서 `event: error` 전송 후 종료

---

## 5. 프론트엔드 이벤트 처리 (chat-v2-demo.html)

| event                | 프론트엔드 동작                                          |
| -------------------- | -------------------------------------------------------- |
| `start`              | 상태를 "답변 중..."으로 변경                             |
| `session`            | localStorage에 sessionId 저장, 화면에 표시               |
| `chunk`              | 마크다운 버퍼에 추가, 원문 텍스트 표시, 커서 깜빡임      |
| `streaming-complete` | 마크다운 렌더링, 커서 제거, 상태를 "검수 중..."으로 변경 |
| `validation`         | 검수 배지 표시 (점수, 통과/실패)                         |
| `complete`           | 스트리밍 종료 처리, 버튼 활성화                          |
| `error`              | 에러 메시지 표시, 스트리밍 종료                          |

---

## 6. 비동기 실행 구조 요약

```
[HTTP 요청]
    → Controller.chatStreaming()
    → Service.chatStreaming()
    → SseEmitter 반환 (즉시 응답)

[백그라운드]
    → CompletableFuture.runAsync()
    → 세션 로드 → 그래프 실행 → 검수 → 저장 → SSE 이벤트 전송
```

클라이언트는 SSE 연결을 유지한 채로 이벤트를 순서대로 수신하며, 각 event 타입에 맞게 UI를 업데이트합니다.
