import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { decodeJwt, getToken, setToken, type JwtClaims } from "./api";

export type Role = "ADMIN" | "INSTRUCTOR" | "USER" | "STUDENT";

type AuthCtx = {
  token: string | null;
  claims: JwtClaims | null;
  role: Role | null;
  email: string | null;
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
};

const Ctx = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setTok] = useState<string | null>(null);

  useEffect(() => {
    setTok(getToken());
    const onStorage = (e: StorageEvent) => {
      if (e.key === "token") setTok(e.newValue);
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  const login = useCallback((t: string) => {
    setToken(t);
    setTok(t);
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setTok(null);
  }, []);

  const value = useMemo<AuthCtx>(() => {
    const claims = decodeJwt(token);
    const rawRole = (claims?.role as string | undefined)?.toUpperCase();
    const role = (rawRole === "ADMIN" || rawRole === "INSTRUCTOR" || rawRole === "USER" || rawRole === "STUDENT"
      ? (rawRole as Role)
      : null);
    return {
      token,
      claims,
      role,
      email: (claims?.email as string) || (claims?.sub as string) || null,
      isAuthenticated: !!token,
      login,
      logout,
    };
  }, [token, login, logout]);

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
