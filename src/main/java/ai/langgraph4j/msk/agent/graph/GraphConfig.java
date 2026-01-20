package ai.langgraph4j.msk.agent.graph;

import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * 그래프 설정 클래스
 * 
 * 참고: Phase 2에서는 간단한 서비스 레이어로 구현하며,
 * Phase 3에서 LangGraph4j의 완전한 StateGraph 통합을 진행합니다.
 * 
 * AgentGraph는 @Component로 이미 등록되어 있으므로
 * 별도의 Bean 등록이 필요하지 않습니다.
 */
@Slf4j
@Configuration
public class GraphConfig {

	// AgentGraph는 @Component로 이미 등록되어 있으므로
	// 여기서는 설정만 관리합니다.
	// Phase 3에서 LangGraph4j StateGraph 통합 시 사용 예정
}
