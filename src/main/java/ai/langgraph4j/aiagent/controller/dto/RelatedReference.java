package ai.langgraph4j.aiagent.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관련 자료 참조 (검색 결과 기반 링크용)
 * documentType과 id를 기반으로 상세 페이지 URL을 가집니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedReference {

	/**
	 * 문서 타입: "counsel", "lawArticle", "yp"
	 */
	private String documentType;

	/**
	 * 표시용 제목
	 */
	private String title;

	/**
	 * 상세 페이지 URL
	 */
	private String url;

	/**
	 * 상담 ID (documentType == "counsel"일 때)
	 */
	private Long counselId;

	/**
	 * 법령 ID (documentType == "lawArticle"일 때)
	 */
	private String lawId;

	/**
	 * 조문 키 (documentType == "lawArticle"일 때)
	 */
	private String articleKey;

	/**
	 * 예규·판례 ID (documentType == "yp"일 때)
	 */
	private Long ypId;
}
