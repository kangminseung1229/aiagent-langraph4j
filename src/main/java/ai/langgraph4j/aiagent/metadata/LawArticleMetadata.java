package ai.langgraph4j.aiagent.metadata;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import ai.langgraph4j.aiagent.entity.law.Article;
import ai.langgraph4j.aiagent.entity.law.LawBasicInformation;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 법령 조문(LawArticle) 문서의 벡터 임베딩 메타데이터
 */
@Slf4j
@Data
@Builder
public class LawArticleMetadata implements DocumentMetadata {

	public static final String DOCUMENT_TYPE = "lawArticle";

	// 법령 정보
	private String lawId;
	private String lawKey;
	private String lawNameKorean;
	private String lawNameNickname;
	private String enforceDate;
	private String lawCategory;

	// 조문 정보
	private Long articleId;
	private String articleKey;
	private String articleCode;
	private String articleKoreanString;
	private String articleNumber;
	private String articleBranchNumber;
	private String articleTitle;
	private String articleEnforceDate;

	// 청크 정보
	private Integer chunkIndex;
	private Integer totalChunks;

	/**
	 * LawBasicInformation과 Article 엔티티로부터 LawArticleMetadata 생성
	 * 
	 * @param law     법령 기본 정보
	 * @param article 조문
	 * @return LawArticleMetadata
	 */
	public static LawArticleMetadata from(LawBasicInformation law, Article article) {
		LawArticleMetadataBuilder builder = LawArticleMetadata.builder()
				.lawId(law.getLawId())
				.lawKey(law.getLawKey())
				.enforceDate(law.getEnforceDate())
				.articleId(article.getId())
				.articleKey(article.getArticleKey())
				.articleCode(article.getArticleCode())
				.articleKoreanString(article.getKoreanString());

		// 법령 정보 (nullable 필드)
		if (law.getLawNameKorean() != null) {
			builder.lawNameKorean(law.getLawNameKorean());
		}
		if (law.getLawNameNickname() != null) {
			builder.lawNameNickname(law.getLawNameNickname());
		}
		if (law.getLawCategory() != null) {
			builder.lawCategory(law.getLawCategory());
		}

		// 조문 정보 (nullable 필드)
		if (article.getArticleNumber() != null) {
			builder.articleNumber(article.getArticleNumber());
		}
		if (article.getArticleBranchNumber() != null) {
			builder.articleBranchNumber(article.getArticleBranchNumber());
		}
		if (article.getArticleTitle() != null) {
			builder.articleTitle(article.getArticleTitle());
		}
		if (article.getArticleEnforceDate() != null) {
			builder.articleEnforceDate(article.getArticleEnforceDate());
		}

		return builder.build();
	}

	/**
	 * Map으로부터 LawArticleMetadata 생성 (검색 결과에서 사용)
	 * 
	 * @param metadataMap 메타데이터 Map
	 * @return LawArticleMetadata
	 */
	public static LawArticleMetadata fromMap(Map<String, Object> metadataMap) {
		LawArticleMetadataBuilder builder = LawArticleMetadata.builder()
				.lawId(extractString(metadataMap, "lawId"))
				.lawKey(extractString(metadataMap, "lawKey"))
				.lawNameKorean(extractString(metadataMap, "lawNameKorean"))
				.lawNameNickname(extractString(metadataMap, "lawNameNickname"))
				.enforceDate(extractString(metadataMap, "enforceDate"))
				.lawCategory(extractString(metadataMap, "lawCategory"))
				.articleId(extractLong(metadataMap, "articleId"))
				.articleKey(extractString(metadataMap, "articleKey"))
				.articleCode(extractString(metadataMap, "articleCode"))
				.articleKoreanString(extractString(metadataMap, "articleKoreanString"))
				.articleNumber(extractString(metadataMap, "articleNumber"))
				.articleBranchNumber(extractString(metadataMap, "articleBranchNumber"))
				.articleTitle(extractString(metadataMap, "articleTitle"))
				.articleEnforceDate(extractString(metadataMap, "articleEnforceDate"))
				.chunkIndex(extractInteger(metadataMap, "chunkIndex"))
				.totalChunks(extractInteger(metadataMap, "totalChunks"));

		return builder.build();
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();

		// 문서 타입
		map.put("documentType", DOCUMENT_TYPE);

		// 법령 정보
		if (lawId != null) {
			map.put("lawId", lawId);
		}
		if (lawKey != null) {
			map.put("lawKey", lawKey);
		}
		if (lawNameKorean != null) {
			map.put("lawNameKorean", lawNameKorean);
		}
		if (lawNameNickname != null) {
			map.put("lawNameNickname", lawNameNickname);
		}
		if (enforceDate != null) {
			map.put("enforceDate", enforceDate);
		}
		if (lawCategory != null) {
			map.put("lawCategory", lawCategory);
		}

		// 조문 정보
		if (articleId != null) {
			map.put("articleId", articleId);
		}
		if (articleKey != null) {
			map.put("articleKey", articleKey);
		}
		if (articleCode != null) {
			map.put("articleCode", articleCode);
		}
		if (articleKoreanString != null) {
			map.put("articleKoreanString", articleKoreanString);
		}
		if (articleNumber != null) {
			map.put("articleNumber", articleNumber);
		}
		if (articleBranchNumber != null) {
			map.put("articleBranchNumber", articleBranchNumber);
		}
		if (articleTitle != null) {
			map.put("articleTitle", articleTitle);
		}
		if (articleEnforceDate != null) {
			map.put("articleEnforceDate", articleEnforceDate);
		}

		// 청크 정보
		if (chunkIndex != null) {
			map.put("chunkIndex", chunkIndex);
		}
		if (totalChunks != null) {
			map.put("totalChunks", totalChunks);
		}

		return map;
	}

	@Override
	public String getDocumentType() {
		return DOCUMENT_TYPE;
	}

	// 유틸리티 메서드들
	private static Long extractLong(Map<String, Object> map, String key) {
		Object value = map.get(key);
		if (value == null) {
			return null;
		}
		if (value instanceof Long) {
			return (Long) value;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		if (value instanceof String) {
			try {
				return Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private static String extractString(Map<String, Object> map, String key) {
		Object value = map.get(key);
		return value != null ? value.toString() : null;
	}

	private static Integer extractInteger(Map<String, Object> map, String key) {
		Object value = map.get(key);
		if (value == null) {
			return null;
		}
		if (value instanceof Integer) {
			return (Integer) value;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * 법령 조문 데이터로부터 임베딩할 텍스트 생성
	 * HTML 태그는 제거하고 순수 텍스트만 추출합니다.
	 * 
	 * @param law     법령 기본 정보
	 * @param article 조문
	 * @return 임베딩할 텍스트 (HTML 태그 제거됨)
	 */
	public static String buildArticleText(LawBasicInformation law, Article article) {
		StringBuilder text = new StringBuilder();

		// 법령명 추가
		if (law.getLawNameKorean() != null && !law.getLawNameKorean().trim().isEmpty()) {
			text.append("법령명: ").append(law.getLawNameKorean()).append("\n");
		}

		// 조문 정보 추가
		text.append("조문: ").append(article.getKoreanString()).append("\n");

		// 조문제목 추가
		if (article.getArticleTitle() != null && !article.getArticleTitle().trim().isEmpty()) {
			String cleanTitle = removeHtmlTags(article.getArticleTitle());
			if (!cleanTitle.trim().isEmpty()) {
				text.append("제목: ").append(cleanTitle).append("\n");
			}
		}

		// 조문내용 추가 (articleOriginalContent 우선, 없으면 articleLinkContent)
		String articleContent = article.getArticleOriginalContent();
		if (articleContent == null || articleContent.trim().isEmpty()) {
			articleContent = article.getArticleLinkContent();
		}

		if (articleContent != null && !articleContent.trim().isEmpty()) {
			String cleanContent = removeHtmlTags(articleContent);
			if (!cleanContent.trim().isEmpty()) {
				text.append("내용: ").append(cleanContent).append("\n");
			}
		}

		// 조문참고자료 추가 (있는 경우)
		if (article.getArticleReference() != null && !article.getArticleReference().trim().isEmpty()) {
			String cleanReference = removeHtmlTags(article.getArticleReference());
			if (!cleanReference.trim().isEmpty()) {
				text.append("참고자료: ").append(cleanReference).append("\n");
			}
		}

		return text.toString().trim();
	}

	/**
	 * HTML 태그를 제거하고 순수 텍스트만 추출
	 * JSoup을 사용하여 HTML을 파싱하고 모든 태그를 제거합니다.
	 * 
	 * @param html HTML이 포함된 텍스트
	 * @return HTML 태그가 제거된 순수 텍스트
	 */
	private static String removeHtmlTags(String html) {
		if (html == null || html.trim().isEmpty()) {
			return "";
		}

		try {
			// JSoup을 사용하여 HTML 태그 제거
			// Safelist.none()은 모든 HTML 태그를 제거하고 텍스트만 추출
			String cleanText = Jsoup.clean(html, Safelist.none());

			// HTML 엔티티 디코딩 (&nbsp; -> 공백 등)
			cleanText = Jsoup.parse(cleanText).text();

			// 연속된 공백을 하나로 정리
			cleanText = cleanText.replaceAll("\\s+", " ").trim();

			return cleanText;
		} catch (Exception e) {
			log.warn("HTML 태그 제거 중 오류 발생, 원본 텍스트 반환: {}", e.getMessage());
			// 오류 발생 시 원본 반환 (최소한의 처리)
			return html.replaceAll("<[^>]+>", "").trim();
		}
	}
}
