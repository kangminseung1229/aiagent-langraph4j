# Nodes 패키지

이 패키지에는 LangGraph4j의 StateGraph에서 사용할 노드(Node) 구현체들이 위치합니다.

## 예상 구조

- `InputNode.java`: 사용자 입력을 처리하는 노드
- `LlmNode.java`: LLM을 호출하는 노드
- `ToolNode.java`: 도구를 실행하는 노드
- `ResponseNode.java`: 최종 응답을 생성하는 노드
- `ConditionalNode.java`: 조건 분기를 처리하는 노드
