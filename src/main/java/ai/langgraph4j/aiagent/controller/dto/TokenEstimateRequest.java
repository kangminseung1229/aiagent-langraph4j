package ai.langgraph4j.aiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 계산 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 계산 요청")
public class TokenEstimateRequest {

	/**
	 * 법령 ID (필수)
	 */
	@NotBlank(message = "법령 ID는 필수입니다.")
	@Schema(description = "법령 ID", example = "법령ID001")
	private String lawId;
}
