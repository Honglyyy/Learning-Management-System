import { createFileRoute, Link, useNavigate, useSearch } from "@tanstack/react-router";
import { useState } from "react";
import { api } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { InputOTP, InputOTPGroup, InputOTPSlot, InputOTPSeparator } from "@/components/ui/input-otp";
import { ApiAlert } from "@/components/ApiAlert";
import { toast } from "sonner";

type Search = { email?: string };

export const Route = createFileRoute("/verify-otp")({
  head: () => ({ meta: [{ title: "Verify OTP — Lumen LMS" }] }),
  validateSearch: (s: Record<string, unknown>): Search => ({ email: typeof s.email === "string" ? s.email : undefined }),
  component: VerifyOtpPage,
});

function VerifyOtpPage() {
  const search = useSearch({ from: "/verify-otp" });
  const nav = useNavigate();
  const [email, setEmail] = useState(search.email || "");
  const [otp, setOtp] = useState("");
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (otp.length !== 6) {
      setError(new Error("Please enter the full 6-digit code."));
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await api("/verify-otp", { method: "POST", body: { email, otp }, auth: false });
      toast.success("Email verified. Please sign in.");
      nav({ to: "/login" });
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  }

  async function resend() {
    if (!email) {
      setError(new Error("Enter your email to resend the code."));
      return;
    }
    try {
      await api("/resend-otp", { method: "POST", body: { email }, auth: false });
      toast.success("A new code has been sent.");
    } catch (e) {
      setError(e);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-accent/30 px-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Verify your email</CardTitle>
          <p className="text-sm text-muted-foreground">Enter the 6-digit code we sent you.</p>
        </CardHeader>
        <CardContent>
          <ApiAlert error={error} />
          <form onSubmit={submit} className="space-y-5">
            <div className="space-y-2">
              <Label>Email</Label>
              <Input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Verification code</Label>
              <div className="flex justify-center py-2">
                <InputOTP maxLength={6} value={otp} onChange={setOtp} autoFocus>
                  <InputOTPGroup>
                    <InputOTPSlot index={0} className="h-12 w-12 text-lg" />
                    <InputOTPSlot index={1} className="h-12 w-12 text-lg" />
                    <InputOTPSlot index={2} className="h-12 w-12 text-lg" />
                  </InputOTPGroup>
                  <InputOTPSeparator />
                  <InputOTPGroup>
                    <InputOTPSlot index={3} className="h-12 w-12 text-lg" />
                    <InputOTPSlot index={4} className="h-12 w-12 text-lg" />
                    <InputOTPSlot index={5} className="h-12 w-12 text-lg" />
                  </InputOTPGroup>
                </InputOTP>
              </div>
            </div>
            <Button type="submit" className="w-full" disabled={loading || otp.length !== 6}>
              {loading ? "Verifying..." : "Verify"}
            </Button>
          </form>
          <div className="mt-4 flex justify-between text-sm text-muted-foreground">
            <button type="button" onClick={resend} className="hover:text-foreground">Resend code</button>
            <Link to="/login" className="hover:text-foreground">Back to sign in</Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
