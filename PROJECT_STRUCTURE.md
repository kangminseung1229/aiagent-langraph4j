# 프로젝트 디렉토리 구조 설명

이 문서는 Spring AI + LangGraph4j 기반 AI 에이전트 프로젝트의 디렉토리 구조와 각 디렉토리의 역할을 설명합니다.

---

## 📁 전체 프로젝트 구조

```
src/main/java/ai/langgraph4j/msk/
├── MskApplication.java          # Spring Boot 메인 애플리케이션 클래스
├── config/                      # 설정 클래스들
│   └── AiConfig.java           # Spring AI 설정 (ChatModel, ChatClient)
├── agent/                      # AI 에이전트 핵심 로직
│   ├── state/                  # 상태 정의
│   │   └── AgentState.java    # 에이전트 상태 스키마
│   ├── nodes/                  # 그래프 노드 구현
│   └── graph/                  # 그래프 구성 및 정의
├── tools/                      # 에이전트가 사용할 도구들
└── controller/                 # REST API 컨트롤러
```

---

## 📂 각 디렉토리 상세 설명

### 1. `config/` - 설정 클래스

**목적**: Spring Boot 및 Spring AI 관련 설정을 담당합니다.

**주요 파일**:
- `AiConfig.java`: Spring AI의 `ChatModel`과 `ChatClient`를 Bean으로 등록하는 설정 클래스

**역할**:
- AI 모델 초기화 및 설정
- ChatClient 빌더 설정
- 조건부 Bean 생성 (`@ConditionalOnBean`)

**예상 추가 파일**:
- `GraphConfig.java`: LangGraph4j 그래프를 Spring Bean으로 등록
- `ToolConfig.java`: 도구들을 Spring Bean으로 등록
- `SecurityConfig.java`: 보안 설정 (필요 시)

---

### 2. `agent/` - AI 에이전트 핵심 로직

**목적**: LangGraph4j를 사용한 AI 에이전트의 핵심 로직을 구현합니다.

#### 2.1 `agent/state/` - 상태 정의

**목적**: LangGraph4j의 StateGraph에서 사용할 상태(State) 스키마를 정의합니다.

**주요 파일**:
- `AgentState.java`: 에이전트의 상태를 나타내는 클래스
  - 사용자 메시지 (`UserMessage`)
  - AI 응답 메시지 (`AiMessage`)
  - 도구 실행 요청 목록 (`ToolExecutionRequest`)
  - 대화 히스토리 (`messages`)
  - 에러 메시지 (`error`)

**역할**:
- 그래프 전체에서 공유되는 상태 데이터 구조 정의
- 각 노드 간 데이터 전달을 위한 스키마 제공
- Reducer 패턴 구현 (상태 병합 로직)

**예상 추가 파일**:
- `ConversationState.java`: 대화 세션 상태 관리
- `TaskState.java`: 작업 실행 상태 관리

---

#### 2.2 `agent/nodes/` - 그래프 노드 구현

**목적**: LangGraph4j StateGraph의 각 노드(Node)를 구현합니다.

**역할**:
- 그래프의 각 단계를 처리하는 노드 구현
- 각 노드는 특정 작업을 수행하고 상태를 업데이트
- `NodeAction` 인터페이스를 구현하여 노드 로직 정의

**예상 파일 구조**:
```
nodes/
├── InputNode.java          # 사용자 입력을 받아 상태에 저장
├── LlmNode.java            # LLM을 호출하여 응답 생성
├── ToolNode.java           # 도구를 실행하는 노드
├── ResponseNode.java       # 최종 응답을 생성하는 노드
├── ConditionalNode.java    # 조건에 따라 다음 노드 결정
└── ErrorNode.java          # 에러 처리 노드
```

**각 노드의 역할**:
- **InputNode**: HTTP 요청에서 사용자 입력을 추출하여 `AgentState`에 저장
- **LlmNode**: Spring AI의 `ChatModel`을 사용하여 LLM 호출 및 응답 생성
- **ToolNode**: 필요한 도구를 실행하고 결과를 상태에 저장
- **ResponseNode**: 최종 응답을 포맷팅하여 반환
- **ConditionalNode**: 조건에 따라 다음 노드를 결정 (예: 도구 실행 필요 여부)
- **ErrorNode**: 에러 발생 시 처리 및 복구 로직

---

#### 2.3 `agent/graph/` - 그래프 구성 및 정의

**목적**: LangGraph4j의 StateGraph를 정의하고 구성합니다.

**역할**:
- StateGraph 빌더를 사용하여 그래프 구조 정의
- 노드들을 연결하고 Edge(간선) 설정
- 조건부 Edge 및 일반 Edge 정의
- 그래프를 컴파일하여 실행 가능한 형태로 변환
- Spring Bean으로 등록하여 다른 컴포넌트에서 사용 가능하게 함

**예상 파일 구조**:
```
graph/
├── AgentGraph.java         # 메인 에이전트 그래프 정의
├── GraphConfig.java        # 그래프를 Spring Bean으로 등록
└── GraphBuilder.java       # 그래프 빌더 유틸리티 (선택사항)
```

**그래프 흐름 예시**:
```
START → InputNode → LlmNode → ConditionalNode
                              ↓ (도구 필요)
                         ToolNode → LlmNode → ResponseNode → END
```

---

### 3. `tools/` - 도구(Tools) 구현

**목적**: AI 에이전트가 사용할 도구들을 구현합니다.

**역할**:
- Spring AI의 `Tool` 인터페이스를 구현하여 도구 정의
- LLM이 필요에 따라 호출할 수 있는 함수들 제공
- 외부 API 호출, 데이터베이스 쿼리, 계산 등 다양한 기능 제공

**예상 파일 구조**:
```
tools/
├── CalculatorTool.java     # 계산기 도구
├── SearchTool.java         # 검색 도구 (예: 웹 검색)
├── WeatherTool.java        # 날씨 조회 도구
├── DatabaseTool.java       # 데이터베이스 쿼리 도구
└── FileTool.java           # 파일 시스템 접근 도구
```

**도구 구현 예시**:
- **CalculatorTool**: 수학 계산 수행
- **SearchTool**: 외부 검색 API 호출
- **WeatherTool**: 날씨 API 호출
- **DatabaseTool**: 데이터베이스 쿼리 실행
- **FileTool**: 파일 읽기/쓰기

**도구 등록 방법**:
- Spring Bean으로 등록하여 자동 감지
- 또는 `GraphConfig`에서 명시적으로 등록

---

### 4. `controller/` - REST API 컨트롤러

**목적**: 외부에서 에이전트를 호출할 수 있는 REST API 엔드포인트를 제공합니다.

**역할**:
- HTTP 요청을 받아 에이전트 그래프 실행
- 요청/응답 DTO 정의
- 에러 처리 및 예외 핸들링
- 스트리밍 응답 지원 (Server-Sent Events 또는 WebSocket)

**예상 파일 구조**:
```
controller/
├── AgentController.java     # 메인 에이전트 API 컨트롤러
├── dto/                     # 요청/응답 DTO
│   ├── AgentRequest.java
│   └── AgentResponse.java
└── exception/               # 예외 처리
    └── AgentExceptionHandler.java
```

**주요 엔드포인트 예시**:
- `POST /api/agent/invoke`: 에이전트 실행
- `POST /api/agent/stream`: 스트리밍 응답
- `GET /api/agent/status`: 에이전트 상태 조회
- `POST /api/agent/resume`: 체크포인트에서 재개

---

## 🔄 데이터 흐름

```
HTTP Request (Controller)
    ↓
AgentState 초기화
    ↓
Graph 실행 시작
    ↓
InputNode → 사용자 입력 처리
    ↓
LlmNode → LLM 호출
    ↓
ConditionalNode → 다음 단계 결정
    ↓
ToolNode (필요 시) → 도구 실행
    ↓
LlmNode → 최종 응답 생성
    ↓
ResponseNode → 응답 포맷팅
    ↓
HTTP Response (Controller)
```

---

## 📝 개발 가이드

### 새로운 노드 추가하기
1. `agent/nodes/` 디렉토리에 새 노드 클래스 생성
2. `NodeAction` 인터페이스 구현
3. `agent/graph/`의 그래프 정의에 노드 추가

### 새로운 도구 추가하기
1. `tools/` 디렉토리에 새 도구 클래스 생성
2. Spring AI의 `Tool` 인터페이스 구현
3. `@Component` 또는 `@Bean`으로 등록
4. 그래프 설정에서 도구 등록

### 새로운 API 엔드포인트 추가하기
1. `controller/` 디렉토리에 컨트롤러 메서드 추가
2. 필요한 DTO 클래스 생성
3. 에러 처리 로직 추가

---

## 🎯 다음 단계

각 디렉토리의 README 파일을 참고하여 다음 작업을 진행하세요:

1. **Phase 2**: `agent/nodes/`에 기본 노드 구현
2. **Phase 2**: `agent/graph/`에 그래프 정의
3. **Phase 2**: `tools/`에 간단한 도구 구현
4. **Phase 2**: `controller/`에 REST API 구현

---

**마지막 업데이트**: 2025-01-XX
