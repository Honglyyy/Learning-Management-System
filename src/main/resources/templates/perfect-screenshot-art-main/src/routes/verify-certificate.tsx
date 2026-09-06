import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useState, useEffect } from "react";
import { api, getApiBase } from "@/lib/api";
import { SiteHeader } from "@/components/SiteHeader";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ApiAlert } from "@/components/ApiAlert";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { Award, CheckCircle2, Search, ExternalLink, Calendar, Clock, User, ShieldCheck, Copy } from "lucide-react";

export const Route = createFileRoute("/verify-certificate")({
  head: () => ({ meta: [{ title: "Verify Certificate — Lumen LMS" }] }),
  component: VerifyCertificatePage,
});

type CertificateVerify = {
  certificateCode: string;
  studentName: string;
  courseTitle: string;
  issuedAt: string;
  instructorName?: string;
  programDuration?: string;
  finalScore?: number;
  isValid: boolean;
  viewUrl?: string;
};

function VerifyCertificatePage() {
  const [codeInput, setCodeInput] = useState("");
  const [submittedCode, setSubmittedCode] = useState("");

  useEffect(() => {
    if (typeof window !== "undefined") {
      const params = new URLSearchParams(window.location.search);
      const codeParam = params.get("code");
      if (codeParam) {
        setCodeInput(codeParam.trim());
        setSubmittedCode(codeParam.trim());
      }
    }
  }, []);

  const verifyQuery = useQuery<CertificateVerify>({
    queryKey: ["certificate-verify", submittedCode],
    queryFn: () => api<CertificateVerify>(`/api/certificates/verify/${encodeURIComponent(submittedCode)}`),
    enabled: !!submittedCode,
    retry: false,
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!codeInput.trim()) {
      toast.error("Please enter a certificate code");
      return;
    }
    setSubmittedCode(codeInput.trim());
  };

  const certificate = verifyQuery.data;
  const viewUrl = submittedCode ? `${getApiBase()}/api/certificates/${encodeURIComponent(submittedCode)}/view` : "";

  const copyLink = () => {
    if (typeof window !== "undefined") {
      const url = `${window.location.origin}/verify-certificate?code=${encodeURIComponent(submittedCode)}`;
      navigator.clipboard.writeText(url);
      toast.success("Verification link copied to clipboard!");
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <main className="container mx-auto max-w-3xl px-4 py-12 space-y-8">
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 rounded-full bg-primary/10 text-primary mb-2">
            <Award className="h-8 w-8" />
          </div>
          <h1 className="text-3xl font-bold tracking-tight text-foreground">Verify Certificate Authenticity</h1>
          <p className="text-muted-foreground max-w-lg mx-auto text-sm">
            Enter an official Lumen LMS certificate credential identifier (e.g. <code>DA-2026-03-XXXX</code>) to verify its validity, recipient, and issuing authority.
          </p>
        </div>

        <Card className="shadow-md">
          <CardContent className="pt-6">
            <form onSubmit={handleSearch} className="flex gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  className="pl-9 font-mono uppercase"
                  placeholder="Enter Certificate Code (e.g. DA-2026-03-XXXX)"
                  value={codeInput}
                  onChange={(e) => setCodeInput(e.target.value)}
                />
              </div>
              <Button type="submit" disabled={verifyQuery.isLoading}>
                {verifyQuery.isLoading ? "Verifying..." : "Verify"}
              </Button>
            </form>
          </CardContent>
        </Card>

        {verifyQuery.isLoading && (
          <div className="space-y-4">
            <Skeleton className="h-48 w-full rounded-xl" />
          </div>
        )}

        {verifyQuery.error && (
          <Card className="border-rose-200 dark:border-rose-900 bg-rose-50/50 dark:bg-rose-950/20">
            <CardContent className="p-6 text-center space-y-2">
              <div className="text-rose-600 font-semibold text-base">Certificate Not Found</div>
              <p className="text-xs text-muted-foreground">
                No active or valid certificate was found with credential code <code>{submittedCode}</code>. Please double-check the code and try again.
              </p>
            </CardContent>
          </Card>
        )}

        {certificate && (
          <Card className="border-emerald-500/50 shadow-lg overflow-hidden animate-in fade-in-50">
            <div className="bg-emerald-600 px-6 py-4 text-white flex items-center justify-between">
              <div className="flex items-center gap-2">
                <ShieldCheck className="h-6 w-6" />
                <span className="font-bold tracking-wide">AUTHENTIC CERTIFICATE VERIFIED</span>
              </div>
              <Badge className="bg-white/20 hover:bg-white/30 text-white border-0 text-xs">
                VALID CREDENTIAL
              </Badge>
            </div>
            <CardHeader className="pb-4">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                <div>
                  <CardDescription className="text-xs">Certified Recipient</CardDescription>
                  <CardTitle className="text-2xl font-bold text-foreground">{certificate.studentName}</CardTitle>
                </div>
                <Badge variant="outline" className="font-mono text-sm px-3 py-1 self-start sm:self-center">
                  {certificate.certificateCode}
                </Badge>
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 rounded-lg bg-muted/40 p-4 border border-border text-sm">
                <div>
                  <span className="text-xs text-muted-foreground block mb-1">Course Program</span>
                  <p className="font-semibold text-foreground">{certificate.courseTitle}</p>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground block mb-1">Program Duration</span>
                  <p className="font-medium text-foreground flex items-center gap-1">
                    <Clock className="h-3.5 w-3.5 text-muted-foreground" />
                    {certificate.programDuration || "Comprehensive Study Program"}
                  </p>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground block mb-1">Issue Date</span>
                  <p className="font-medium text-foreground flex items-center gap-1">
                    <Calendar className="h-3.5 w-3.5 text-muted-foreground" />
                    {certificate.issuedAt ? new Date(certificate.issuedAt).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' }) : "Verified"}
                  </p>
                </div>
                {certificate.instructorName && (
                  <div>
                    <span className="text-xs text-muted-foreground block mb-1">Instructor / Evaluator</span>
                    <p className="font-medium text-foreground flex items-center gap-1">
                      <User className="h-3.5 w-3.5 text-muted-foreground" />
                      {certificate.instructorName}
                    </p>
                  </div>
                )}
              </div>

              <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
                <Button
                  size="sm"
                  variant="outline"
                  className="gap-1.5"
                  onClick={copyLink}
                >
                  <Copy className="h-3.5 w-3.5" />
                  Copy Verification Link
                </Button>

                {viewUrl && (
                  <Button
                    size="sm"
                    className="gap-1.5 bg-[#005a87] hover:bg-[#00476a] text-white"
                    onClick={() => window.open(viewUrl, "_blank", "noopener,noreferrer")}
                  >
                    <ExternalLink className="h-3.5 w-3.5" />
                    View Official Certificate Template
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        )}
      </main>
    </div>
  );
}
