package ai.langgraph4j.aiagent.controller.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 계산 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 계산 결과")
public class TokenEstimateResponse {

	@Schema(description = "법령 ID", example = "법령ID001")
	private String lawId;

	@Schema(description = "법령명", example = "소비자기본법")
	private String lawNameKorean;

	@Schema(description = "법령 키", example = "법령키001")
	private String lawKey;

	@Schema(description = "사용 모델", example = "gemini-embedding-001")
	private String embeddingModel;

	@Schema(description = "모델당 1M 토큰 비용 (USD)", example = "0.15")
	private BigDecimal costPerMillionTokens;

	@Schema(description = "총 조문 수", example = "50")
	private Integer totalArticles;

	@Schema(description = "총 예상 청크 수", example = "75")
	private Integer totalChunks;

	@Schema(description = "총 예상 토큰 수", example = "150000")
	private Long totalTokens;

	@Schema(description = "예상 비용 (USD)", example = "0.0225")
	private BigDecimal estimatedCost;

	@Schema(description = "조문별 상세 정보")
	private List<ArticleTokenInfo> articleDetails;

	/**
	 * 조문별 토큰 정보
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "조문별 토큰 정보")
	public static class ArticleTokenInfo {

		@Schema(description = "조문 ID", example = "1")
		private Long articleId;

		@Schema(description = "조문 키", example = "0018001")
		private String articleKey;

		@Schema(description = "조문 번호", example = "제18조")
		private String articleNumber;

		@Schema(description = "조문 제목", example = "소비자의 권리")
		private String articleTitle;

		@Schema(description = "조문 텍스트 길이 (문자 수)", example = "5000")
		private Integer textLength;

		@Schema(description = "예상 토큰 수", example = "1667")
		private Long estimatedTokens;

		@Schema(description = "예상 청크 수", example = "1")
		private Integer estimatedChunks;

		@Schema(description = "예상 비용 (USD)", example = "0.00025")
		private BigDecimal estimatedCost;
	}
}
