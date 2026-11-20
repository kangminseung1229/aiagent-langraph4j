# Hello Swagger App

Node.js Express demo with Swagger UI. Cursor Apps hosting runs this web app (not the JVM Spring Boot sibling in `hello-swagger-spring/`).

## Run

```bash
cd hello-swagger-app
npm install
npm start
```

## Endpoints

| Path | Description |
|------|-------------|
| `GET /api/hello` | Returns `{"message":"hello world"}` |
| `/swagger-ui/` | Swagger UI |
| `/v3/api-docs` | OpenAPI JSON |
| `/health` | Health check |
