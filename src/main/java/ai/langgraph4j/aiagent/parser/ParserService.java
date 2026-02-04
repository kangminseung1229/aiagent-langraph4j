package ai.langgraph4j.aiagent.parser;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * ParserService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParserService {

    @Resource(name = "intellicon")
    private final WebClient parserWebClient;

    public ParserReturnType executeParser(ParserParam parserParam) {
        try {
            return parserWebClient.post()
                    .uri("/parser/law_link")
                    .bodyValue(parserParam)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, response -> handleServerError(response, parserParam))
                    .bodyToMono(ParserReturnType.class)
                    .blockOptional()
                    .orElse(new ParserReturnType());
        } catch (WebClientResponseException e) {
            // WebClientResponseException은 5xx 외의 에러를 처리합니다.
            // 여기에서 필요한 예외 처리를 수행하거나, 적절한 방식으로 로깅합니다.
            // 예: log.error("WebClient request failed with status code: {}",
            // e.getStatusCode());
            return new ParserReturnType(); // 또는 예외를 다시 던지거나, 기본값 등을 반환할 수 있습니다.
        } catch (Exception ex) {
            return new ParserReturnType();
        }
    }

    private Mono<? extends Throwable> handleServerError(ClientResponse response, ParserParam parserParam) {

        log.info("[PARSER 파라미터] :: {}", parserParam);
        log.info("[PARSER response] :: {}", response.statusCode());

        return Mono.error(new Exception(response.toString()));
    }

    // --- 예규/판례(yp) 파서 (단일 소스 정규식 → 검증/찾기 공유,
    // docs/taxnet4_public_yp_regex_analysis.md 기준) ---

    private static final String YP_COMPOUND_DELIM = "·";
    private static final String YP_DETAIL_URL = "https://beta.taxnet.co.kr/taxmanage/yp/yp-detail?documentNumber=";
    private static final String YP_CATEGORY_JIBEOP_GOBEOP = "지법/고법";

    /**
     * yp 정규식 단일 소스. core만 정의하고, 검증(^core.*) / 찾기(core) 패턴은 여기서 파생.
     * 순서: 구체적 → 일반 (먼저 매칭된 것이 적용됨).
     */
    private static final YpPatternDef[] YP_PATTERNS = {
            new YpPatternDef("대법원\\d{4}[가-힣]{1,3}\\d+", "대법원"), // 대법원1976누25
            new YpPatternDef(":?헌재\\d{2,4}[가-힣]+\\d+", "헌재"), // 헌재98헌마425, :헌재2014헌바66
            new YpPatternDef("조심\\s*\\d{4}[가-힣]+\\d+", "조심"), // 조심2006서2797, 조심 2025서3770, 조심2018전1149
            new YpPatternDef("국심\\d{4}[가-힣]+\\d+", "국심"), // 국심1989광0021
            new YpPatternDef("(?:감심|감독)\\d+-\\d+", "감심/감독"), // 감심1994-10, 감독01254-117
            new YpPatternDef("고법\\d{2}[가-힣]+\\d+", YP_CATEGORY_JIBEOP_GOBEOP), // 고법79구30
            new YpPatternDef("[가-힣○]{2,}행법\\d{2,4}[가-힣]+\\d+", YP_CATEGORY_JIBEOP_GOBEOP), // 서울행법2018구합66272
            new YpPatternDef("[가-힣○]{2,}고법\\d{2,4}[가-힣]+\\d+", YP_CATEGORY_JIBEOP_GOBEOP), // 서울고법2019누54384
            new YpPatternDef("[가-힣○]{2,}지법\\d{2,4}[가-힣]+\\d+", YP_CATEGORY_JIBEOP_GOBEOP), // ○○지법2008구합4207,
                                                                                           // 대구지법2019구합21278
            new YpPatternDef("[가-힣]+(?:지법|지원|법원)\\d{4}[가-힣]+\\d+", YP_CATEGORY_JIBEOP_GOBEOP), // 부산지법동부지원2012가단203401
            new YpPatternDef("(?:19|20)\\d{2}[가-힣]+\\d+(?:-\\d+)?", "연도한글숫자"), // 2000부노1, 2000부노129-1
            new YpPatternDef("\\d{2}[가-힣]+\\d+", "연도한글숫자"), // 95부노139
            new YpPatternDef("[가-힣0-9A-Za-z]+-\\d+", "예규/부서"), // 기준법규법인-101
    };

    /** 검증용: 전체 문자열이 해당 형식인지 확인 (^core.*) */
    private static final Pattern[] YP_VALIDATION_PATTERNS = buildValidationPatterns();
    /** 본문에서 yp 후보 찾기: core들을 | 로 묶은 하나의 패턴 */
    private static final Pattern YP_FIND_IN_TEXT = buildFindPattern();

    private static Pattern[] buildValidationPatterns() {
        Pattern[] arr = new Pattern[YP_PATTERNS.length];
        for (int i = 0; i < YP_PATTERNS.length; i++) {
            arr[i] = Pattern.compile("^" + YP_PATTERNS[i].core + ".*");
        }
        return arr;
    }

    private static Pattern buildFindPattern() {
        String alternation = Arrays.stream(YP_PATTERNS)
                .map(p -> "(?:" + p.core + ")")
                .reduce((a, b) -> a + "|" + b)
                .orElse("(?!)");
        return Pattern.compile(alternation);
    }

    private record YpPatternDef(String core, String category) {
    }

    /**
     * 입력 문자열이 예규/판례(yp) 형식인지 순차 정규식으로 판별하고, 매칭 시 유형을 반환한다.
     * - 빈 값, 단순 숫자, 영문만, 숫자 없는 접두어만 등은 비yp로 처리.
     * - 복합(·로 구분)은 분리 후 하나라도 yp면 '복합'으로 인정.
     *
     * @param raw CSV 셀 값 등 원본 문자열 (앞뒤 공백, 앞의 ':', 따옴표 허용)
     * @return 매칭 여부와 유형(category)
     */
    public YpParseResult parseYp(String raw) {
        if (raw == null) {
            return YpParseResult.noMatch();
        }
        String s = normalizeYpInput(raw);
        if (s.isBlank()) {
            return YpParseResult.noMatch();
        }
        // 비yp 후보: 단일 숫자, 영문만, 숫자 전혀 없음
        if (s.matches("^\\d$") || s.matches("^[A-Za-z]+$") || !s.matches(".*\\d+.*")) {
            return YpParseResult.noMatch();
        }
        // 복합(·): 분리 후 하나라도 yp면 복합
        if (s.contains(YP_COMPOUND_DELIM)) {
            boolean any = Arrays.stream(s.split(Pattern.quote(YP_COMPOUND_DELIM)))
                    .map(String::trim)
                    .filter(seg -> !seg.isEmpty())
                    .anyMatch(seg -> parseYp(seg).isMatched());
            return any ? YpParseResult.of("복합") : YpParseResult.noMatch();
        }
        String category = matchYpCategory(s);
        return category != null ? YpParseResult.of(category) : YpParseResult.noMatch();
    }

    /** 순차 정규식으로 단일 문자열의 yp 유형 반환. 비매칭 시 null (검증용 패턴 = YP_PATTERNS 기반) */
    private static String matchYpCategory(String s) {
        for (int i = 0; i < YP_VALIDATION_PATTERNS.length; i++) {
            if (YP_VALIDATION_PATTERNS[i].matcher(s).matches()) {
                return YP_PATTERNS[i].category;
            }
        }
        return null;
    }

    /** yp 입력 정규화: trim, 앞 ':' 제거, 감싼 따옴표 제거 */
    private static String normalizeYpInput(String raw) {
        if (raw == null)
            return "";
        String s = raw.trim();
        if (s.startsWith(":"))
            s = s.substring(1).trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    /** 편의: yp 형식 여부만 필요할 때 */
    public boolean isYpFormat(String raw) {
        return parseYp(raw).isMatched();
    }

    /**
     * 텍스트 안에서 예규/판례(yp) 형식 문자열을 찾아, 택스넷 상세 페이지 링크 a 태그로 감싼 HTML을 반환한다.
     * 예: "대법원1976누25" → &lt;a
     * href="https://www.taxnet.co.kr/taxmanage/yp/yp-detail?documentNumber=대법원1976누25"&gt;대법원1976누25&lt;/a&gt;
     *
     * @param text 원본 텍스트 (null이면 빈 문자열 취급)
     * @return yp가 링크로 치환된 문자열
     */
    public String wrapYpWithLink(String text) {
        if (text == null) {
            return "";
        }
        Matcher m = YP_FIND_IN_TEXT.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String yp = m.group(0);
            if (parseYp(yp).isMatched()) {
                // 한글이 href에 그대로 보이도록 인코딩하지 않음. 브라우저가 전송 시 인코딩함.
                String href = YP_DETAIL_URL + escapeHtmlForHref(yp);
                String linkText = escapeHtml(yp);
                String replacement = "<a href=\"" + href + "\">" + linkText + "</a>";
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String escapeHtml(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** href 속성 값 안에서만 이스케이프 (한글 유지, 따옴표·& 등만 처리) */
    private static String escapeHtmlForHref(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("\"", "&quot;");
    }
}