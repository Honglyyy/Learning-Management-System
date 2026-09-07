import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { decodeJwt, getToken, setToken, isTokenExpired, SESSION_EXPIRED_EVENT, type JwtClaims, type SessionExpiredReason } from "./api";
import { toast } from "sonner";

export type Role = "ADMIN" | "INSTRUCTOR" | "USER" | "STUDENT";

export const INACTIVITY_TIMEOUT_MS = 15 * 60 * 1000; // 15 minutes of inactivity
const ACTIVITY_KEY = "lms_last_activity";

type AuthCtx = {
  token: string | null;
  claims: JwtClaims | null;
  role: Role | null;
  email: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string) => void;
  logout: (reason?: SessionExpiredReason) => void;
  refreshActivity: () => void;
  inactivityTimeoutMs: number;
};

const Ctx = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  // Synchronously initialize token from localStorage on client to prevent premature redirect
  const [token, setTok] = useState<string | null>(() => {
    if (typeof window !== "undefined") {
      const existing = getToken();
      if (existing && !isTokenExpired(existing)) {
        return existing;
      }
    }
    return null;
  });

  // During SSR or before client hydration, keep isLoading true
  const [isLoading, setIsLoading] = useState<boolean>(() => {
    return typeof window === "undefined";
  });

  const lastWriteRef = useRef<number>(0);

  // Validate on mount and update activity timestamp
  useEffect(() => {
    setIsLoading(false);
    if (typeof window !== "undefined") {
      const existing = getToken();
      if (existing) {
        if (isTokenExpired(existing)) {
          setToken(null);
          setTok(null);
        } else {
          setTok(existing);
          // User is actively loading the page: refresh activity timestamp!
          window.localStorage.setItem(ACTIVITY_KEY, String(Date.now()));
        }
      }
    }
  }, []);

  const refreshActivity = useCallback(() => {
    const now = Date.now();
    // Throttle writing to localStorage to once every 5 seconds
    if (now - lastWriteRef.current > 5000) {
      lastWriteRef.current = now;
      if (typeof window !== "undefined") {
        window.localStorage.setItem(ACTIVITY_KEY, String(now));
      }
    }
  }, []);

  const logout = useCallback((reason?: SessionExpiredReason) => {
    setToken(null);
    if (typeof window !== "undefined") {
      window.localStorage.removeItem(ACTIVITY_KEY);
    }
    setTok(null);

    // Provide immediate user feedback
    if (reason === "inactivity") {
      toast.warning("You have been logged out due to inactivity.", { id: "auth-session-toast" });
    } else if (reason === "token_expired" || reason === "unauthorized") {
      toast.error("Your session has expired. Please sign in again.", { id: "auth-session-toast" });
    }

    // Redirect to login if user was on a protected page
    if (typeof window !== "undefined" && reason && reason !== "manual") {
      const path = window.location.pathname;
      const isAuthPage =
        path.startsWith("/login") ||
        path.startsWith("/register") ||
        path.startsWith("/forgot-password") ||
        path.startsWith("/reset-password") ||
        path.startsWith("/verify-otp");

      if (!isAuthPage) {
        window.location.href = `/login?reason=${encodeURIComponent(reason)}`;
      }
    }
  }, []);

  const login = useCallback((t: string) => {
    setToken(t);
    setTok(t);
    if (typeof window !== "undefined") {
      window.localStorage.setItem(ACTIVITY_KEY, String(Date.now()));
    }
  }, []);

  // Activity listeners across user interactions
  useEffect(() => {
    if (!token) return;

    // Stamp initial activity timestamp
    if (typeof window !== "undefined" && !window.localStorage.getItem(ACTIVITY_KEY)) {
      window.localStorage.setItem(ACTIVITY_KEY, String(Date.now()));
    }

    const onUserInteraction = () => {
      refreshActivity();
    };

    const events = ["mousedown", "keydown", "scroll", "touchstart", "click"] as const;
    events.forEach((evt) => window.addEventListener(evt, onUserInteraction, { passive: true }));

    return () => {
      events.forEach((evt) => window.removeEventListener(evt, onUserInteraction));
    };
  }, [token, refreshActivity]);

  // Periodic heartbeat checker: checks token validity and inactivity timeout
  useEffect(() => {
    if (!token) return;

    const interval = setInterval(() => {
      // 1. Check if token itself expired
      if (isTokenExpired(token)) {
        logout("token_expired");
        return;
      }

      // 2. Check inactivity timestamp
      if (typeof window !== "undefined") {
        const storedActivity = window.localStorage.getItem(ACTIVITY_KEY);
        if (!storedActivity) {
          window.localStorage.setItem(ACTIVITY_KEY, String(Date.now()));
          return;
        }
        const lastActivity = parseInt(storedActivity, 10);
        if (!isNaN(lastActivity)) {
          const elapsed = Date.now() - lastActivity;
          if (elapsed >= INACTIVITY_TIMEOUT_MS) {
            logout("inactivity");
          }
        }
      }
    }, 10000);

    return () => clearInterval(interval);
  }, [token, logout]);

  // Listen to cross-tab storage changes and custom session expired events
  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === "token") {
        if (!e.newValue) {
          setTok(null);
        } else {
          setTok(e.newValue);
        }
      }
    };

    const onSessionExpired = (e: Event) => {
      const customEvent = e as CustomEvent<{ reason?: SessionExpiredReason; message?: string }>;
      logout(customEvent.detail?.reason || "unauthorized");
    };

    window.addEventListener("storage", onStorage);
    window.addEventListener(SESSION_EXPIRED_EVENT, onSessionExpired);

    return () => {
      window.removeEventListener("storage", onStorage);
      window.removeEventListener(SESSION_EXPIRED_EVENT, onSessionExpired);
    };
  }, [logout]);

  const value = useMemo<AuthCtx>(() => {
    const claims = decodeJwt(token);
    const expired = isTokenExpired(token);
    const rawRole = (claims?.role as string | undefined)?.toUpperCase();
    const role =
      rawRole === "ADMIN" || rawRole === "INSTRUCTOR" || rawRole === "USER" || rawRole === "STUDENT"
        ? (rawRole as Role)
        : null;

    return {
      token: expired ? null : token,
      claims: expired ? null : claims,
      role: expired ? null : role,
      email: expired ? null : (claims?.email as string) || (claims?.sub as string) || null,
      isAuthenticated: !expired && !!token,
      isLoading,
      login,
      logout,
      refreshActivity,
      inactivityTimeoutMs: INACTIVITY_TIMEOUT_MS,
    };
  }, [token, isLoading, login, logout, refreshActivity]);

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth() {
  const v = useContext(Ctx);
  if (!v) throw new Error("useAuth must be used inside AuthProvider");
  return v;
}

export function roleHome(role: Role | null): string {
  if (role === "ADMIN") return "/admin";
  if (role === "INSTRUCTOR") return "/instructor";
  return "/";
}
