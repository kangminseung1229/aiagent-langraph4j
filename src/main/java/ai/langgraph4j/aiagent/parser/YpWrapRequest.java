package ai.langgraph4j.aiagent.parser;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class YpWrapRequest {

    @JsonProperty("text")
    private String text;
}