package ai.langgraph4j.msk.agent.nodes;

import ai.langgraph4j.msk.agent.state.AgentState;
import ai.langgraph4j.msk.controller.dto.AgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 응답 생성 노드
 * 최종 응답을 포맷팅하여 반환합니다.
 */
@Slf4j
@Component
public class ResponseNode {

	/**
	 * 최종 응답 생성
	 * 
	 * @param state 현재 상태
	 * @param startTime 실행 시작 시간 (밀리초)
	 * @return AgentResponse
	 */
	public AgentResponse process(AgentState state, long startTime) {
		log.debug("ResponseNode: 응답 생성 시작");
		
		// 응답 텍스트 추출
		String responseText = "";
		if (state.getAiMessage() != null) {
			responseText = state.getAiMessage().text();
		} else if (state.getError() != null) {
			responseText = "죄송합니다. 오류가 발생했습니다: " + state.getError();
		} else {
			responseText = "응답을 생성할 수 없습니다.";
		}
		
		// 사용된 도구 목록 추출
		List<String> toolsUsed = extractToolsUsed(state);
		
		// 실행 시간 계산
		double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;
		
		// 응답 생성
		AgentResponse response = AgentResponse.builder()
				.response(responseText)
				.sessionId(state.getSessionId())
				.toolsUsed(toolsUsed)
				.executionTime(executionTime)
				.iterationCount(state.getIterationCount())
				.build();
		
		state.setCurrentStep("response");
		log.debug("ResponseNode: 응답 생성 완료, 실행 시간: {}초", executionTime);
		
		return response;
	}

	/**
	 * 사용된 도구 목록 추출
	 */
	private List<String> extractToolsUsed(AgentState state) {
		List<String> toolsUsed = new ArrayList<>();
		
		if (state.getToolExecutionResults() != null) {
			for (AgentState.ToolExecutionResult result : state.getToolExecutionResults()) {
				if (result.isSuccess() && result.getToolName() != null) {
					toolsUsed.add(result.getToolName());
				}
			}
		}
		
		return toolsUsed;
	}
}
