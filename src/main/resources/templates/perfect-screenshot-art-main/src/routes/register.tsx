import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { api } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ApiAlert } from "@/components/ApiAlert";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { toast } from "sonner";

export const Route = createFileRoute("/register")({
  head: () => ({ meta: [{ title: "Create account — Lumen LMS" }] }),
  component: RegisterPage,
});

function RegisterPage() {
  const nav = useNavigate();
  const [form, setForm] = useState({
    fullName: "",
    phoneNumber: "",
    email: "",
    username: "",
    password: "",
    role: "STUDENT" as "STUDENT" | "INSTRUCTOR",
  });
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await api("/register", { method: "POST", body: form, auth: false });
      toast.success("Account created. Check your email for the OTP.");
      nav({ to: "/verify-otp", search: { email: form.email } as any });
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-accent/30 px-4 py-10">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-2xl">Create your account</CardTitle>
          <p className="text-sm text-muted-foreground">Start learning or teaching today.</p>
        </CardHeader>
        <CardContent>
          <ApiAlert error={error} />
          <form onSubmit={submit} className="space-y-4">
            <div className="space-y-2">
              <Label>Full Name</Label>
              <Input
                placeholder="e.g. Jane Doe"
                value={form.fullName}
                onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Phone Number</Label>
              <Input
                placeholder="e.g. +123456789"
                value={form.phoneNumber}
                onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Email</Label>
              <Input
                type="email"
                required
                placeholder="name@example.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Username</Label>
              <Input
                required
                placeholder="username"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Password</Label>
              <Input
                type="password"
                required
                minLength={6}
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>I want to</Label>
              <RadioGroup
                value={form.role}
                onValueChange={(v) => setForm({ ...form, role: v as any })}
                className="grid grid-cols-2 gap-2"
              >
                <Label className="flex items-center gap-2 rounded-md border border-border p-3 cursor-pointer">
                  <RadioGroupItem value="STUDENT" /> Learn
                </Label>
                <Label className="flex items-center gap-2 rounded-md border border-border p-3 cursor-pointer">
                  <RadioGroupItem value="INSTRUCTOR" /> Teach
                </Label>
              </RadioGroup>
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Creating..." : "Create account"}
            </Button>
          </form>
          <p className="mt-4 text-center text-sm text-muted-foreground">Already have an account? <Link to="/login" className="text-foreground hover:underline">Sign in</Link></p>
        </CardContent>
      </Card>
    </div>
  );
}
