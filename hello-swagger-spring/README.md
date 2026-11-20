# Hello Swagger Spring (local/JVM)

Spring Boot 3 + springdoc-openapi demo. For Cursor Apps hosting use `../hello-swagger-app` (Node Express); Apps does not run JVM Spring Boot.

## Run

```bash
cd hello-swagger-spring
./mvnw spring-boot:run
```

## Endpoints

| Path | Description |
|------|-------------|
| `GET /api/hello` | Returns `{"message":"hello world"}` |
| `/swagger-ui.html` | Swagger UI |
| `/v3/api-docs` | OpenAPI JSON |
