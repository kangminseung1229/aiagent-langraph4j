# API 설계 문서

## REST API 엔드포인트

### 1. 에이전트 실행

#### POST /api/agent/invoke

에이전트를 실행하여 사용자 메시지에 대한 응답을 생성합니다.

**요청**:
```http
POST /api/agent/invoke
Content-Type: application/json

{
  "message": "123 + 456은 얼마인가요?",
  "sessionId": "session-12345",
  "options": {
    "temperature": 0.7,
    "maxIterations": 5,
    "stream": false
  }
}
```

**요청 필드**:
- `message` (required): 사용자 메시지
- `sessionId` (optional): 세션 ID (대화 히스토리 유지용)
- `options` (optional): 추가 옵션
  - `temperature`: LLM 온도 설정 (0.0 ~ 2.0)
  - `maxIterations`: 최대 반복 횟수 (기본값: 5)
  - `stream`: 스트리밍 응답 여부 (기본값: false)

**응답** (성공):
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "response": "579입니다.",
  "sessionId": "session-12345",
  "toolsUsed": ["CalculatorTool"],
  "executionTime": 1.5,
  "iterationCount": 2
}
```

**응답 필드**:
- `response`: AI 응답 텍스트
- `sessionId`: 세션 ID
- `toolsUsed`: 사용된 도구 목록
- `executionTime`: 실행 시간 (초)
- `iterationCount`: 반복 횟수

**응답** (에러):
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": true,
  "errorCode": "INVALID_INPUT",
  "message": "메시지가 비어있습니다.",
  "timestamp": "2025-01-19T10:30:00Z"
}
```

**에러 코드**:
- `INVALID_INPUT`: 잘못된 입력
- `MAX_ITERATIONS_EXCEEDED`: 최대 반복 횟수 초과
- `LLM_ERROR`: LLM 호출 실패
- `TOOL_ERROR`: 도구 실행 실패
- `INTERNAL_ERROR`: 내부 서버 오류

---

### 2. 스트리밍 응답 (Phase 3)

#### POST /api/agent/stream

에이전트를 실행하여 스트리밍 방식으로 응답을 받습니다.

**요청**:
```http
POST /api/agent/stream
Content-Type: application/json

{
  "message": "서울의 인구를 알려줘",
  "sessionId": "session-12345"
}
```

**응답** (Server-Sent Events):
```http
HTTP/1.1 200 OK
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive

data: {"type":"start","sessionId":"session-12345"}

data: {"type":"node","node":"InputNode","status":"completed"}

data: {"type":"node","node":"LlmNode","status":"processing"}

data: {"type":"chunk","text":"서울의"}

data: {"type":"chunk","text":" 인구는"}

data: {"type":"chunk","text":" 약 9.7백만 명입니다."}

data: {"type":"tool","tool":"SearchTool","status":"executing"}

data: {"type":"complete","response":"서울의 인구는 약 9.7백만 명입니다."}
```

**이벤트 타입**:
- `start`: 스트리밍 시작
- `node`: 노드 실행 상태
- `chunk`: 텍스트 청크 (스트리밍)
- `tool`: 도구 실행 상태
- `complete`: 완료
- `error`: 에러 발생

---

### 3. 에이전트 상태 조회

#### GET /api/agent/status

에이전트의 현재 상태를 조회합니다.

**요청**:
```http
GET /api/agent/status
```

**응답**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "running",
  "activeSessions": 5,
  "totalRequests": 1234,
  "averageResponseTime": 2.3,
  "version": "0.0.1-SNAPSHOT"
}
```

---

### 4. 세션 관리 (Phase 3)

#### GET /api/agent/session/{sessionId}

특정 세션의 대화 히스토리를 조회합니다.

**요청**:
```http
GET /api/agent/session/session-12345
```

**응답**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "sessionId": "session-12345",
  "messages": [
    {
      "role": "user",
      "content": "안녕하세요",
      "timestamp": "2025-01-19T10:00:00Z"
    },
    {
      "role": "assistant",
      "content": "안녕하세요! 무엇을 도와드릴까요?",
      "timestamp": "2025-01-19T10:00:01Z"
    }
  ],
  "createdAt": "2025-01-19T10:00:00Z",
  "lastActivity": "2025-01-19T10:05:00Z"
}
```

#### DELETE /api/agent/session/{sessionId}

세션을 삭제합니다.

**요청**:
```http
DELETE /api/agent/session/session-12345
```

**응답**:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "세션이 삭제되었습니다."
}
```

---

## DTO 클래스 설계

### AgentRequest

```java
public class AgentRequest {
    @NotBlank
    private String message;
    
    private String sessionId;
    
    private AgentOptions options;
    
    // Getters and Setters
}
```

### AgentOptions

```java
public class AgentOptions {
    private Double temperature = 0.7;
    private Integer maxIterations = 5;
    private Boolean stream = false;
    
    // Getters and Setters
}
```

### AgentResponse

```java
public class AgentResponse {
    private String response;
    private String sessionId;
    private List<String> toolsUsed;
    private Double executionTime;
    private Integer iterationCount;
    
    // Getters and Setters
}
```

### ErrorResponse

```java
public class ErrorResponse {
    private Boolean error = true;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    
    // Getters and Setters
}
```

---

## 예외 처리

### 전역 예외 핸들러

```java
@RestControllerAdvice
public class AgentExceptionHandler {
    
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(
            InvalidInputException e) {
        ErrorResponse error = ErrorResponse.builder()
            .errorCode("INVALID_INPUT")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(MaxIterationsExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxIterations(
            MaxIterationsExceededException e) {
        ErrorResponse error = ErrorResponse.builder()
            .errorCode("MAX_ITERATIONS_EXCEEDED")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(429).body(error);
    }
    
    // 기타 예외 처리...
}
```

---

## 인증 및 보안 (Phase 4)

### API 키 인증

```http
POST /api/agent/invoke
Authorization: Bearer {api-key}
Content-Type: application/json
```

### Rate Limiting

- IP당 분당 60회 요청 제한
- 세션당 분당 30회 요청 제한

---

**작성일**: 2025-01-XX
**버전**: 1.0
