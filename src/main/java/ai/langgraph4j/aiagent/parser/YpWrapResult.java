package ai.langgraph4j.aiagent.parser;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class YpWrapResult {

    @JsonProperty("html")
    String html;
}
