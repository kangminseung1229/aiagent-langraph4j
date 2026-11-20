"use client";

import { useState } from "react";

export default function HomePage() {
  const [result, setResult] = useState("Click Try it out to call GET /api/hello");
  const [loading, setLoading] = useState(false);

  async function tryHello() {
    setLoading(true);
    try {
      const response = await fetch("/api/hello");
      const body = await response.json();
      setResult(`HTTP ${response.status}\n${JSON.stringify(body, null, 2)}`);
    } catch (error) {
      setResult(error instanceof Error ? error.message : "Request failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main>
      <section className="hero">
        <h1>Hello Swagger API</h1>
        <p>
          OpenAPI-style demo. <code>GET /api/hello</code> returns{" "}
          <code>{`{"message":"hello world"}`}</code>.
        </p>
      </section>

      <section className="panel">
        <div className="endpoint">
          <span className="method">GET</span>
          <span className="path">/api/hello</span>
        </div>
        <div className="body">
          <h2>Say hello</h2>
          <p>Returns a simple hello world message.</p>
          <button type="button" onClick={tryHello} disabled={loading}>
            {loading ? "Calling…" : "Try it out"}
          </button>
          <pre className="result">{result}</pre>
        </div>
      </section>

      <div className="links">
        <a href="/api/hello">Open /api/hello</a>
        <a href="/api/openapi">Open OpenAPI JSON</a>
      </div>
    </main>
  );
}
