import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Hello Swagger API",
  description: "Hello World API with OpenAPI docs",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
