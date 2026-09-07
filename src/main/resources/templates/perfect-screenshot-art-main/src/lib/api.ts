// Lightweight fetch wrapper that talks to the Spring Boot LMS backend.
// Backend base URL: VITE_API_BASE_URL > localStorage('apiBaseUrl') > default.

const DEFAULT_BASE = "http://localhost:8080";

export function getApiBase(): string {
  if (typeof window !== "undefined") {
    const ls = window.localStorage.getItem("apiBaseUrl");
    if (ls) return ls.replace(/\/$/, "");
  }
  const env = (import.meta as any).env?.VITE_API_BASE_URL as string | undefined;
  return (env || DEFAULT_BASE).replace(/\/$/, "");
}

export function setApiBase(url: string) {
  if (typeof window === "undefined") return;
  window.localStorage.setItem("apiBaseUrl", url.replace(/\/$/, ""));
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem("token");
}

export function setToken(token: string | null) {
  if (typeof window === "undefined") return;
  if (token) window.localStorage.setItem("token", token);
  else window.localStorage.removeItem("token");
}

export type JwtClaims = {
  sub?: string;
  email?: string;
  role?: string;
  exp?: number;
  [k: string]: unknown;
};

export const SESSION_EXPIRED_EVENT = "lms:session-expired";
export type SessionExpiredReason = "inactivity" | "token_expired" | "unauthorized" | "manual";

export function decodeJwt(token: string | null): JwtClaims | null {
  if (!token) return null;
  try {
    const part = token.split(".")[1];
    const json = atob(part.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(decodeURIComponent(escape(json)));
  } catch {
    try {
      return JSON.parse(atob(token.split(".")[1]));
    } catch {
      return null;
    }
  }
}

export function isTokenExpired(token: string | null): boolean {
  if (!token) return false;
  const claims = decodeJwt(token);
  if (!claims || typeof claims.exp !== "number") return false;
  // Check if expiration has passed (claims.exp is in seconds)
  return claims.exp * 1000 <= Date.now();
}

export function notifySessionExpired(reason: SessionExpiredReason = "token_expired", message?: string) {
  if (typeof window === "undefined") return;
  setToken(null);
  window.dispatchEvent(
    new CustomEvent(SESSION_EXPIRED_EVENT, {
      detail: { reason, message: message || "Your session has expired. Please sign in again." },
    })
  );
}

export class ApiError extends Error {
  status: number;
  data: unknown;
  constructor(message: string, status: number, data: unknown) {
    super(message);
    this.status = status;
    this.data = data;
  }
}

type ApiOptions = {
  method?: string;
  body?: unknown;
  formData?: FormData;
  auth?: boolean;
  query?: Record<string, string | number | undefined>;
};

export async function api<T = any>(path: string, opts: ApiOptions = {}): Promise<T> {
  const base = getApiBase();
  const qs = opts.query
    ? "?" +
      Object.entries(opts.query)
        .filter(([, v]) => v !== undefined && v !== "")
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
        .join("&")
    : "";
  const url = `${base}${path}${qs}`;
  const headers: Record<string, string> = {};
  if (opts.auth !== false) {
    const t = getToken();
    if (t) {
      if (isTokenExpired(t)) {
        notifySessionExpired("token_expired", "Your session has expired. Please sign in again.");
        throw new ApiError("Session has expired. Please sign in again.", 401, null);
      }
      headers.Authorization = `Bearer ${t}`;
    }
  }
  let body: BodyInit | undefined;
  if (opts.formData) {
    body = opts.formData;
  } else if (typeof FormData !== "undefined" && opts.body instanceof FormData) {
    body = opts.body;
  } else if (opts.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = typeof opts.body === "string" ? opts.body : JSON.stringify(opts.body);
  }
  const res = await fetch(url, { method: opts.method || "GET", headers, body });
  const ct = res.headers.get("content-type") || "";
  let data: any = null;
  if (ct.includes("application/json")) {
    data = await res.json().catch(() => null);
  } else {
    data = await res.text().catch(() => null);
  }
  if (!res.ok) {
    const msg =
      (data && typeof data === "object" && (data.message || data.error)) ||
      (typeof data === "string" && data) ||
      `Request failed (${res.status})`;
    if (res.status === 401 && opts.auth !== false && getToken()) {
      notifySessionExpired("unauthorized", "Unauthorized: Your session has expired or is invalid.");
    }
    throw new ApiError(msg, res.status, data);
  }
  return data as T;
}

// Resolve relative media URLs (e.g. /uploads/...) to the API host.
export function mediaUrl(path?: string | null): string {
  if (!path) return "";
  if (/^https?:\/\//.test(path)) return path;
  return `${getApiBase()}${path.startsWith("/") ? "" : "/"}${path}`;
}
