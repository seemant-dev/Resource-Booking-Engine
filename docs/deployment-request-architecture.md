# Deployment request architecture

## Decision

Use the Next.js `/api` proxy with host-only, `HttpOnly`, `SameSite=Lax` cookies.

```text
Browser -> frontend origin /api/* -> Next.js proxy -> Spring API /api/*
```

The browser never receives the backend origin. `API_INTERNAL_URL` is server-only and must contain an HTTP(S) origin without a path, query, or fragment.

| Environment | Browser origin | `API_INTERNAL_URL` | Cookie `Secure` |
| --- | --- | --- | --- |
| Local | `http://localhost:3000` | `http://localhost:8080` | `false` |
| Production | Frontend HTTPS origin | Hosted/internal API origin | `true` |

## Consequences

- Browser API requests are same-origin and do not require CORS preflights.
- The proxy must forward request cookies and backend `Set-Cookie` response headers.
- Production keeps `SameSite=Lax`; `SameSite=None` is not required.
- `GET /health` is not proxied. Infrastructure checks it directly on the backend.

## Fallback

Use direct cross-site browser requests only if the frontend host cannot run Next.js rewrites. That fallback requires `SameSite=None`, `Secure`, credentialed CORS with an exact frontend origin, and strict origin validation.
