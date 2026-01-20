package ai.langgraph4j.msk.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	/**
	 * 에러 여부
	 */
	@Builder.Default
	private Boolean error = true;

	/**
	 * 에러 코드
	 */
	private String errorCode;

	/**
	 * 에러 메시지
	 */
	private String message;

	/**
	 * 발생 시간
	 */
	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();
}
