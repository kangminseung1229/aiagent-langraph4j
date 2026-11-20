const express = require("express");
const swaggerUi = require("swagger-ui-express");

const app = express();
const port = Number(process.env.PORT) || 8080;

const openApiDocument = {
  openapi: "3.1.0",
  info: {
    title: "Hello Swagger API",
    description: "Demo that returns hello world via Swagger/OpenAPI.",
    version: "1.0.0",
  },
  paths: {
    "/api/hello": {
      get: {
        tags: ["Hello"],
        summary: "Say hello",
        description: "Returns a simple hello world message.",
        responses: {
          200: {
            description: "Greeting returned",
            content: {
              "application/json": {
                schema: {
                  type: "object",
                  properties: {
                    message: { type: "string", example: "hello world" },
                  },
                  required: ["message"],
                },
              },
            },
          },
        },
      },
    },
    "/health": {
      get: {
        tags: ["Health"],
        summary: "Health check",
        responses: {
          200: {
            description: "Service is healthy",
            content: {
              "application/json": {
                schema: {
                  type: "object",
                  properties: {
                    status: { type: "string", example: "ok" },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
};

app.get("/api/hello", (_req, res) => {
  res.json({ message: "hello world" });
});

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

app.use("/swagger-ui", swaggerUi.serve, swaggerUi.setup(openApiDocument));
app.get("/swagger-ui.html", (_req, res) => {
  res.redirect("/swagger-ui/");
});
app.get("/v3/api-docs", (_req, res) => {
  res.json(openApiDocument);
});
app.get("/", (_req, res) => {
  res.redirect("/swagger-ui/");
});

app.listen(port, "0.0.0.0", () => {
  console.log(`Hello Swagger API listening on http://127.0.0.1:${port}`);
});
