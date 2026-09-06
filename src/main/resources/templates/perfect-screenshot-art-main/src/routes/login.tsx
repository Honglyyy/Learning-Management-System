import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { api } from "@/lib/api";
import { useAuth, roleHome } from "@/lib/auth";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ApiAlert } from "@/components/ApiAlert";

export const Route = createFileRoute("/login")({
  head: () => ({ meta: [{ title: "Sign in — Lumen LMS" }] }),
  component: LoginPage,
});

function LoginPage() {
  const nav = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const token = await api<string>("/authenticate", { method: "POST", body: { email, password }, auth: false });
      const tokenStr = typeof token === "string" ? token : (token as any)?.token || (token as any)?.jwt;
      if (!tokenStr) throw new Error("No token in response");
      login(tokenStr);
      const claims = JSON.parse(atob(tokenStr.split(".")[1]));
      const role = (claims?.role as string)?.toUpperCase();
      nav({ to: roleHome((role as any) || null) });
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-accent/30 px-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-2xl">Welcome back</CardTitle>
          <p className="text-sm text-muted-foreground">Sign in to your Lumen LMS account.</p>
        </CardHeader>
        <CardContent>
          <ApiAlert error={error} />
          <form onSubmit={submit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email, Username, or Phone</Label>
              <Input
                id="email"
                type="text"
                required
                placeholder="Email, username, or phone number"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="pw">Password</Label>
              <Input
                id="pw"
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Signing in..." : "Sign in"}
            </Button>
          </form>
          <div className="mt-4 flex justify-between text-sm text-muted-foreground">
            <Link to="/forgot-password" className="hover:text-foreground">Forgot password?</Link>
            <Link to="/register" className="hover:text-foreground">Create account</Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
