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
    if (t) headers.Authorization = `Bearer ${t}`;
  }
  let body: BodyInit | undefined;
  if (opts.formData) {
    body = opts.formData;
  } else if (opts.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(opts.body);
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
