import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "BookEngine — Resource Booking",
  description: "Frontend foundation for the concurrency-safe resource booking engine.",
};

/**
 * No next/font/google here on purpose: the approved design specifies a
 * system font stack (-apple-system, "Segoe UI", system-ui, sans-serif),
 * so there's no need to fetch a webfont at build time. This also avoids
 * the build depending on network access to fonts.googleapis.com.
 */
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="h-full">{children}</body>
    </html>
  );
}
