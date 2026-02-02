package ai.langgraph4j.aiagent.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import ai.langgraph4j.aiagent.controller.dto.RelatedReference;

/**
 * 검색 결과로부터 만든 관련 자료 참조 목록을 요청(세션) 단위로 임시 보관합니다.
 * SearchTool은 Reactor 스레드(oundedElastic 등)에서, ChatV2Service는 runAsync
 * 스레드(onPool-worker)에서
 * 실행되므로 ThreadLocal이 아닌 sessionId 키의 Map으로 스레드 간 공유합니다.
 */
@Component
public class RelatedReferencesHolder {

	/** sessionId → 해당 턴에서 검색 도구가 저장한 관련 자료 (스트리밍 완료 후 제거) */
	private final Map<String, List<RelatedReference>> refsBySession = new ConcurrentHashMap<>();

	/** 현재 처리 중인 세션 ID (검색 도구가 refs를 넣을 때 사용). 동시 요청 시 락으로 보호 */
	private volatile String currentSessionId;

	/** setCurrentSession ~ takeRefs 구간을 한 요청만 수행하도록 보호 (스트리밍 시 호출 측에서 사용) */
	private final Object lock = new Object();

	public Object getLock() {
		return lock;
	}

	/**
	 * 이 턴의 스트리밍 시작 시 호출. 이후 검색 도구가 호출되면 이 sessionId로 refs를 저장합니다.
	 */
	public void setCurrentSession(String sessionId) {
		synchronized (lock) {
			this.currentSessionId = sessionId;
			this.refsBySession.put(sessionId, new ArrayList<>());
		}
	}

	/**
	 * 해당 세션에 보관된 관련 자료를 꺼내고, 저장소에서 제거합니다.
	 */
	public List<RelatedReference> takeRefs(String sessionId) {
		synchronized (lock) {
			this.currentSessionId = null;
			List<RelatedReference> refs = refsBySession.remove(sessionId);
			return refs != null ? refs : Collections.emptyList();
		}
	}

	/**
	 * 현재 스레드/요청에 보관된 관련 자료 목록을 반환합니다. (비스트리밍 등 기존 호출 호환용)
	 */
	public List<RelatedReference> getRefs() {
		return Collections.emptyList();
	}

	/**
	 * 검색 도구에서 호출. 현재 처리 중인 세션에 관련 자료를 저장합니다.
	 * 다른 스레드(oundedElastic 등)에서 호출되므로 락 없이 volatile currentSessionId만 읽어 데드락을 피합니다.
	 */
	public void setRefs(List<RelatedReference> refs) {
		if (refs == null || refs.isEmpty()) {
			return;
		}
		String sessionId = this.currentSessionId;
		if (sessionId != null) {
			refsBySession.put(sessionId, new ArrayList<>(refs));
		}
	}

	/**
	 * 현재 세션 보관 목록만 비웁니다. (요청 처리 후 호출)
	 */
	public void clear() {
		synchronized (lock) {
			this.currentSessionId = null;
			// refsBySession은 takeRefs 시 remove 되므로 여기서는 currentSessionId만 초기화
		}
	}
}
