package ai.langgraph4j.msk.agent.graph;

import org.springframework.stereotype.Component;

import ai.langgraph4j.msk.agent.nodes.ConditionalNode;
import ai.langgraph4j.msk.agent.nodes.InputNode;
import ai.langgraph4j.msk.agent.nodes.LlmNode;
import ai.langgraph4j.msk.agent.nodes.ResponseNode;
import ai.langgraph4j.msk.agent.nodes.ToolNode;
import ai.langgraph4j.msk.agent.state.AgentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	private final ToolNode toolNode;

	/**
	 * 그래프 실행
	 * 
	 * 참고: LangGraph4j의 StateGraph는 org.bsc.langgraph4j.state.State를 상속한
	 * 클래스를 요구하므로, 현재는 간단한 서비스 레이어로 구현합니다.
	 * Phase 3에서 완전한 LangGraph4j 통합을 진행합니다.
	 * 
	 * 그래프 흐름:
	 * 1. InputNode -> 사용자 입력 처리
	 * 2. LlmNode -> LLM 호출
	 * 3. ConditionalNode -> 다음 단계 결정
	 *    - "tool" -> ToolNode -> LlmNode (반복)
	 *    - "response" -> 종료
	 *    - "error" -> 종료
	 * 
	 * @param initialState 초기 상태
	 * @param userInput 사용자 입력
	 * @return 최종 상태
	 */
	public AgentState execute(AgentState initialState, String userInput) {
		log.info("AgentGraph: 그래프 실행 시작 - 입력: {}", userInput);
		
		AgentState state = initialState;
		
		// 1. InputNode: 사용자 입력 처리
		state = inputNode.process(state, userInput);
		if (state.getError() != null) {
			log.warn("AgentGraph: InputNode에서 에러 발생 - {}", state.getError());
			return state;
		}
		
		// 반복 루프: 최대 반복 횟수까지 Tool 실행 및 LLM 재호출
		int maxIterations = 5; // ConditionalNode에서도 체크하지만 여기서도 제한
		int iteration = 0;
		
		while (iteration < maxIterations) {
			iteration++;
			log.debug("AgentGraph: 반복 {} 시작", iteration);
			
			// 2. LlmNode: LLM 호출
			state = llmNode.process(state);
			if (state.getError() != null) {
				log.warn("AgentGraph: LlmNode에서 에러 발생 - {}", state.getError());
				return state;
			}
			
			// 3. ConditionalNode: 다음 단계 결정
			String nextStep = conditionalNode.route(state);
			log.debug("AgentGraph: 다음 단계 결정 - {}", nextStep);
			
			// 4. 조건부 분기 처리
			if ("response".equals(nextStep)) {
				// 응답 완료 - Controller에서 ResponseNode로 처리
				log.info("AgentGraph: 응답 완료, 반복 횟수: {}", iteration);
				return state;
			}
			
			if ("error".equals(nextStep)) {
				// 에러 발생
				log.warn("AgentGraph: 에러 발생 - {}", state.getError());
				return state;
			}
			
			if ("tool".equals(nextStep)) {
				// 5. ToolNode: 도구 실행
				log.debug("AgentGraph: 도구 실행 시작");
				state = toolNode.process(state);
				
				if (state.getError() != null) {
					log.warn("AgentGraph: ToolNode에서 에러 발생 - {}", state.getError());
					return state;
				}
				
				// Tool 실행 후 다시 LlmNode로 (반복 루프 계속)
				log.debug("AgentGraph: 도구 실행 완료, LLM 재호출");
				continue;
			}
			
			// 예상치 못한 nextStep
			log.warn("AgentGraph: 예상치 못한 nextStep - {}", nextStep);
			state.setError("예상치 못한 그래프 상태: " + nextStep);
			return state;
		}
		
		// 최대 반복 횟수 초과
		log.warn("AgentGraph: 최대 반복 횟수 초과 - {}", maxIterations);
		if (state.getError() == null || state.getError().isEmpty()) {
			state.setError("최대 반복 횟수를 초과했습니다. 요청이 너무 복잡합니다.");
		}
		
		return state;
	}
}
