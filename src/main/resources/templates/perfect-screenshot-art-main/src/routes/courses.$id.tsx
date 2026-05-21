import { createFileRoute, useNavigate, useParams } from "@tanstack/react-router";
import { useQuery, useMutation } from "@tanstack/react-query";
import { api, mediaUrl } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { SiteHeader } from "@/components/SiteHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ApiAlert } from "@/components/ApiAlert";
import { Clock, User, Star } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";

export const Route = createFileRoute("/courses/$id")({
  component: CourseDetail,
});

function CourseDetail() {
  const { id } = useParams({ from: "/courses/$id" });
  const nav = useNavigate();
  const { isAuthenticated } = useAuth();
  const [checkoutError, setCheckoutError] = useState<unknown>(null);

  const course = useQuery<any>({
    queryKey: ["course", id],
    queryFn: () => api(`/api/courses/${id}`, { auth: true }),
  });

  const sections = useQuery<any[]>({
    queryKey: ["sections", id],
    queryFn: async () => {
      const all = await api<any[]>(`/api/sections`, { auth: true }).catch(() => []);
      return (all || []).filter((s: any) => String(s.courseId ?? s.course?.id) === String(id));
    },
  });

  const reviews = useQuery<any[]>({
    queryKey: ["reviews", id],
    queryFn: () => api<any[]>(`/api/courses/${id}/reviews`, { auth: true     }).catch(() => []),
  });

  const buy = useMutation({
    mutationFn: async () => {
      const checkout = await api<any>(`/api/payments/checkout`, { method: "POST", body: { courseId: Number(id), provider: "MANUAL" } });
      await api(`/api/payments/${checkout.paymentId}/confirm`, { method: "POST" });
      return checkout;
    },
    onSuccess: () => { toast.success("Payment confirmed. You're enrolled!"); nav({ to: "/my/enrollments" }); },
    onError: (e) => setCheckoutError(e),
  });

  if (course.isLoading) {
    return (
      <div className="min-h-screen bg-background">
        <SiteHeader />
        <div className="container mx-auto px-4 py-10"><Skeleton className="h-64 w-full" /></div>
      </div>
    );
  }

  if (course.error || !course.data) {
    return (
      <div className="min-h-screen bg-background"><SiteHeader />
        <div className="container mx-auto px-4 py-10"><ApiAlert error={course.error || "Course not found"} /></div>
      </div>
    );
  }

  const c = course.data;
  const cover = mediaUrl(c.coverDir);
  const inst = typeof c.instructor === "object" ? c.instructor?.username || c.instructor?.email : c.instructorName;
  const cats = (c.categories || []).map((x: any) => (typeof x === "string" ? x : x?.category)).filter(Boolean);

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <div className="container mx-auto grid gap-8 px-4 py-10 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <div className="mb-4 flex flex-wrap gap-2">{cats.map((cat: string) => <Badge key={cat} variant="secondary">{cat}</Badge>)}</div>
          <h1 className="text-3xl font-semibold text-foreground">{c.title}</h1>
          <p className="mt-2 text-muted-foreground">{c.description}</p>
          <div className="mt-4 flex flex-wrap gap-4 text-sm text-muted-foreground">
            {inst && <span className="flex items-center gap-1"><User className="h-4 w-4" />{inst}</span>}
            {c.overallDuration && <span className="flex items-center gap-1"><Clock className="h-4 w-4" />{c.overallDuration}</span>}
            {typeof c.rating === "number" && <span className="flex items-center gap-1"><Star className="h-4 w-4 fill-current" />{c.rating.toFixed(1)}</span>}
          </div>

          {cover && <img src={cover} alt={c.title} className="mt-6 aspect-video w-full rounded-lg object-cover" />}

          <h2 className="mt-10 mb-3 text-xl font-semibold text-foreground">Curriculum</h2>
          {sections.isLoading ? <Skeleton className="h-24 w-full" /> : sections.data && sections.data.length > 0 ? (
            <div className="space-y-2">
              {sections.data.map((s: any) => (
                <div key={s.id} className="rounded-md border border-border bg-card p-4">
                  <div className="flex items-center justify-between">
                    <div className="font-medium text-foreground">{s.title}</div>
                    {s.duration && <span className="text-xs text-muted-foreground">{s.duration}</span>}
                  </div>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-muted-foreground">No sections yet.</p>}

          <h2 className="mt-10 mb-3 text-xl font-semibold text-foreground">Reviews</h2>
          {reviews.data && reviews.data.length > 0 ? (
            <div className="space-y-3">
              {reviews.data.map((r: any) => (
                <Card key={r.id}><CardContent className="pt-4">
                  <div className="flex items-center gap-2 text-sm"><Star className="h-4 w-4 fill-current text-primary" />{r.rating}</div>
                  <p className="mt-1 text-sm text-foreground">{r.comment || r.content}</p>
                </CardContent></Card>
              ))}
            </div>
          ) : <p className="text-sm text-muted-foreground">No reviews yet.</p>}
        </div>

        <aside>
          <Card className="sticky top-20">
            <CardHeader><CardTitle className="text-3xl">${Number(c.price ?? 0).toFixed(2)}</CardTitle></CardHeader>
            <CardContent className="space-y-3">
              <ApiAlert error={checkoutError} />
              <Button
                className="w-full"
                disabled={buy.isPending}
                onClick={() => {
                  if (!isAuthenticated) { nav({ to: "/login" }); return; }
                  setCheckoutError(null);
                  buy.mutate();
                }}
              >
                {buy.isPending ? "Processing..." : isAuthenticated ? "Enroll now" : "Sign in to enroll"}
              </Button>
              <p className="text-center text-xs text-muted-foreground">Manual payment provider (test). Real gateways can be added.</p>
            </CardContent>
          </Card>
        </aside>
      </div>
    </div>
  );
}
