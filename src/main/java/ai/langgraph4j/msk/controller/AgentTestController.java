package ai.langgraph4j.msk.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ai.langgraph4j.msk.agent.graph.AgentGraph;
import ai.langgraph4j.msk.agent.nodes.ResponseNode;
import ai.langgraph4j.msk.agent.state.AgentState;
import ai.langgraph4j.msk.controller.dto.AgentRequest;
import ai.langgraph4j.msk.controller.dto.AgentResponse;
import ai.langgraph4j.msk.controller.dto.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 에이전트 테스트 컨트롤러
 * Phase 2 테스트를 위한 간단한 REST API 엔드포인트
 */
@Slf4j
@RestController
@RequestMapping("/api/test/agent")
@RequiredArgsConstructor
public class AgentTestController {

	private final AgentGraph agentGraph;
	private final ResponseNode responseNode;

	/**
	 * 에이전트 실행 테스트
	 * 
	 * @param request 에이전트 실행 요청
	 * @return 에이전트 응답
	 */
	@PostMapping("/invoke")
	public ResponseEntity<?> invoke(@Valid @RequestBody AgentRequest request) {
		log.info("AgentTestController: 에이전트 실행 요청 - {}", request.getMessage());

		long startTime = System.currentTimeMillis();

		try {
			// 초기 상태 생성
			AgentState initialState = new AgentState();
			initialState.setSessionId(request.getSessionId());

			// 옵션 설정 (있는 경우)
			if (request.getOptions() != null) {
				// 옵션은 나중에 사용 가능
			}

			// 그래프 실행
			AgentState finalState = agentGraph.execute(initialState, request.getMessage());

			// 에러가 있는 경우
			if (finalState.getError() != null && !finalState.getError().isEmpty()) {
				ErrorResponse errorResponse = ErrorResponse.builder()
						.errorCode("AGENT_ERROR")
						.message(finalState.getError())
						.build();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
			}

			// 응답 생성
			AgentResponse response = responseNode.process(finalState, startTime);

			log.info("AgentTestController: 에이전트 실행 완료 - 실행 시간: {}초", response.getExecutionTime());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("AgentTestController: 에이전트 실행 중 오류 발생", e);
			ErrorResponse errorResponse = ErrorResponse.builder()
					.errorCode("INTERNAL_ERROR")
					.message("에이전트 실행 중 오류가 발생했습니다: " + e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * 간단한 헬스 체크
	 */
	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("Agent Test Controller is running");
	}

	/**
	 * 간단한 테스트 (GET 요청)
	 */
	@GetMapping("/test")
	public ResponseEntity<AgentResponse> test(@RequestParam(defaultValue = "안녕하세요") String message) {
		log.info("AgentTestController: 간단한 테스트 요청 - {}", message);

		AgentRequest request = AgentRequest.builder()
				.message(message)
				.build();

		ResponseEntity<?> response = invoke(request);

		if (response.getBody() instanceof AgentResponse) {
			return ResponseEntity.ok((AgentResponse) response.getBody());
		} else {
			// 에러 응답인 경우
			return ResponseEntity.status(response.getStatusCode())
					.body(AgentResponse.builder()
							.response("테스트 실패")
							.build());
		}
	}
}
