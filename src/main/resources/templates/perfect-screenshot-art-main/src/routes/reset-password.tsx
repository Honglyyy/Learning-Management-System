import { createFileRoute, Link, useNavigate, useSearch } from "@tanstack/react-router";
import { useState } from "react";
import { api } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ApiAlert } from "@/components/ApiAlert";
import { toast } from "sonner";

type Search = { email?: string };

export const Route = createFileRoute("/reset-password")({
  head: () => ({ meta: [{ title: "Reset password — Lumen LMS" }] }),
  validateSearch: (s: Record<string, unknown>): Search => ({ email: typeof s.email === "string" ? s.email : undefined }),
  component: ResetPage,
});

function ResetPage() {
  const search = useSearch({ from: "/reset-password" });
  const nav = useNavigate();
  const [form, setForm] = useState({ email: search.email || "", otp: "", password: "" });
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null); setLoading(true);
    try {
      await api("/reset-password", { method: "POST", body: form, auth: false });
      toast.success("Password updated. Please sign in.");
      nav({ to: "/login" });
    } catch (e) { setError(e); } finally { setLoading(false); }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-accent/30 px-4">
      <Card className="w-full max-w-md">
        <CardHeader><CardTitle>Set a new password</CardTitle></CardHeader>
        <CardContent>
          <ApiAlert error={error} />
          <form onSubmit={submit} className="space-y-4">
            <div className="space-y-2"><Label>Email</Label><Input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
            <div className="space-y-2"><Label>OTP</Label><Input required value={form.otp} onChange={(e) => setForm({ ...form, otp: e.target.value })} /></div>
            <div className="space-y-2"><Label>New password</Label><Input type="password" required minLength={6} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div>
            <Button type="submit" className="w-full" disabled={loading}>{loading ? "Updating..." : "Reset password"}</Button>
          </form>
          <p className="mt-4 text-center text-sm"><Link to="/login" className="text-muted-foreground hover:text-foreground">Back to sign in</Link></p>
        </CardContent>
      </Card>
    </div>
  );
}
