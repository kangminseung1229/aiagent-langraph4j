const openApiDocument = {
  openapi: "3.1.0",
  info: {
    title: "Hello Swagger API",
    description: "Demo that returns hello world via OpenAPI.",
    version: "1.0.0",
  },
  paths: {
    "/api/hello": {
      get: {
        tags: ["Hello"],
        summary: "Say hello",
        description: "Returns a simple hello world message.",
        responses: {
          "200": {
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
  },
};

export async function GET() {
  return Response.json(openApiDocument, {
    headers: { "cache-control": "no-store" },
  });
}
