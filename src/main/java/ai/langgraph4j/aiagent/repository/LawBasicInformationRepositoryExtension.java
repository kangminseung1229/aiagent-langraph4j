package ai.langgraph4j.aiagent.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ai.langgraph4j.aiagent.entity.law.LawBasicInformation;

/**
 * LawBasicInformationRepository의 QueryDSL Extension 인터페이스
 */
public interface LawBasicInformationRepositoryExtension {

	/**
	 * lawId별로 enforceDate가 최신인 법령 1개 조회
	 * enforceDate는 yyyyMMdd 형식의 문자열이므로 문자열 비교로 최신값 찾기
	 * 
	 * @param lawId 법령ID
	 * @return 최신 시행일자의 법령 (Optional)
	 */
	Optional<LawBasicInformation> findLatestByLawId(String lawId);

	/**
	 * 모든 lawId별로 최신 enforceDate의 법령 목록 조회
	 * 각 lawId마다 enforceDate가 최신인 법령 1개씩만 반환
	 * 
	 * @return lawId별 최신 법령 목록
	 */
	List<LawBasicInformation> findAllLatestByLawId();

	/**
	 * 모든 lawId별로 최신 enforceDate의 법령 목록을 페이징하여 조회
	 * 각 lawId마다 enforceDate가 최신인 법령 1개씩만 반환
	 * 
	 * @param pageable 페이징 정보
	 * @return lawId별 최신 법령 목록 (페이징)
	 */
	Page<LawBasicInformation> findAllLatestByLawId(Pageable pageable);

	/**
	 * 모든 lawId별로 최신 enforceDate의 법령 총 개수 조회
	 * 
	 * @return 법령 총 개수
	 */
	long countAllLatestByLawId();

	Set<String> findLawIdGroup();
}
