import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { api } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ApiAlert } from "@/components/ApiAlert";
import { toast } from "sonner";

export const Route = createFileRoute("/forgot-password")({
  head: () => ({ meta: [{ title: "Forgot password — Lumen LMS" }] }),
  component: ForgotPage,
});

function ForgotPage() {
  const nav = useNavigate();
  const [email, setEmail] = useState("");
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null); setLoading(true);
    try {
      await api(`/send-reset-otp`, { method: "POST", auth: false, query: { email } });
      toast.success("Reset OTP sent. Check your email.");
      nav({ to: "/reset-password", search: { email } as any });
    } catch (e) { setError(e); } finally { setLoading(false); }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-accent/30 px-4">
      <Card className="w-full max-w-md">
        <CardHeader><CardTitle>Reset your password</CardTitle><p className="text-sm text-muted-foreground">We'll email you a one-time code.</p></CardHeader>
        <CardContent>
          <ApiAlert error={error} />
          <form onSubmit={submit} className="space-y-4">
            <div className="space-y-2"><Label>Email</Label><Input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} /></div>
            <Button type="submit" className="w-full" disabled={loading}>{loading ? "Sending..." : "Send reset OTP"}</Button>
          </form>
          <p className="mt-4 text-center text-sm"><Link to="/login" className="text-muted-foreground hover:text-foreground">Back to sign in</Link></p>
        </CardContent>
      </Card>
    </div>
  );
}
