type ApiRequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
};

type ApiPath = `/api/${string}`;

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
  path: ApiPath,
  options: ApiRequestOptions = {},
): Promise<T> {
  const response = await fetch(path, {
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
