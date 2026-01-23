package ai.langgraph4j.aiagent.metadata;

import java.util.Map;

/**
 * 벡터 스토어 문서 메타데이터의 공통 인터페이스
 * 모든 메타데이터 클래스는 이 인터페이스를 구현하여 Map으로 변환할 수 있어야 합니다.
 */
public interface DocumentMetadata {

	/**
	 * 메타데이터를 Map<String, Object> 형식으로 변환
	 * Vector Store에 저장하기 위해 사용됩니다.
	 * 
	 * @return 메타데이터 Map
	 */
	Map<String, Object> toMap();

	/**
	 * 문서 타입 반환
	 * 
	 * @return 문서 타입 (예: "counsel", "lawArticle")
	 */
	String getDocumentType();
}
