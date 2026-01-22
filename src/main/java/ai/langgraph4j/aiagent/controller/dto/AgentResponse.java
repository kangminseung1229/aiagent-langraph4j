package ai.langgraph4j.aiagent.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 에이전트 실행 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

	/**
	 * AI 응답 텍스트
	 */
	private String response;

	/**
	 * 세션 ID
	 */
	private String sessionId;

	/**
	 * 사용된 도구 목록
	 */
	private List<String> toolsUsed;

	/**
	 * 실행 시간 (초)
	 */
	private Double executionTime;

	/**
	 * 반복 횟수
	 */
	private Integer iterationCount;
}
