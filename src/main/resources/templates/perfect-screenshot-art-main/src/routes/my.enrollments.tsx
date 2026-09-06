import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { SiteHeader } from "@/components/SiteHeader";
import { RequireAuth } from "@/components/RequireRole";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ApiAlert } from "@/components/ApiAlert";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { User, Calendar } from "lucide-react";

export const Route = createFileRoute("/my/enrollments")({
  head: () => ({ meta: [{ title: "My learning — Lumen LMS" }] }),
  component: () => <RequireAuth><Page /></RequireAuth>,
});

function Page() {
  const qc = useQueryClient();
  const { data, isLoading, error } = useQuery<any[]>({
    queryKey: ["my-enrollments"],
    queryFn: () => api("/api/enrollments/me"),
  });
  const cancel = useMutation({
    mutationFn: (courseId: number) => api(`/api/enrollments/me/courses/${courseId}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Enrollment cancelled."); qc.invalidateQueries({ queryKey: ["my-enrollments"] }); },
    onError: (e: any) => toast.error(e.message || "Failed to cancel"),
  });

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <div className="container mx-auto px-4 py-10">
        <h1 className="mb-6 text-2xl font-semibold text-foreground">My learning</h1>
        <ApiAlert error={error} />
        {isLoading ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {[1, 2, 3, 4].map(i => <Skeleton key={i} className="h-32" />)}
          </div>
        ) : !data || data.length === 0 ? (
          <p className="text-muted-foreground">You haven't enrolled in any courses yet.</p>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {data.map((e: any) => {
              const course = e.course;
              const courseId = course?.courseId ?? e.courseId;
              const title = course?.title || e.courseTitle || `Course #${courseId}`;
              const inst = e.instructor || course?.instructor?.username || e.instructorName;
              const enrolledDate = e.enrolledAt || e.createdAt;

              return (
                <Card key={e.enrollmentId || e.id || courseId} className="hover:shadow-md transition">
                  {/* HEADER */}
                  <CardHeader>
                    <CardTitle className="text-base line-clamp-1">
                      {title}
                    </CardTitle>
                  </CardHeader>

                  <CardContent>
                    {/* META */}
                    <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
                      {inst && (
                        <span className="flex items-center gap-1 font-medium text-foreground">
                          <User className="h-3.5 w-3.5 text-muted-foreground" /> {inst}
                        </span>
                      )}

                      <Badge variant={e.status === "ACTIVE" ? "default" : "secondary"}>
                        {e.status}
                      </Badge>

                      {enrolledDate && (
                        <span className="flex items-center gap-1 text-xs">
                          <Calendar className="h-3.5 w-3.5" />
                          Enrolled {new Date(enrolledDate).toLocaleDateString()}
                        </span>
                      )}
                    </div>

                    {/* ACTIONS */}
                    <div className="mt-4 flex items-center justify-between">
                      <Button
                        size="sm"
                        onClick={() => {
                          window.location.href = `/courses/${courseId}`;
                        }}
                      >
                        Go to course
                      </Button>

                      <Button
                        variant="outline"
                        size="sm"
                        disabled={cancel.isPending}
                        onClick={() => cancel.mutate(courseId)}
                      >
                        Cancel
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
