package ai.langgraph4j.aiagent.metadata;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.langgraph4j.aiagent.entity.counsel.Counsel;
import ai.langgraph4j.aiagent.entity.law.LawArticleCode;
import lombok.Builder;
import lombok.Data;

/**
 * 상담(Counsel) 문서의 벡터 임베딩 메타데이터
 */
@Data
@Builder
public class ConsultationMetadata implements DocumentMetadata {

	public static final String DOCUMENT_TYPE = "counsel";

	// 기본 정보
	private Long counselId;
	private String title;
	private String fieldLarge;
	private LocalDateTime createdAt;

	// 연관 법령 조문 정보
	private List<String> lawArticleKeys; // 기존 호환성 필드
	private List<String> lawArticles; // 한국어 형식
	private List<String> lawIds; // 법령 ID 리스트
	private List<String> articleKeys; // 조문 키 리스트
	private List<String> lawArticlePairs; // "lawId:articleKey" 형식의 쌍

	// 청크 정보
	private Integer chunkIndex;
	private Integer totalChunks;

	/**
	 * Counsel 엔티티로부터 ConsultationMetadata 생성
	 * 
	 * @param consultation 상담 엔티티
	 * @return ConsultationMetadata
	 */
	public static ConsultationMetadata from(Counsel consultation) {
		ConsultationMetadataBuilder builder = ConsultationMetadata.builder()
				.counselId(consultation.getId())
				.title(consultation.getCounselTitle())
				.createdAt(consultation.getCounselAt());

		// fieldLarge 처리
		if (consultation.getCounselFieldLarge() != null) {
			builder.fieldLarge(consultation.getCounselFieldLarge().toString());
		}

		// 연관 법령 조문 정보 처리
		if (consultation.getLawArticleCodes() != null && !consultation.getLawArticleCodes().isEmpty()) {
			List<LawArticleCode> lawArticleCodes = consultation.getLawArticleCodes();

			// articleKey 리스트 (기존 호환성 유지)
			List<String> lawArticleKeys = lawArticleCodes.stream()
					.map(LawArticleCode::getArticleKey)
					.toList();
			builder.lawArticleKeys(lawArticleKeys);

			// 한국어 형식으로도 저장 (검색 시 활용)
			List<String> lawArticleFormats = lawArticleCodes.stream()
					.map(lawCode -> LawArticleCode.convertToKoreanFormat(lawCode.getArticleKey()))
					.toList();
			builder.lawArticles(lawArticleFormats);

			// lawId 리스트 (중복 제거)
			List<String> lawIds = lawArticleCodes.stream()
					.map(LawArticleCode::getLawId)
					.filter(lawId -> lawId != null && !lawId.trim().isEmpty())
					.distinct()
					.toList();
			if (!lawIds.isEmpty()) {
				builder.lawIds(lawIds);
			}

			// articleKey 리스트 (중복 제거)
			List<String> articleKeys = lawArticleCodes.stream()
					.map(LawArticleCode::getArticleKey)
					.filter(articleKey -> articleKey != null && !articleKey.trim().isEmpty())
					.distinct()
					.toList();
			if (!articleKeys.isEmpty()) {
				builder.articleKeys(articleKeys);
			}

			// lawId-articleKey 쌍 리스트 (정확한 매칭을 위해)
			List<String> lawArticlePairs = lawArticleCodes.stream()
					.filter(lawCode -> lawCode.getLawId() != null && !lawCode.getLawId().trim().isEmpty()
							&& lawCode.getArticleKey() != null && !lawCode.getArticleKey().trim().isEmpty())
					.map(lawCode -> lawCode.getLawId() + ":" + lawCode.getArticleKey())
					.distinct()
					.toList();
			if (!lawArticlePairs.isEmpty()) {
				builder.lawArticlePairs(lawArticlePairs);
			}
		}

		return builder.build();
	}

	/**
	 * Map으로부터 ConsultationMetadata 생성 (검색 결과에서 사용)
	 * 
	 * @param metadataMap 메타데이터 Map
	 * @return ConsultationMetadata
	 */
	@SuppressWarnings("unchecked")
	public static ConsultationMetadata fromMap(Map<String, Object> metadataMap) {
		ConsultationMetadataBuilder builder = ConsultationMetadata.builder();

		// 기본 정보
		if (metadataMap.get("counselId") != null) {
			builder.counselId(extractLong(metadataMap, "counselId"));
		}
		builder.title(extractString(metadataMap, "title"));
		builder.fieldLarge(extractString(metadataMap, "fieldLarge"));
		if (metadataMap.get("createdAt") != null) {
			builder.createdAt(extractLocalDateTime(metadataMap, "createdAt"));
		}

		// 연관 법령 조문 정보
		builder.lawArticleKeys(extractStringList(metadataMap, "lawArticleKeys"));
		builder.lawArticles(extractStringList(metadataMap, "lawArticles"));
		builder.lawIds(extractStringList(metadataMap, "lawIds"));
		builder.articleKeys(extractStringList(metadataMap, "articleKeys"));
		builder.lawArticlePairs(extractStringList(metadataMap, "lawArticlePairs"));

		// 청크 정보
		builder.chunkIndex(extractInteger(metadataMap, "chunkIndex"));
		builder.totalChunks(extractInteger(metadataMap, "totalChunks"));

		return builder.build();
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();

		// 문서 타입
		map.put("documentType", DOCUMENT_TYPE);

		// 기본 정보
		if (counselId != null) {
			map.put("counselId", counselId);
		}
		if (title != null) {
			map.put("title", title);
		}
		if (fieldLarge != null) {
			map.put("fieldLarge", fieldLarge);
		}
		if (createdAt != null) {
			map.put("createdAt", createdAt.toString());
		}

		// 연관 법령 조문 정보
		if (lawArticleKeys != null && !lawArticleKeys.isEmpty()) {
			map.put("lawArticleKeys", lawArticleKeys);
		}
		if (lawArticles != null && !lawArticles.isEmpty()) {
			map.put("lawArticles", lawArticles);
		}
		if (lawIds != null && !lawIds.isEmpty()) {
			map.put("lawIds", lawIds);
		}
		if (articleKeys != null && !articleKeys.isEmpty()) {
			map.put("articleKeys", articleKeys);
		}
		if (lawArticlePairs != null && !lawArticlePairs.isEmpty()) {
			map.put("lawArticlePairs", lawArticlePairs);
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

	@SuppressWarnings("unchecked")
	private static List<String> extractStringList(Map<String, Object> map, String key) {
		Object value = map.get(key);
		if (value == null) {
			return new ArrayList<>();
		}
		if (value instanceof List) {
			return (List<String>) value;
		}
		return new ArrayList<>();
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

	private static LocalDateTime extractLocalDateTime(Map<String, Object> map, String key) {
		Object value = map.get(key);
		if (value == null) {
			return null;
		}
		if (value instanceof LocalDateTime) {
			return (LocalDateTime) value;
		}
		if (value instanceof String) {
			try {
				return LocalDateTime.parse((String) value);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
