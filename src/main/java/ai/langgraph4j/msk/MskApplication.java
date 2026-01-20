package ai.langgraph4j.msk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MskApplication {

	public static void main(String[] args) {
		SpringApplication.run(MskApplication.class, args);
	}

}
