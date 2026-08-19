const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type ApiRequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
};

type ApiErrorBody = {
  status?: number;
  error?: string;
  message?: string;
  timestamp?: string;
  path?: string;
};

export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly details?: ApiErrorBody;

  constructor(message: string, status: number, code?: string, details?: ApiErrorBody) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export async function apiRequest<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    credentials: "include",
    headers: buildHeaders(options.headers, options.body),
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (!response.ok) {
    throw await buildApiError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

function buildHeaders(headers: HeadersInit | undefined, body: unknown): HeadersInit {
  if (body === undefined) {
    return headers ?? {};
  }

  return {
    "Content-Type": "application/json",
    ...headers,
  };
}

async function buildApiError(response: Response): Promise<ApiError> {
  const fallbackMessage = `Request failed with status ${response.status}`;

  try {
    const body = (await response.json()) as ApiErrorBody;

    return new ApiError(
      body.message ?? fallbackMessage,
      response.status,
      body.error,
      body,
    );
  } catch {
    return new ApiError(fallbackMessage, response.status);
  }
}
