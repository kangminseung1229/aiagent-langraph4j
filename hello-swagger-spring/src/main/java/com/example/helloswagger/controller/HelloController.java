package com.example.helloswagger.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Hello", description = "Hello World API")
public class HelloController {

    @GetMapping("/hello")
    @Operation(
            summary = "Say hello",
            description = "Returns a simple hello world message."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Greeting returned",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    public Map<String, String> hello() {
        return Map.of("message", "hello world");
    }
}
