# Phase 1: 요구사항 정의 및 설계

## 1. 에이전트 역할 정의

### 1.1 에이전트 개요

**에이전트 이름**: MSK AI Agent (또는 프로젝트에 맞는 이름)

**에이전트 역할**: 
일반적인 AI 어시스턴트로, 사용자의 질문에 답변하고 필요한 경우 도구를 사용하여 정보를 검색하거나 작업을 수행합니다.

### 1.2 주요 기능

1. **대화형 질의응답**
   - 사용자의 자연어 질문 이해
   - 맥락을 고려한 답변 생성
   - 대화 히스토리 유지

2. **도구 활용**
   - 필요 시 계산기 도구 사용
   - 검색 도구를 통한 정보 검색
   - 기타 외부 API 연동

3. **에러 처리**
   - 잘못된 입력 처리
   - 도구 실행 실패 시 복구
   - 사용자 친화적 에러 메시지 제공

### 1.3 사용 시나리오

#### 시나리오 1: 간단한 질문
```
사용자: "안녕하세요"
에이전트: "안녕하세요! 무엇을 도와드릴까요?"
```

#### 시나리오 2: 계산 요청
```
사용자: "123 + 456은 얼마인가요?"
에이전트: CalculatorTool 사용 → "579입니다."
```

#### 시나리오 3: 정보 검색
```
사용자: "오늘 날씨가 어때?"
에이전트: WeatherTool 사용 → "오늘 날씨는 맑고 기온은 20도입니다."
```

#### 시나리오 4: 복합 요청
```
사용자: "서울의 인구를 백만 단위로 계산해줘"
에이전트: SearchTool로 인구 조회 → CalculatorTool로 계산 → "약 9.7백만 명입니다."
```

---

## 2. 필요한 Tools 정의

### 2.1 필수 도구 (Phase 2에서 구현)

#### 1. CalculatorTool (계산기)
- **목적**: 수학 계산 수행
- **기능**:
  - 기본 사칙연산 (+, -, *, /)
  - 복잡한 수식 계산
- **입력**: 수학 표현식 문자열
- **출력**: 계산 결과

#### 2. SearchTool (검색 도구)
- **목적**: 정보 검색
- **기능**:
  - 웹 검색 (선택사항)
  - 내부 지식베이스 검색
- **입력**: 검색 쿼리
- **출력**: 검색 결과

### 2.2 선택적 도구 (Phase 3+에서 구현)

#### 3. WeatherTool (날씨 조회)
- **목적**: 날씨 정보 조회
- **기능**: 특정 지역의 날씨 정보 제공

#### 4. DatabaseTool (데이터베이스 쿼리)
- **목적**: 데이터베이스에서 정보 조회
- **기능**: SQL 쿼리 실행 (보안 고려 필요)

#### 5. FileTool (파일 시스템)
- **목적**: 파일 읽기/쓰기
- **기능**: 파일 내용 읽기, 파일 생성/수정

---

## 3. 제약사항 및 목표

### 3.1 성능 목표

- **응답 시간**: 
  - 단순 질문: 2초 이내
  - 도구 사용 포함: 5초 이내
  - 복잡한 요청: 10초 이내

- **동시 처리**: 
  - 최소 10개의 동시 요청 처리 가능

### 3.2 기능 제약사항

- **토큰 제한**: 
  - 입력: 최대 4000 토큰
  - 출력: 최대 2000 토큰

- **도구 실행 제한**:
  - 한 요청당 최대 5개의 도구 실행
  - 각 도구 실행 시간 제한: 10초

- **대화 히스토리**:
  - 최대 10턴의 대화 히스토리 유지

### 3.3 보안 및 프라이버시

- API 키는 환경 변수로 관리
- 사용자 입력 검증 및 sanitization
- 도구 실행 권한 제어
- 민감한 정보 로깅 방지

### 3.4 확장성

- 새로운 도구 추가 용이
- 다양한 LLM 모델 지원 (OpenAI, Ollama 등)
- 스트리밍 응답 지원 (Phase 3)

---

## 4. State 스키마 상세 설계

### 4.1 AgentState 구조

```java
public class AgentState {
    // 사용자 입력
    private UserMessage userMessage;
    
    // AI 응답
    private AiMessage aiMessage;
    
    // 도구 실행 요청 목록
    private List<ToolExecutionRequest> toolExecutionRequests;
    
    // 도구 실행 결과
    private List<ToolExecutionResult> toolExecutionResults;
    
    // 대화 히스토리 (전체 메시지)
    private List<ChatMessage> messages;
    
    // 현재 단계 추적
    private String currentStep; // "input", "llm", "tool", "response"
    
    // 반복 횟수 (무한 루프 방지)
    private int iterationCount;
    
    // 에러 정보
    private String error;
    private Exception exception;
    
    // 메타데이터
    private Map<String, Object> metadata;
}
```

### 4.2 State Reducer 전략

- **messages**: Append 방식으로 대화 히스토리 추가
- **toolExecutionRequests**: Replace 방식으로 최신 요청만 유지
- **toolExecutionResults**: Append 방식으로 결과 누적
- **currentStep**: Replace 방식으로 현재 단계 업데이트
- **iterationCount**: Increment 방식으로 증가

---

## 5. Graph 설계

### 5.1 그래프 구조

```
                    START
                      ↓
                [InputNode]
                      ↓
                 [LlmNode]
                      ↓
            [ConditionalNode]
         ↙                    ↘
    [ToolNode]            [ResponseNode]
         ↓                        ↓
    [LlmNode]                    END
         ↓
    [ConditionalNode]
         ↓
    [ResponseNode]
         ↓
         END
```

### 5.2 노드 상세 설계

#### InputNode
- **역할**: HTTP 요청에서 사용자 입력 추출
- **입력**: HTTP Request
- **출력**: AgentState (userMessage 설정)
- **에러 처리**: 입력 검증 실패 시 ErrorNode로 이동

#### LlmNode
- **역할**: LLM 호출하여 응답 생성
- **입력**: AgentState (messages, toolExecutionResults)
- **출력**: AgentState (aiMessage 업데이트)
- **조건**: 
  - 도구 실행이 필요한 경우 → ToolNode
  - 응답 완료 → ResponseNode

#### ConditionalNode
- **역할**: 다음 노드 결정
- **로직**:
  ```java
  if (aiMessage.hasToolExecutionRequests()) {
      return "tool";
  } else if (iterationCount > MAX_ITERATIONS) {
      return "error";
  } else {
      return "response";
  }
  ```

#### ToolNode
- **역할**: 필요한 도구 실행
- **입력**: AgentState (toolExecutionRequests)
- **출력**: AgentState (toolExecutionResults 업데이트)
- **에러 처리**: 도구 실행 실패 시 에러 메시지 추가 후 LlmNode로

#### ResponseNode
- **역할**: 최종 응답 포맷팅
- **입력**: AgentState (aiMessage)
- **출력**: HTTP Response
- **포맷**: JSON 또는 텍스트

#### ErrorNode (선택사항)
- **역할**: 에러 처리 및 복구
- **입력**: AgentState (error, exception)
- **출력**: AgentState (에러 메시지 설정) 또는 HTTP Error Response

### 5.3 Edge 정의

#### 일반 Edge (Normal Edges)
- START → InputNode
- InputNode → LlmNode
- ToolNode → LlmNode

#### 조건부 Edge (Conditional Edges)
- LlmNode → ConditionalNode
- ConditionalNode → ToolNode (조건: 도구 실행 필요)
- ConditionalNode → ResponseNode (조건: 응답 완료)
- ConditionalNode → ErrorNode (조건: 최대 반복 초과)

---

## 6. 데이터 흐름 다이어그램

### 6.1 정상 흐름

```
1. HTTP Request → InputNode
   - userMessage 설정
   
2. InputNode → LlmNode
   - LLM 호출
   - aiMessage 생성
   - toolExecutionRequests 생성 (필요 시)
   
3. LlmNode → ConditionalNode
   - 다음 단계 결정
   
4a. ConditionalNode → ToolNode (도구 필요)
   - 도구 실행
   - toolExecutionResults 업데이트
   - ToolNode → LlmNode (다시 LLM 호출)
   
4b. ConditionalNode → ResponseNode (응답 완료)
   - 최종 응답 생성
   - HTTP Response 반환
```

### 6.2 에러 흐름

```
에러 발생 → ErrorNode
   - 에러 메시지 설정
   - 사용자 친화적 에러 응답 생성
   - HTTP Error Response 반환
```

---

## 7. API 설계

### 7.1 REST API 엔드포인트

#### POST /api/agent/invoke
- **목적**: 에이전트 실행
- **요청**:
  ```json
  {
    "message": "사용자 메시지",
    "sessionId": "세션 ID (선택사항)",
    "options": {
      "temperature": 0.7,
      "maxIterations": 5
    }
  }
  ```
- **응답**:
  ```json
  {
    "response": "AI 응답",
    "sessionId": "세션 ID",
    "toolsUsed": ["CalculatorTool"],
    "executionTime": 1.5
  }
  ```

#### POST /api/agent/stream (Phase 3)
- **목적**: 스트리밍 응답
- **응답**: Server-Sent Events (SSE)

#### GET /api/agent/status
- **목적**: 에이전트 상태 조회
- **응답**: 상태 정보

---

## 8. 구현 우선순위

### Phase 2 (기본 구현)
1. ✅ InputNode 구현
2. ✅ LlmNode 구현
3. ✅ ConditionalNode 구현
4. ✅ ResponseNode 구현
5. ✅ CalculatorTool 구현
6. ✅ 기본 Graph 구성
7. ✅ AgentController 구현

### Phase 3 (고급 기능)
1. ToolNode 구현
2. ErrorNode 구현
3. 스트리밍 응답
4. 체크포인트 기능
5. 추가 도구들

---

## 9. 테스트 시나리오

### 9.1 단위 테스트
- 각 노드의 로직 테스트
- State Reducer 테스트
- 도구 실행 테스트

### 9.2 통합 테스트
- 전체 그래프 실행 테스트
- 에러 처리 테스트
- 도구 연동 테스트

### 9.3 E2E 테스트
- API 엔드포인트 테스트
- 실제 LLM 호출 테스트
- 복합 시나리오 테스트

---

**작성일**: 2025-01-XX
**다음 단계**: Phase 2 - 기본 코어 구현
