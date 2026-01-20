package ai.langgraph4j.msk.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 텍스트 생성 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextGenerationResponse {
	
	/**
	 * 생성된 텍스트 응답
	 */
	private String response;
	
	/**
	 * 사용된 모델명
	 */
	private String model;
}
