package ai.langgraph4j.aiagent.parser;

import lombok.Builder;
import lombok.Value;

/**
 * 예규/판례(yp) 파싱 결과.
 * - matched: yp 형식으로 인정 여부
 * - category: 대법원, 헌재, 조심, 국심, 감심/감독, 지법/고법, 연도한글숫자, 예규부서, 복합, 기타 등
 */
@Value
@Builder
public class YpParseResult {

    boolean matched;
    String category;

    public static YpParseResult noMatch() {
        return YpParseResult.builder().matched(false).category("").build();
    }

    public static YpParseResult of(String category) {
        return YpParseResult.builder().matched(true).category(category).build();
    }
}
