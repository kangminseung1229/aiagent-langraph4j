package ai.langgraph4j.msk.agent.nodes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ai.langgraph4j.msk.agent.state.AgentState;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM 호출 노드
 * Spring AI ChatModel을 사용하여 LLM을 호출하고 응답을 생성합니다.
 * 
 * AgentGraph에서 사용되며, Gemini API를 통해 LLM 응답을 생성합니다.
 * 
 * @Qualifier("chatModel")을 사용하여 AiConfig에서 생성한 chatModel Bean을 명시적으로 지정합니다.
 */
@Slf4j
@Component
public class LlmNode {

	private final ChatModel chatModel;

	@Value("${agent.max-iterations:5}")
	private int maxIterations;

	public LlmNode(@Qualifier("chatModel") ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	/**
	 * LLM을 호출하여 응답 생성
	 * 
	 * @param state 현재 상태
	 * @return 업데이트된 상태
	 */
	public AgentState process(AgentState state) {
		log.debug("LlmNode: LLM 호출 시작, 현재 반복 횟수: {}", state.getIterationCount());

		// LLM 호출 횟수 제한 체크
		if (state.getIterationCount() >= maxIterations) {
			log.warn("LlmNode: 최대 LLM 호출 횟수 초과 ({}), LLM 호출을 건너뜁니다", maxIterations);
			state.setError("최대 LLM 호출 횟수(" + maxIterations + "회)를 초과했습니다. 요청이 너무 복잡합니다.");
			state.setCurrentStep("error");
			return state;
		}

		try {
			// 반복 횟수 증가 (LLM 호출 전에 증가)
			state.incrementIterationCount();

			// 대화 히스토리 준비
			List<Message> messages = prepareMessages(state);

			// LLM 호출
			Prompt prompt = new Prompt(messages);
			ChatResponse response = chatModel.call(prompt);

			// 응답 추출
			String content = response.getResult().getOutput().getText();
			AiMessage aiMessage = new AiMessage(content);

			// 도구 실행 요청 추출
			// 주의: Tool 실행 결과가 이미 있으면 (Tool 실행 후 재호출),
			// LLM이 최종 응답을 생성하는 것이므로 Tool 요청을 생성하지 않음
			List<ToolExecutionRequest> toolRequests = new ArrayList<>();
			boolean hasToolResults = state.getToolExecutionResults() != null &&
					!state.getToolExecutionResults().isEmpty();

			if (!hasToolResults) {
				// Tool 실행 결과가 없을 때만 Tool 요청 추출 (사용자 원본 요청에서만)
				toolRequests = extractToolRequests(aiMessage);
				log.debug("LlmNode: Tool 요청 추출 - {}개", toolRequests.size());
			} else {
				log.debug("LlmNode: Tool 실행 결과가 있으므로 Tool 요청 추출 건너뜀 (최종 응답 생성 단계)");
			}

			// 상태 업데이트
			state.setAiMessage(aiMessage);
			state.setToolExecutionRequests(toolRequests);
			state.getMessages().add(aiMessage);
			state.setCurrentStep("llm");

			log.debug("LlmNode: LLM 호출 완료, 반복 횟수: {}", state.getIterationCount());

			return state;
		} catch (NonTransientAiException e) {
			log.error("LlmNode: LLM 호출 실패 (NonTransient)", e);

			// 예외 메시지에서 quota 관련 키워드 확인
			String errorMessage = e.getMessage();
			if (errorMessage != null && (errorMessage.contains("quota") ||
					errorMessage.contains("insufficient_quota") ||
					errorMessage.contains("429") ||
					errorMessage.contains("exceeded"))) {
				String errorMsg = "Gemini API 할당량이 초과되었습니다. API 키의 사용량을 확인하거나 결제 정보를 확인해주세요.";
				log.error("LlmNode: Gemini API 할당량 초과 - {}", errorMessage);
				state.setError(errorMsg);
			} else {
				state.setError("LLM 호출 중 오류 발생: " + errorMessage);
			}
			state.setException(e);
			state.setCurrentStep("error");
			return state;
		} catch (Exception e) {
			log.error("LlmNode: LLM 호출 실패", e);

			// 예외 메시지에서 quota 관련 키워드 확인
			String errorMessage = e.getMessage();
			if (errorMessage != null && (errorMessage.contains("quota") ||
					errorMessage.contains("insufficient_quota") ||
					errorMessage.contains("429") ||
					errorMessage.contains("exceeded"))) {
				state.setError("Gemini API 할당량이 초과되었습니다. API 키의 사용량을 확인하거나 결제 정보를 확인해주세요.");
			} else {
				state.setError("LLM 호출 중 오류 발생: " + errorMessage);
			}
			state.setException(e);
			state.setCurrentStep("error");
			return state;
		}
	}

	/**
	 * 대화 히스토리를 Spring AI Message 리스트로 변환
	 */
	private List<Message> prepareMessages(AgentState state) {
		List<Message> messages = new ArrayList<>();

		// 사용자 메시지가 있으면 추가
		if (state.getUserMessage() != null) {
			messages.add(new org.springframework.ai.chat.messages.UserMessage(
					state.getUserMessage().text()));
		}

		// 도구 실행 결과가 있으면 추가
		if (!state.getToolExecutionResults().isEmpty()) {
			StringBuilder toolResults = new StringBuilder("도구 실행 결과:\n");
			for (AgentState.ToolExecutionResult result : state.getToolExecutionResults()) {
				toolResults.append("- ").append(result.getToolName())
						.append(": ").append(result.getResult()).append("\n");
			}
			messages.add(new org.springframework.ai.chat.messages.UserMessage(toolResults.toString()));
		}

		return messages;
	}

	/**
	 * AI 메시지에서 도구 실행 요청 추출
	 * 
	 * Phase 2에서는 간단한 텍스트 파싱 방식으로 구현합니다.
	 * Phase 3에서 Spring AI Tool 통합으로 개선 예정입니다.
	 * 
	 * 현재는 계산 요청을 감지하여 CalculatorTool 호출 요청을 생성합니다.
	 */
	private List<ToolExecutionRequest> extractToolRequests(AiMessage aiMessage) {
		List<ToolExecutionRequest> toolRequests = new ArrayList<>();

		if (aiMessage == null || aiMessage.text() == null) {
			return toolRequests;
		}

		String text = aiMessage.text().toLowerCase();

		// 계산 요청 감지 패턴
		// 예: "123 + 456", "계산해줘", "10 * 5는?", "100을 4로 나눈 값" 등
		if (containsCalculationRequest(text)) {
			// 계산 표현식 추출 시도
			String expression = extractCalculationExpression(aiMessage.text());
			if (expression != null && !expression.isEmpty()) {
				ToolExecutionRequest request = ToolExecutionRequest.builder()
						.name("calculator")
						.arguments(expression)
						.build();
				toolRequests.add(request);
				log.debug("LlmNode: 계산 요청 감지 - {}", expression);
			}
		}

		return toolRequests;
	}

	/**
	 * 텍스트에 계산 요청이 포함되어 있는지 확인
	 */
	private boolean containsCalculationRequest(String text) {
		// 계산 관련 키워드
		String[] calculationKeywords = {
				"계산", "더하기", "빼기", "곱하기", "나누기",
				"calculate", "compute", "add", "subtract", "multiply", "divide",
				"+", "-", "*", "/", "×", "÷"
		};

		for (String keyword : calculationKeywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}

		// 숫자와 연산자 패턴 (예: "123 + 456", "10*5")
		if (text.matches(".*\\d+\\s*[+\\-*/×÷]\\s*\\d+.*")) {
			return true;
		}

		return false;
	}

	/**
	 * 텍스트에서 계산 표현식 추출
	 * 예: "123 + 456을 계산해줘" -> "123 + 456"
	 */
	private String extractCalculationExpression(String text) {
		// 숫자와 연산자 패턴 매칭
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
				"(\\d+(?:\\.\\d+)?)\\s*([+\\-*/×÷])\\s*(\\d+(?:\\.\\d+)?)");
		java.util.regex.Matcher matcher = pattern.matcher(text);

		if (matcher.find()) {
			String operand1 = matcher.group(1);
			String operator = matcher.group(2);
			String operand2 = matcher.group(3);

			// 연산자 변환 (× -> *, ÷ -> /)
			operator = operator.replace("×", "*").replace("÷", "/");

			return operand1 + " " + operator + " " + operand2;
		}

		return null;
	}
}
