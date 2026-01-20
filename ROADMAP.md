# Spring AI + LangGraph4j AI Agent 개발 로드맵

## 프로젝트 개요
Spring AI와 LangGraph4j를 활용하여 지능형 AI 에이전트를 구축하는 프로젝트입니다.

---

## 📋 단계별 로드맵

### Phase 0: 준비 단계 (1주)
**목표**: 개발 환경 구축 및 핵심 기술 스택 이해

#### 주요 작업
- [ ] **의존성 추가**
  - Spring AI 의존성 추가 (`spring-ai-core`, `spring-ai-openai` 또는 `spring-ai-ollama`)
  - LangGraph4j 의존성 추가 (`langgraph4j-core`, `langgraph4j-spring-ai`)
  - LangChain4j 의존성 추가 (`langchain4j`)
  
- [ ] **개발 환경 설정**
  - Java 17+ 확인
  - Gradle 빌드 설정 업데이트
  - IDE 설정 (IntelliJ IDEA 권장)

- [ ] **기술 스택 학습**
  - LangGraph4j 핵심 개념 학습
    - StateGraph, Nodes, Edges
    - State 스키마 및 Reducer
    - Conditional vs Normal Edges
    - Async & Streaming
    - Checkpoint & Replay
  - Spring AI 기본 구조 이해
    - ChatModel 인터페이스
    - PromptTemplate
    - Vector Store
    - Tools & Functions

- [ ] **참고 자료**
  - [LangGraph4j 공식 문서](https://langgraph4j.github.io/langgraph4j/)
  - [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
  - [LangGraph4j GitHub](https://github.com/langgraph4j/langgraph4j)

---

### Phase 1: 요구사항 정의 및 설계 (1주)
**목표**: 에이전트의 역할과 기능 범위 명확화

#### 주요 작업
- [ ] **에이전트 역할 정의**
  - 예시: 고객지원 챗봇, 문서 요약 에이전트, Q&A 에이전트, 업무 자동화 에이전트 등
  - 목표 사용자 및 사용 시나리오 정의

- [ ] **필요한 Tools 정의**
  - 외부 API 연동 (예: 날씨 API, 검색 API)
  - 데이터베이스 쿼리
  - 파일 시스템 접근
  - Vector DB 연동 (RAG 구현 시)
  - 계산기, 검색 등 유틸리티 함수

- [ ] **제약사항 및 목표 정하기**
  - 응답 시간 목표
  - 스트리밍 응답 필요 여부
  - 상태 보존(메모리) 요구사항
  - 보안 및 프라이버시 요구사항
  - 확장성 요구사항

- [ ] **Graph 설계**
  - State 스키마 설계 (AgentState 상속)
  - 노드 구성 설계
    - Entry Node (시작점)
    - LLM 호출 노드
    - Tool 호출 노드
    - 조건 분기 노드
    - 응답 생성 노드
    - Exit Node (종료점)
  - Edge 흐름 설계 (순차, 조건부, 병렬)
  - 예외 처리 흐름 설계

- [ ] **산출물**
  - 에이전트 명세서
  - Graph 다이어그램 (Mermaid 또는 PlantUML)
  - State 스키마 문서

---

### Phase 2: 기본 코어 구현 (2주)
**목표**: 최소 기능을 가진 에이전트 프로토타입 구축

#### 주요 작업
- [ ] **프로젝트 구조 설정**
  ```
  src/main/java/ai/langgraph4j/msk/
  ├── agent/
  │   ├── state/          # State 정의
  │   ├── nodes/          # Node 구현
  │   ├── edges/          # Edge 정의
  │   └── graph/          # Graph 구성
  ├── tools/              # Tool 구현
  ├── config/             # Spring 설정
  └── controller/         # REST API
  ```

- [ ] **State 정의**
  - AgentState를 상속한 커스텀 State 클래스 생성
  - 필요한 필드 정의 (사용자 입력, 시스템 응답, 대화 히스토리, tool 사용 기록 등)
  - Reducer 구현 (Appender, Default 등)

- [ ] **LLM 모델 설정**
  - Spring AI ChatModel Bean 설정
  - OpenAI 또는 Ollama 연동
  - application.properties에 API 키 및 설정 추가

- [ ] **핵심 노드 구현**
  - **입력 처리 노드**: 사용자 입력을 State에 저장
  - **LLM 호출 노드**: ChatModel을 사용하여 LLM 호출
  - **Tool 호출 노드**: 필요한 Tool 실행
  - **조건 분기 노드**: 다음 노드 결정 로직
  - **응답 생성 노드**: 최종 응답 생성

- [ ] **간단한 Tool 구현**
  - 예: 계산기, 검색, 시간 조회 등
  - Spring AI Tool 인터페이스 구현

- [ ] **Graph 구성**
  - StateGraph 생성
  - 노드 추가
  - Edge 연결 (START → 노드들 → END)
  - Graph 컴파일

- [ ] **REST API 구현**
  - Spring MVC Controller 생성
  - 에이전트 실행 엔드포인트 (`POST /api/agent/invoke`)
  - 요청/응답 DTO 정의

- [ ] **기본 테스트**
  - 단위 테스트: 각 노드 로직 테스트
  - 통합 테스트: 전체 Graph 실행 테스트
  - API 테스트: REST 엔드포인트 테스트

---

### Phase 3: 고급 기능 추가 (2-3주)
**목표**: 확장성, 안정성, 사용자 경험 향상

#### 주요 작업
- [ ] **비동기 및 스트리밍 지원**
  - CompletableFuture를 활용한 비동기 노드 구현
  - 스트리밍 응답 지원 (Server-Sent Events 또는 WebSocket)
  - 중간 결과 전송 기능

- [ ] **체크포인트 & 브레이크포인트**
  - 상태 저장소 구현 (메모리 기반 또는 DB 기반)
  - Checkpoint 저장 및 복구 기능
  - 브레이크포인트를 통한 디버깅 지원
  - 장기 대화 세션 관리

- [ ] **병렬 처리**
  - 여러 노드 병렬 실행
  - Sub-graph 활용
  - 성능 최적화

- [ ] **멀티 에이전트 아키텍처** (선택사항)
  - 여러 에이전트 간 협업 구조
  - Agent Handoff 구현
  - 역할 분리 및 전문화

- [ ] **Observability (관측성)**
  - 로깅 강화 (각 노드 실행 로그)
  - Tracing 구현 (Spring Cloud Sleuth 또는 Micrometer)
  - 메트릭 수집 (Spring Boot Actuator)
  - Graph 실행 시각화

- [ ] **에러 처리 및 복구**
  - 예외 처리 전략 수립
  - Fallback 노드 구현
  - 재시도 로직
  - 사용자 친화적 에러 메시지

- [ ] **LangGraph4j Studio 연동** (선택사항)
  - Spring Boot 기반 Studio 설정
  - 워크플로우 시각화
  - 디버깅 도구 활용

---

### Phase 4: 통합 및 외부 연동 (1-2주)
**목표**: 외부 시스템과의 통합 및 프로덕션 준비

#### 주요 작업
- [ ] **Vector Store 연동** (RAG 구현 시)
  - Spring AI Vector Store 설정
  - 문서 임베딩 및 저장
  - 검색 및 리트리벌 구현

- [ ] **외부 API 통합**
  - REST API 클라이언트 구현
  - Webhook 연동
  - 인증 및 보안 처리

- [ ] **데이터베이스 연동**
  - 대화 히스토리 저장
  - 사용자 정보 관리
  - 상태 영속성 (Checkpoint 저장)

- [ ] **인증 및 보안**
  - Spring Security 설정
  - API 키 관리
  - 사용자 인증 및 권한 관리
  - 민감 정보 보호

- [ ] **API 문서화**
  - SpringDoc OpenAPI (Swagger) 설정
  - API 엔드포인트 문서화
  - 사용 예제 작성

---

### Phase 5: 배포 및 운영 (1주)
**목표**: 프로덕션 환경 배포 준비

#### 주요 작업
- [ ] **환경 설정**
  - 프로파일별 설정 (dev, staging, prod)
  - 환경 변수 관리
  - 외부 설정 파일 분리

- [ ] **컨테이너화**
  - Dockerfile 작성
  - Docker Compose 설정 (로컬 개발용)
  - 멀티 스테이지 빌드 최적화

- [ ] **CI/CD 파이프라인**
  - GitHub Actions 또는 GitLab CI 설정
  - 자동 빌드 및 테스트
  - 자동 배포 스크립트

- [ ] **모니터링 및 알림**
  - 로그 집계 설정
  - 메트릭 대시보드 구성
  - 알림 설정 (에러 발생 시)

- [ ] **성능 테스트**
  - 부하 테스트
  - 레이텐시 측정
  - 병목 지점 파악 및 최적화

- [ ] **문서화**
  - README 작성
  - 아키텍처 문서
  - 배포 가이드
  - 운영 매뉴얼

---

### Phase 6: 검증 및 개선 (지속적)
**목표**: 품질 확보 및 지속적 개선

#### 주요 작업
- [ ] **테스트 커버리지 향상**
  - 단위 테스트 커버리지 80% 이상 목표
  - 통합 테스트 강화
  - E2E 테스트 시나리오 작성

- [ ] **Spring AI Bench 활용** (선택사항)
  - 벤치마크 테스트 실행
  - 목표 지향 워크플로우 평가
  - 성능 비교 및 개선

- [ ] **사용자 피드백 수집**
  - 피드백 수집 메커니즘 구축
  - 로그 분석을 통한 문제점 파악
  - 개선 사항 반영

- [ ] **기능 확장**
  - 추가 Tool 구현
  - 멀티모달 지원 (이미지, 음성)
  - 다국어 지원
  - 고급 에이전트 패턴 적용

- [ ] **코드 품질 개선**
  - 리팩토링
  - 코드 리뷰
  - 모듈화 및 재사용성 향상

---

## 🛠️ 기술 스택

### 필수 의존성
- **Spring Boot**: 4.0.1+
- **Spring AI**: 최신 버전
- **LangGraph4j**: 1.7.5+
- **LangChain4j**: 최신 버전
- **Java**: 17+

### 선택적 의존성
- **Spring Security**: 인증/보안
- **Spring Data JPA**: 데이터베이스 연동
- **Spring Boot Actuator**: 모니터링
- **Micrometer**: 메트릭 수집
- **Docker**: 컨테이너화

---

## 📚 참고 자료

### 공식 문서
- [LangGraph4j 공식 문서](https://langgraph4j.github.io/langgraph4j/)
- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [LangChain4j GitHub](https://github.com/langchain4j/langchain4j)

### 유용한 링크
- [LangGraph4j GitHub](https://github.com/langgraph4j/langgraph4j)
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)
- [Spring AI Blog - Agents and Benchmarks](https://spring.io/blog/2025/10/28/agents-and-benchmarks/)

---

## 📝 체크리스트

각 Phase를 진행하면서 체크리스트를 활용하여 진행 상황을 추적하세요.

### Phase 0 체크리스트
- [ ] build.gradle에 필요한 의존성 추가 완료
- [ ] 프로젝트 빌드 성공 확인
- [ ] LangGraph4j 기본 예제 실행 성공
- [ ] Spring AI 기본 예제 실행 성공

### Phase 1 체크리스트
- [ ] 에이전트 역할 및 기능 명세서 작성 완료
- [ ] Graph 다이어그램 작성 완료
- [ ] State 스키마 설계 완료
- [ ] 필요한 Tools 목록 정의 완료

### Phase 2 체크리스트
- [ ] State 클래스 구현 완료
- [ ] 핵심 노드 구현 완료
- [ ] Graph 구성 및 컴파일 성공
- [ ] REST API 엔드포인트 동작 확인
- [ ] 기본 테스트 통과

### Phase 3 체크리스트
- [ ] 비동기 처리 구현 완료
- [ ] 스트리밍 응답 동작 확인
- [ ] 체크포인트 기능 구현 완료
- [ ] 로깅 및 모니터링 설정 완료
- [ ] 에러 처리 로직 구현 완료

### Phase 4 체크리스트
- [ ] 외부 API 연동 완료
- [ ] 데이터베이스 연동 완료
- [ ] 인증/보안 설정 완료
- [ ] API 문서화 완료

### Phase 5 체크리스트
- [ ] Docker 이미지 빌드 성공
- [ ] CI/CD 파이프라인 구축 완료
- [ ] 모니터링 대시보드 구성 완료
- [ ] 배포 문서 작성 완료

---

## 🎯 예상 일정

| Phase | 기간 | 누적 기간 |
|-------|------|----------|
| Phase 0: 준비 단계 | 1주 | 1주 |
| Phase 1: 요구사항 정의 및 설계 | 1주 | 2주 |
| Phase 2: 기본 코어 구현 | 2주 | 4주 |
| Phase 3: 고급 기능 추가 | 2-3주 | 6-7주 |
| Phase 4: 통합 및 외부 연동 | 1-2주 | 7-9주 |
| Phase 5: 배포 및 운영 | 1주 | 8-10주 |
| Phase 6: 검증 및 개선 | 지속적 | - |

**총 예상 기간**: 8-10주 (약 2-2.5개월)

---

## 💡 팁 및 권장사항

1. **점진적 개발**: 한 번에 모든 기능을 구현하려 하지 말고, 단계적으로 기능을 추가하세요.
2. **테스트 우선**: 각 기능을 구현할 때마다 테스트를 작성하세요.
3. **문서화**: 코드 작성과 동시에 문서를 작성하는 습관을 기르세요.
4. **커뮤니티 활용**: LangGraph4j와 Spring AI 커뮤니티를 적극 활용하세요.
5. **예제 참고**: 공식 예제와 샘플 코드를 참고하여 학습하세요.

---

**마지막 업데이트**: 2025-01-XX
**프로젝트 버전**: 0.0.1-SNAPSHOT
