export async function GET() {
  return Response.json(
    { message: "hello world" },
    {
      headers: {
        "cache-control": "no-store",
      },
    },
  );
}
