# Phase 2-1: Tool 사용 기능 구현 완료 보고서

## 완료된 작업

### ✅ 1. ToolNode 구현
- **파일**: `src/main/java/ai/langgraph4j/msk/agent/nodes/ToolNode.java`
- **기능**:
  - `ToolExecutionRequest` 리스트를 받아서 각 도구를 실행
  - `CalculatorTool` 통합
  - 도구 실행 결과를 `AgentState`에 저장
  - 에러 처리 및 로깅

### ✅ 2. LlmNode 개선
- **파일**: `src/main/java/ai/langgraph4j/msk/agent/nodes/LlmNode.java`
- **개선 사항**:
  - `extractToolRequests()` 메서드 구현
  - LLM 응답 텍스트에서 계산 요청 감지
  - 계산 표현식 추출 (정규식 사용)
  - `ToolExecutionRequest` 생성

### ✅ 3. AgentGraph 통합
- **파일**: `src/main/java/ai/langgraph4j/msk/agent/graph/AgentGraph.java`
- **개선 사항**:
  - `ToolNode` 통합
  - 반복 루프 구현 (Tool 실행 → LLM 재호출)
  - 최대 반복 횟수 제한 (5회)
  - 조건부 분기 처리 개선

## 구현된 그래프 흐름

```
1. InputNode
   ↓
2. LlmNode (LLM 호출)
   ↓
3. ConditionalNode (다음 단계 결정)
   ├─ "tool" → ToolNode → LlmNode (반복)
   ├─ "response" → 종료
   └─ "error" → 종료
```

## Tool 실행 흐름

1. **사용자 입력**: "123 + 456을 계산해줘"
2. **InputNode**: 사용자 입력을 `AgentState`에 저장
3. **LlmNode**: 
   - LLM 호출
   - 응답에서 계산 요청 감지
   - `ToolExecutionRequest` 생성
4. **ConditionalNode**: "tool" 반환
5. **ToolNode**: 
   - `CalculatorTool` 실행
   - 결과를 `AgentState`에 저장
6. **LlmNode** (재호출):
   - Tool 실행 결과를 포함하여 LLM 재호출
   - 최종 응답 생성
7. **ConditionalNode**: "response" 반환
8. **종료**: Controller에서 `ResponseNode`로 최종 응답 생성

## 현재 제한사항

### 1. Tool 감지 방식
- 현재는 **텍스트 파싱** 방식으로 계산 요청을 감지
- LLM이 명시적으로 Tool을 호출하지 않음
- 정규식 기반 패턴 매칭 사용

### 2. 지원하는 Tool
- **CalculatorTool**만 구현됨
- 다른 Tool 추가 시 `ToolNode.executeTool()` 메서드에 case 추가 필요

### 3. ToolExecutionRequest 생성
- LLM 응답 텍스트에서 수동으로 추출
- Spring AI Tool 인터페이스 미사용

## 다음 단계 (Phase 2-2)

### 1. Spring AI Tool 통합
- `@Tool` 어노테이션 사용
- `ChatModel`에 Tool 등록
- LLM이 자동으로 Tool을 인식하고 호출하도록 개선

### 2. 추가 Tool 구현
- `SearchTool` 구현
- `WeatherTool` 구현 (선택사항)

### 3. Tool 감지 개선
- Spring AI Tool 통합으로 자동 Tool 호출 지원
- 텍스트 파싱 방식에서 벗어나기

## 테스트 방법

### 1. 간단한 계산 요청
```bash
curl -X POST http://localhost:8080/api/test/agent/invoke \
  -H "Content-Type: application/json" \
  -d '{
    "message": "123 + 456을 계산해줘",
    "sessionId": "test-1"
  }'
```

### 2. 복잡한 계산 요청
```bash
curl -X POST http://localhost:8080/api/test/agent/invoke \
  -H "Content-Type: application/json" \
  -d '{
    "message": "100을 4로 나눈 값은?",
    "sessionId": "test-2"
  }'
```

### 3. GET 요청 테스트
```bash
curl "http://localhost:8080/api/test/agent/test?message=10*5는%20얼마인가요?"
```

## 코드 구조

```
src/main/java/ai/langgraph4j/msk/
├── agent/
│   ├── graph/
│   │   └── AgentGraph.java          # 그래프 실행 로직 (반복 루프 포함)
│   ├── nodes/
│   │   ├── InputNode.java           # 입력 처리
│   │   ├── LlmNode.java              # LLM 호출 (Tool 요청 추출)
│   │   ├── ConditionalNode.java    # 조건 분기
│   │   ├── ToolNode.java            # ✨ 새로 추가: Tool 실행
│   │   └── ResponseNode.java        # 응답 생성
│   └── state/
│       └── AgentState.java          # 상태 관리
└── tools/
    └── CalculatorTool.java          # 계산기 도구
```

## 주요 변경 사항

### ToolNode.java (신규)
- `process()`: Tool 실행 요청 처리
- `executeTool()`: 개별 Tool 실행
- `CalculatorTool` 통합

### LlmNode.java (수정)
- `extractToolRequests()`: Tool 요청 추출 로직 구현
- `containsCalculationRequest()`: 계산 요청 감지
- `extractCalculationExpression()`: 계산 표현식 추출

### AgentGraph.java (수정)
- `ToolNode` 의존성 추가
- 반복 루프 구현
- Tool 실행 후 LLM 재호출 로직

---

**완료일**: 2025-01-XX
**다음 단계**: Phase 2-2 - Spring AI Tool 통합
