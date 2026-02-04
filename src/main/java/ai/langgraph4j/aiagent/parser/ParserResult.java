package ai.langgraph4j.aiagent.parser;

import java.util.List;

import lombok.Data;

@Data
public class ParserResult {

    private String api_link_with_text; // hyper link text
    private List<ParserResultLaw> laws; // law list

    // private List<String> degt_laws; // TODO: 위임법령
}
