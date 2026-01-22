package ai.langgraph4j.aiagent.agent.nodes;

import ai.langgraph4j.aiagent.agent.state.AgentState;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 입력 처리 노드
 * 사용자 입력을 받아 AgentState에 저장합니다.
 */
@Slf4j
@Component
public class InputNode {

	/**
	 * 사용자 입력을 처리하여 상태에 저장
	 * 
	 * @param state 현재 상태
	 * @param userInput 사용자 입력 문자열
	 * @return 업데이트된 상태
	 */
	public AgentState process(AgentState state, String userInput) {
		log.debug("InputNode: 사용자 입력 처리 시작 - {}", userInput);
		
		// 입력 검증
		if (userInput == null || userInput.trim().isEmpty()) {
			state.setError("사용자 입력이 비어있습니다.");
			state.setCurrentStep("error");
			return state;
		}
		
		// UserMessage 생성 및 상태 업데이트
		UserMessage userMessage = new UserMessage(userInput.trim());
		state.setUserMessage(userMessage);
		state.getMessages().add(userMessage);
		state.setCurrentStep("input");
		
		log.debug("InputNode: 사용자 입력 처리 완료");
		
		return state;
	}
}
