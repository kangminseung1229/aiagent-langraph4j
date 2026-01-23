package ai.langgraph4j.aiagent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.langgraph4j.aiagent.service.ConsultationEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 임베딩 관리 컨트롤러
 * 상담 데이터를 임베딩하여 Vector Store에 저장하는 API를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

	private final ConsultationEmbeddingService embeddingService;

	/**
	 * 모든 상담 데이터 임베딩
	 * 
	 * @return 처리된 상담 수
	 */
	@PostMapping("/all")
	public ResponseEntity<EmbeddingResponse> embedAll() {
		log.info("전체 상담 데이터 임베딩 요청");

		int count = embeddingService.embedAllConsultations();

		return ResponseEntity.ok(new EmbeddingResponse(
				"전체 상담 데이터 임베딩 완료",
				count));
	}

	/**
	 * 특정 상담 ID 임베딩
	 * 
	 * @param counselId 상담 ID
	 * @return 응답
	 */
	@PostMapping("/{counselId}")
	public ResponseEntity<EmbeddingResponse> embedOne(@PathVariable Long counselId) {
		log.info("상담 ID {} 임베딩 요청", counselId);

		embeddingService.embedConsultation(counselId);

		return ResponseEntity.ok(new EmbeddingResponse(
				"상담 ID " + counselId + " 임베딩 완료",
				1));
	}

	/**
	 * 임베딩 응답 DTO
	 */
	public record EmbeddingResponse(String message, int processedCount) {
	}
}
