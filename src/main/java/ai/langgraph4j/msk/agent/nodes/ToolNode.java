package ai.langgraph4j.msk.agent.nodes;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import ai.langgraph4j.msk.agent.state.AgentState;
import ai.langgraph4j.msk.tools.CalculatorTool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도구 실행 노드
 * LLM이 요청한 도구를 실행하고 결과를 상태에 저장합니다.
 * 
 * Phase 2에서는 수동 Tool 호출 방식으로 구현하며,
 * Phase 3에서 Spring AI Tool 인터페이스로 개선 예정입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolNode {

	private final CalculatorTool calculatorTool;

	/**
	 * 도구 실행 요청을 처리하여 도구를 실행하고 결과를 상태에 저장
	 * 
	 * @param state 현재 상태
	 * @return 업데이트된 상태
	 */
	public AgentState process(AgentState state) {
		log.debug("ToolNode: 도구 실행 시작, 요청 수: {}", 
				state.getToolExecutionRequests() != null ? state.getToolExecutionRequests().size() : 0);

		// 도구 실행 요청이 없으면 그대로 반환
		if (state.getToolExecutionRequests() == null || state.getToolExecutionRequests().isEmpty()) {
			log.debug("ToolNode: 도구 실행 요청이 없습니다.");
			return state;
		}

		// 도구 실행 결과 리스트 초기화 (이전 결과는 유지)
		if (state.getToolExecutionResults() == null) {
			state.setToolExecutionResults(new ArrayList<>());
		}

		// 각 도구 실행 요청 처리
		for (ToolExecutionRequest request : state.getToolExecutionRequests()) {
			try {
				AgentState.ToolExecutionResult result = executeTool(request);
				state.getToolExecutionResults().add(result);
				
				log.debug("ToolNode: 도구 실행 완료 - {}: {}", 
						result.getToolName(), result.isSuccess() ? "성공" : "실패");
			} catch (Exception e) {
				log.error("ToolNode: 도구 실행 중 오류 발생 - {}", request.name(), e);
				
				// 에러 결과 추가
				AgentState.ToolExecutionResult errorResult = new AgentState.ToolExecutionResult();
				errorResult.setToolName(request.name());
				errorResult.setSuccess(false);
				errorResult.setError("도구 실행 중 오류 발생: " + e.getMessage());
				state.getToolExecutionResults().add(errorResult);
			}
		}

		// 상태 업데이트
		state.setCurrentStep("tool");
		// 도구 실행 요청은 초기화 (다음 LLM 호출을 위해)
		state.setToolExecutionRequests(new ArrayList<>());

		log.debug("ToolNode: 도구 실행 완료, 결과 수: {}", state.getToolExecutionResults().size());

		return state;
	}

	/**
	 * 개별 도구 실행
	 * 
	 * @param request 도구 실행 요청
	 * @return 도구 실행 결과
	 */
	private AgentState.ToolExecutionResult executeTool(ToolExecutionRequest request) {
		String toolName = request.name();
		String arguments = request.arguments() != null ? request.arguments() : "";

		log.debug("ToolNode: 도구 실행 - 이름: {}, 인자: {}", toolName, arguments);

		AgentState.ToolExecutionResult result = new AgentState.ToolExecutionResult();
		result.setToolName(toolName);

		try {
			// 도구별 실행 로직
			switch (toolName.toLowerCase()) {
				case "calculator":
				case "calculate":
					String calculationResult = calculatorTool.calculate(arguments);
					result.setResult(calculationResult);
					result.setSuccess(true);
					break;

				default:
					result.setSuccess(false);
					result.setError("지원하지 않는 도구입니다: " + toolName);
					log.warn("ToolNode: 지원하지 않는 도구 - {}", toolName);
			}
		} catch (Exception e) {
			result.setSuccess(false);
			result.setError("도구 실행 중 예외 발생: " + e.getMessage());
			log.error("ToolNode: 도구 실행 예외 - {}", toolName, e);
		}

		return result;
	}
}
