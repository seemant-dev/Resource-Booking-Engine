import type { NextConfig } from "next";

const DEFAULT_API_INTERNAL_URL = "http://localhost:8080";

function apiInternalOrigin(): string {
  const configuredUrl = process.env.API_INTERNAL_URL ?? DEFAULT_API_INTERNAL_URL;
  const url = new URL(configuredUrl);

  if (url.protocol !== "http:" && url.protocol !== "https:") {
    throw new Error("API_INTERNAL_URL must use http or https");
  }

  if (url.pathname !== "/" || url.search || url.hash) {
    throw new Error("API_INTERNAL_URL must contain only the backend origin");
  }

  return url.origin;
}

const nextConfig: NextConfig = {
  turbopack: {
    root: process.cwd(),
  },
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${apiInternalOrigin()}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
