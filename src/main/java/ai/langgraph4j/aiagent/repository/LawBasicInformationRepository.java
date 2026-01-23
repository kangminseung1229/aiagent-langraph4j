package ai.langgraph4j.aiagent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ai.langgraph4j.aiagent.entity.law.LawBasicInformation;

/**
 * 법령 기본 정보(LawBasicInformation) Repository
 * QueryDSL Extension을 통해 복잡한 쿼리를 처리합니다.
 */
public interface LawBasicInformationRepository extends JpaRepository<LawBasicInformation, Long>,
		LawBasicInformationRepositoryExtension {

	/**
	 * lawId로 법령 조회
	 * 
	 * @param lawId 법령ID
	 * @return 법령 목록
	 */
	List<LawBasicInformation> findByLawId(String lawId);

	/**
	 * lawId로 법령 존재 여부 확인
	 * 
	 * @param lawId 법령ID
	 * @return 존재 여부
	 */
	boolean existsByLawId(String lawId);
}
