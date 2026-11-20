# Hello Swagger App

Spring Boot 3 + springdoc-openapi demo.

## Run

```bash
cd hello-swagger-app
./mvnw spring-boot:run
```

## Endpoints

| Path | Description |
|------|-------------|
| `GET /api/hello` | Returns `{"message":"hello world"}` |
| `/swagger-ui.html` | Swagger UI |
| `/v3/api-docs` | OpenAPI JSON |
