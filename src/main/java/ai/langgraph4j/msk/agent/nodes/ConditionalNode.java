package ai.langgraph4j.msk.agent.nodes;

import ai.langgraph4j.msk.agent.state.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 조건 분기 노드
 * 현재 상태를 기반으로 다음 노드를 결정합니다.
 */
@Slf4j
@Component
public class ConditionalNode {

	@Value("${agent.max-iterations:5}")
	private int maxIterations;

	/**
	 * 다음 노드를 결정하는 라우팅 함수
	 * 
	 * @param state 현재 상태
	 * @return 다음 노드 이름 ("tool", "response", "error")
	 */
	public String route(AgentState state) {
		log.debug("ConditionalNode: 다음 노드 결정 시작, 반복 횟수: {}", state.getIterationCount());
		
		// 최대 반복 횟수 초과 체크
		if (state.getIterationCount() > maxIterations) {
			log.warn("ConditionalNode: 최대 반복 횟수 초과 ({}), 에러 노드로 이동", maxIterations);
			state.setError("최대 반복 횟수를 초과했습니다. 요청이 너무 복잡합니다.");
			state.setCurrentStep("error");
			return "error";
		}
		
		// 도구 실행이 필요한 경우
		if (state.getToolExecutionRequests() != null && 
		    !state.getToolExecutionRequests().isEmpty()) {
			log.debug("ConditionalNode: 도구 실행 필요, ToolNode로 이동");
			return "tool";
		}
		
		// 에러가 있는 경우
		if (state.getError() != null && !state.getError().isEmpty()) {
			log.debug("ConditionalNode: 에러 발생, ErrorNode로 이동");
			return "error";
		}
		
		// 정상 응답 완료
		log.debug("ConditionalNode: 응답 완료, ResponseNode로 이동");
		return "response";
	}
}
