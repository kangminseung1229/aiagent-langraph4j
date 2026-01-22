package ai.langgraph4j.aiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class AiagentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiagentApplication.class, args);
	}
}
