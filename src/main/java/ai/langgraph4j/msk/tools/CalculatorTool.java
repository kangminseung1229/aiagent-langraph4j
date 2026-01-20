package ai.langgraph4j.msk.tools;

import org.springframework.stereotype.Component;

/**
 * 계산기 도구
 * AI 에이전트가 수학 계산을 수행할 수 있도록 하는 도구입니다.
 * 
 * Phase 2에서는 간단한 구현으로 시작하며, Phase 3에서 Spring AI Tool 인터페이스로 통합 예정
 */
@Component
public class CalculatorTool {

	/**
	 * 계산기 함수 정의
	 * 
	 * @param expression 수학 표현식 (예: "123 + 456", "10 * 5")
	 * @return 계산 결과
	 */
	public String calculate(String expression) {
		try {
			// 간단한 수식 계산 (실제로는 더 복잡한 파서가 필요할 수 있음)
			expression = expression.trim();
			
			// 덧셈
			if (expression.contains("+")) {
				String[] parts = expression.split("\\+");
				double result = Double.parseDouble(parts[0].trim()) + 
				                Double.parseDouble(parts[1].trim());
				return String.valueOf(result);
			}
			// 뺄셈
			else if (expression.contains("-") && !expression.startsWith("-")) {
				String[] parts = expression.split("-", 2);
				double result = Double.parseDouble(parts[0].trim()) - 
				                Double.parseDouble(parts[1].trim());
				return String.valueOf(result);
			}
			// 곱셈
			else if (expression.contains("*")) {
				String[] parts = expression.split("\\*");
				double result = Double.parseDouble(parts[0].trim()) * 
				                Double.parseDouble(parts[1].trim());
				return String.valueOf(result);
			}
			// 나눗셈
			else if (expression.contains("/")) {
				String[] parts = expression.split("/");
				double divisor = Double.parseDouble(parts[1].trim());
				if (divisor == 0) {
					return "오류: 0으로 나눌 수 없습니다.";
				}
				double result = Double.parseDouble(parts[0].trim()) / divisor;
				return String.valueOf(result);
			}
			else {
				return "지원하지 않는 연산입니다. +, -, *, / 만 지원합니다.";
			}
		} catch (Exception e) {
			return "계산 오류: " + e.getMessage();
		}
	}
}
