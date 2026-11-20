const fs = require("fs");
const path = require("path");

const publicDir = path.join(__dirname, "..", "public");
fs.mkdirSync(publicDir, { recursive: true });

if (!fs.existsSync(path.join(publicDir, "index.html"))) {
  throw new Error("public/index.html is missing");
}

console.log("Build complete: public/ ready");
