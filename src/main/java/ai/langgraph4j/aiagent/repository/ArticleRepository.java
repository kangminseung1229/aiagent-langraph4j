package ai.langgraph4j.aiagent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ai.langgraph4j.aiagent.entity.law.Article;

/**
 * 조문(Article) Repository
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

	/**
	 * LawBasicInformation ID로 조문 목록 조회
	 * 
	 * @param lawBasicInformationId 법령 기본 정보 ID
	 * @return 조문 목록
	 */
	List<Article> findByLawBasicInformationId(Long lawBasicInformationId);

	/**
	 * LawBasicInformation ID로 조문 목록 조회 (삭제되지 않은 조문만)
	 * articleTitle이 비어있지 않은 조문만 조회
	 * 
	 * @param lawBasicInformationId 법령 기본 정보 ID
	 * @return 조문 목록 (삭제되지 않은 조문만)
	 */
	@Query("SELECT a FROM Article a " +
			"WHERE a.lawBasicInformation.id = :lawBasicInformationId " +
			"AND a.articleTitle IS NOT NULL " +
			"AND a.articleTitle != '' " +
			"ORDER BY a.articleNumber, a.articleBranchNumber")
	List<Article> findActiveArticlesByLawBasicInformationId(@Param("lawBasicInformationId") Long lawBasicInformationId);

	/**
	 * articleKey로 조문 조회
	 * 
	 * @param articleKey 조문키
	 * @return 조문 (Optional)
	 */
	@Query("SELECT a FROM Article a WHERE a.articleKey = :articleKey")
	List<Article> findByArticleKey(@Param("articleKey") String articleKey);
}
