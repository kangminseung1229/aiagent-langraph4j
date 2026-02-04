package ai.langgraph4j.aiagent.parser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParserParam {

    private String query; // 조문 내용
    private String base_dy; // 해당 법령의 시행일자
    private String law_id; // 법령 아이디

    @Builder.Default
    private String law_type = "L"; // 법 타입 기본 L

    private String prvs_no; // 조문 번호. ???조
    private String target_blank_yn; // 링크 타겟 여부
}
