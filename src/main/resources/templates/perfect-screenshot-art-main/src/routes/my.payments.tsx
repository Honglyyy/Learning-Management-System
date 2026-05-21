import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { SiteHeader } from "@/components/SiteHeader";
import { RequireAuth } from "@/components/RequireRole";
import { ApiAlert } from "@/components/ApiAlert";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

export const Route = createFileRoute("/my/payments")({
  head: () => ({ meta: [{ title: "My payments — Lumen LMS" }] }),
  component: () => <RequireAuth><Page /></RequireAuth>,
});

function statusVariant(s: string): "default" | "secondary" | "destructive" | "outline" {
  if (s === "PAID") return "default";
  if (s === "PENDING") return "secondary";
  if (s === "FAILED") return "destructive";
  return "outline";
}

function Page() {
  const { data, isLoading, error } = useQuery<any[]>({
    queryKey: ["my-payments"],
    queryFn: () => api("/api/payments/me"),
  });
  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <div className="container mx-auto px-4 py-10">
        <h1 className="mb-6 text-2xl font-semibold text-foreground">My payments</h1>
        <ApiAlert error={error} />
        {isLoading ? <Skeleton className="h-64 w-full" /> : !data || data.length === 0 ? (
          <p className="text-muted-foreground">No payments yet.</p>
        ) : (
          <div className="rounded-md border border-border bg-card">
            <Table>
              <TableHeader><TableRow>
                <TableHead>Course</TableHead><TableHead>Amount</TableHead><TableHead>Provider</TableHead><TableHead>Reference</TableHead><TableHead>Status</TableHead><TableHead>Date</TableHead>
              </TableRow></TableHeader>
              <TableBody>
                {data.map((p: any) => (
                  <TableRow key={p.paymentId || p.id}>
                    <TableCell>{p.courseTitle || `#${p.courseId}`}</TableCell>
                    <TableCell>${Number(p.amount ?? 0).toFixed(2)}</TableCell>
                    <TableCell>{p.provider}</TableCell>
                    <TableCell className="font-mono text-xs">{p.providerReference || "—"}</TableCell>
                    <TableCell><Badge variant={statusVariant(p.status)}>{p.status}</Badge></TableCell>
                    <TableCell>{p.createdAt ? new Date(p.createdAt).toLocaleString() : "—"}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </div>
    </div>
  );
}
