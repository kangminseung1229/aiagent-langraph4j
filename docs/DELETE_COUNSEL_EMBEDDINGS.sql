-- ============================================
-- Counsel 임베딩 데이터 삭제 쿼리
-- ============================================
-- 
-- 이 스크립트는 spring_ai_vector_store 테이블에서
-- counsel(상담) 관련 임베딩 데이터를 삭제하는 쿼리입니다.
--
-- 사용 전 확인사항:
-- 1. PostgreSQL에 연결되어 있어야 합니다.
-- 2. spring_ai_vector_store 테이블이 존재해야 합니다.
-- 3. 삭제 전에 백업을 권장합니다.
-- ============================================

-- ============================================
-- 1. 삭제 전 데이터 확인 (실행 권장)
-- ============================================

-- counsel 임베딩 데이터 개수 확인
SELECT COUNT(*) as counsel_embedding_count
FROM spring_ai_vector_store
WHERE metadata->>'documentType' = 'counsel';

-- counsel 임베딩 데이터 샘플 조회 (최대 10개)
SELECT 
    id,
    LEFT(content, 100) as content_preview,
    metadata->>'counselId' as counsel_id,
    metadata->>'title' as title,
    metadata->>'documentType' as document_type
FROM spring_ai_vector_store
WHERE metadata->>'documentType' = 'counsel'
LIMIT 10;

-- counselId별 청크 개수 확인
SELECT 
    metadata->>'counselId' as counsel_id,
    COUNT(*) as chunk_count
FROM spring_ai_vector_store
WHERE metadata->>'documentType' = 'counsel'
GROUP BY metadata->>'counselId'
ORDER BY chunk_count DESC
LIMIT 20;

-- ============================================
-- 2. 전체 counsel 임베딩 삭제
-- ============================================
-- ⚠️ 주의: 모든 counsel 임베딩 데이터가 삭제됩니다!
-- 실행 전에 위의 확인 쿼리로 데이터를 확인하세요.

-- DELETE FROM spring_ai_vector_store
-- WHERE metadata->>'documentType' = 'counsel';

-- 삭제된 행 수 확인
-- SELECT ROW_COUNT(); -- PostgreSQL에서는 DELETE 후 자동으로 반환됨

-- ============================================
-- 3. 특정 counselId의 임베딩만 삭제
-- ============================================
-- 특정 상담 ID의 모든 청크를 삭제합니다.
-- 예: counselId가 123인 경우

-- DELETE FROM spring_ai_vector_store
-- WHERE metadata->>'counselId' = '123';

-- ============================================
-- 4. 여러 counselId의 임베딩 삭제
-- ============================================
-- 여러 상담 ID를 한 번에 삭제합니다.

-- DELETE FROM spring_ai_vector_store
-- WHERE metadata->>'documentType' = 'counsel'
--   AND metadata->>'counselId' IN ('123', '456', '789');

-- ============================================
-- 5. 특정 날짜 이전의 counsel 임베딩 삭제
-- ============================================
-- createdAt 필드를 기준으로 삭제합니다.
-- 예: 2024-01-01 이전 데이터 삭제

-- DELETE FROM spring_ai_vector_store
-- WHERE metadata->>'documentType' = 'counsel'
--   AND (metadata->>'createdAt')::timestamp < '2024-01-01'::timestamp;

-- ============================================
-- 6. 삭제 후 확인
-- ============================================

-- 남은 counsel 임베딩 개수 확인
-- SELECT COUNT(*) as remaining_counsel_count
-- FROM spring_ai_vector_store
-- WHERE metadata->>'documentType' = 'counsel';

-- 전체 벡터 스토어 데이터 개수 확인
-- SELECT COUNT(*) as total_vector_count
-- FROM spring_ai_vector_store;

-- ============================================
-- 7. 백업 후 삭제 (권장)
-- ============================================
-- 삭제 전에 백업 테이블을 만드는 것을 권장합니다.

-- 백업 테이블 생성
-- CREATE TABLE spring_ai_vector_store_backup_counsel AS
-- SELECT * FROM spring_ai_vector_store
-- WHERE metadata->>'documentType' = 'counsel';

-- 백업 확인
-- SELECT COUNT(*) FROM spring_ai_vector_store_backup_counsel;

-- 백업에서 복원 (필요한 경우)
-- INSERT INTO spring_ai_vector_store
-- SELECT * FROM spring_ai_vector_store_backup_counsel;

-- ============================================
-- 사용 예시
-- ============================================
-- 
-- 1. 전체 counsel 임베딩 삭제:
--    DELETE FROM spring_ai_vector_store
--    WHERE metadata->>'documentType' = 'counsel';
--
-- 2. 특정 counselId 삭제:
--    DELETE FROM spring_ai_vector_store
--    WHERE metadata->>'counselId' = '123';
--
-- 3. 백업 후 삭제:
--    CREATE TABLE spring_ai_vector_store_backup_counsel AS
--    SELECT * FROM spring_ai_vector_store
--    WHERE metadata->>'documentType' = 'counsel';
--    
--    DELETE FROM spring_ai_vector_store
--    WHERE metadata->>'documentType' = 'counsel';
-- ============================================
