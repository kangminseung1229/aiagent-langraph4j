const http = require("http");
const fs = require("fs");
const path = require("path");

const port = Number(process.env.PORT) || 8080;
const publicDir = path.join(__dirname, "..", "public");

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
  },
};

function sendJson(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, {
    "content-type": "application/json; charset=utf-8",
    "cache-control": "no-store",
    "content-length": Buffer.byteLength(payload),
  });
  res.end(payload);
}

function sendFile(res, filePath) {
  const ext = path.extname(filePath).toLowerCase();
  const type =
    ext === ".html"
      ? "text/html; charset=utf-8"
      : ext === ".css"
        ? "text/css; charset=utf-8"
        : ext === ".js"
          ? "text/javascript; charset=utf-8"
          : "application/octet-stream";
  const body = fs.readFileSync(filePath);
  res.writeHead(200, { "content-type": type, "cache-control": "no-store" });
  res.end(body);
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url || "/", `http://${req.headers.host || "localhost"}`);

  if (url.pathname === "/api/hello") {
    return sendJson(res, 200, { message: "hello world" });
  }
  if (url.pathname === "/api/openapi") {
    return sendJson(res, 200, openApiDocument);
  }

  const relative =
    url.pathname === "/" ? "index.html" : url.pathname.replace(/^\//, "");
  const filePath = path.join(publicDir, relative);
  if (!filePath.startsWith(publicDir) || !fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    res.writeHead(404, { "content-type": "text/plain; charset=utf-8" });
    return res.end("Not found");
  }
  return sendFile(res, filePath);
});

server.listen(port, "0.0.0.0", () => {
  console.log(`Hello Swagger listening on http://127.0.0.1:${port}`);
});
