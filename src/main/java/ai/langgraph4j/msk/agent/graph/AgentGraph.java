package ai.langgraph4j.msk.agent.graph;

import ai.langgraph4j.msk.agent.nodes.ConditionalNode;
import ai.langgraph4j.msk.agent.nodes.InputNode;
import ai.langgraph4j.msk.agent.nodes.LlmNode;
import ai.langgraph4j.msk.agent.nodes.ResponseNode;
import ai.langgraph4j.msk.agent.state.AgentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 에이전트 그래프 정의
 * LangGraph4j StateGraph를 사용하여 에이전트 워크플로우를 구성합니다.
 * 
 * 참고: Phase 2에서는 간단한 구현으로 시작하며, Phase 3에서 LangGraph4j의 
 * 완전한 통합을 진행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentGraph {

	private final InputNode inputNode;
	private final LlmNode llmNode;
	private final ConditionalNode conditionalNode;
	private final ResponseNode responseNode;

	/**
	 * 그래프 빌드
	 * 
	 * 참고: LangGraph4j의 StateGraph는 org.bsc.langgraph4j.state.State를 상속한
	 * 클래스를 요구하므로, 현재는 간단한 서비스 레이어로 구현합니다.
	 * Phase 3에서 완전한 LangGraph4j 통합을 진행합니다.
	 * 
	 * @return 그래프 실행 함수
	 */
	public AgentState execute(AgentState initialState, String userInput) {
		log.info("AgentGraph: 그래프 실행 시작");
		
		AgentState state = initialState;
		
		// 1. InputNode: 사용자 입력 처리
		state = inputNode.process(state, userInput);
		if (state.getError() != null) {
			return state;
		}
		
		// 2. LlmNode: LLM 호출
		state = llmNode.process(state);
		if (state.getError() != null) {
			return state;
		}
		
		// 3. ConditionalNode: 다음 단계 결정
		String nextStep = conditionalNode.route(state);
		
		// 4. ResponseNode: 응답 생성 (조건부 분기는 간단하게 처리)
		if ("response".equals(nextStep) || "error".equals(nextStep)) {
			// 응답은 Controller에서 처리
			return state;
		}
		
		// 도구가 필요한 경우 (Phase 3에서 구현)
		if ("tool".equals(nextStep)) {
			// 현재는 LLM 응답만 반환
			return state;
		}
		
		return state;
	}
}
