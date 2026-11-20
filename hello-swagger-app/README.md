# Hello Swagger App

Static `public/` site + `/api` handlers for Cursor Apps hosting.

## Run locally

```bash
cd hello-swagger-app
npm start
```

## Endpoints

| Path | Description |
|------|-------------|
| `/` | OpenAPI-style UI with Try it out |
| `GET /api/hello` | `{"message":"hello world"}` |
| `GET /api/openapi` | OpenAPI JSON |
