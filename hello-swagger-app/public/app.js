const button = document.getElementById("try");
const result = document.getElementById("result");

button.addEventListener("click", async () => {
  button.disabled = true;
  try {
    const response = await fetch("/api/hello");
    const body = await response.json();
    result.textContent = `HTTP ${response.status}\n${JSON.stringify(body, null, 2)}`;
  } catch (error) {
    result.textContent = error instanceof Error ? error.message : "Request failed";
  } finally {
    button.disabled = false;
  }
});
