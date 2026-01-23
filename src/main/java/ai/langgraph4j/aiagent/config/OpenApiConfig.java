package ai.langgraph4j.aiagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Swagger (SpringDoc OpenAPI) 설정 클래스
 * API 문서화를 위한 OpenAPI 설정을 제공합니다.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("AI Agent API")
						.version("1.0.0")
						.description("Spring AI + LangGraph4j 기반 AI 에이전트 API 문서")
						.contact(new Contact()
								.name("AI Agent Team")
								.email("support@example.com"))
						.license(new License()
								.name("Apache 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0.html")));
	}
}
