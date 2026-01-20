package ai.langgraph4j.msk.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 에이전트 실행 옵션
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentOptions {

	/**
	 * LLM 온도 설정 (0.0 ~ 2.0)
	 * 기본값: 0.7
	 */
	@Builder.Default
	private Double temperature = 0.7;

	/**
	 * 최대 반복 횟수
	 * 기본값: 5
	 */
	@Builder.Default
	private Integer maxIterations = 5;

	/**
	 * 스트리밍 응답 여부
	 * 기본값: false
	 */
	@Builder.Default
	private Boolean stream = false;
}
