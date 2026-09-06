import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api, mediaUrl } from "@/lib/api";
import { SiteHeader } from "@/components/SiteHeader";
import { RequireAuth } from "@/components/RequireRole";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ApiAlert } from "@/components/ApiAlert";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { toast } from "sonner";
import { User, Calendar, Heart, BookmarkCheck, BookOpen, Clock, Star } from "lucide-react";

export const Route = createFileRoute("/my/enrollments")({
  head: () => ({ meta: [{ title: "My learning — Lumen LMS" }] }),
  component: () => <RequireAuth><Page /></RequireAuth>,
});

function Page() {
  const qc = useQueryClient();

  const enrollmentsQuery = useQuery<any[]>({
    queryKey: ["my-enrollments"],
    queryFn: () => api("/api/enrollments/me"),
  });

  const favoritesQuery = useQuery<any[]>({
    queryKey: ["my-favorites"],
    queryFn: () => api("/api/favorites"),
  });

  const cancelEnrollment = useMutation({
    mutationFn: (courseId: number) => api(`/api/enrollments/me/courses/${courseId}`, { method: "DELETE" }),
    onSuccess: () => {
      toast.success("Enrollment cancelled.");
      qc.invalidateQueries({ queryKey: ["my-enrollments"] });
    },
    onError: (e: any) => toast.error(e.message || "Failed to cancel"),
  });

  const removeFavorite = useMutation({
    mutationFn: (courseId: number) => api(`/api/favorites/${courseId}`, { method: "DELETE" }),
    onSuccess: () => {
      toast.success("Course removed from saved list.");
      qc.invalidateQueries({ queryKey: ["my-favorites"] });
      qc.invalidateQueries({ queryKey: ["courses"] });
    },
    onError: (e: any) => toast.error(e.message || "Failed to remove favorite"),
  });

  const enrollments = enrollmentsQuery.data || [];
  const inProgress = enrollments.filter((e) => e.status !== "COMPLETED");
  const completed = enrollments.filter((e) => e.status === "COMPLETED");
  const favorites = favoritesQuery.data || [];

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <div className="container mx-auto px-4 py-10">
        <div className="mb-6 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-foreground">My Learning & Courses</h1>
            <p className="text-sm text-muted-foreground">
              Track your enrolled courses, certifications, and saved favorites.
            </p>
          </div>
        </div>

        <ApiAlert error={enrollmentsQuery.error || favoritesQuery.error} />

        <Tabs defaultValue="in-progress" className="space-y-6">
          <TabsList className="grid w-full grid-cols-3 max-w-md">
            <TabsTrigger value="in-progress" className="flex items-center gap-1.5">
              <BookOpen className="h-4 w-4" />
              <span>In Progress ({inProgress.length})</span>
            </TabsTrigger>
            <TabsTrigger value="completed" className="flex items-center gap-1.5">
              <BookmarkCheck className="h-4 w-4" />
              <span>Completed ({completed.length})</span>
            </TabsTrigger>
            <TabsTrigger value="saved" className="flex items-center gap-1.5">
              <Heart className="h-4 w-4 text-rose-500" />
              <span>Saved ({favorites.length})</span>
            </TabsTrigger>
          </TabsList>

          {/* IN PROGRESS TAB */}
          <TabsContent value="in-progress">
            {enrollmentsQuery.isLoading ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {[1, 2, 3].map((i) => <Skeleton key={i} className="h-40" />)}
              </div>
            ) : inProgress.length === 0 ? (
              <div className="rounded-lg border border-dashed border-border p-10 text-center">
                <BookOpen className="mx-auto h-8 w-8 text-muted-foreground mb-3" />
                <h3 className="font-semibold text-foreground">No active courses</h3>
                <p className="text-sm text-muted-foreground mt-1 mb-4">
                  You don't have any in-progress courses right now.
                </p>
                <Button size="sm" onClick={() => (window.location.href = "/")}>
                  Explore Courses
                </Button>
              </div>
            ) : (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {inProgress.map((e: any) => renderEnrollmentCard(e, cancelEnrollment))}
              </div>
            )}
          </TabsContent>

          {/* COMPLETED TAB */}
          <TabsContent value="completed">
            {enrollmentsQuery.isLoading ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {[1, 2].map((i) => <Skeleton key={i} className="h-40" />)}
              </div>
            ) : completed.length === 0 ? (
              <div className="rounded-lg border border-dashed border-border p-10 text-center">
                <BookmarkCheck className="mx-auto h-8 w-8 text-muted-foreground mb-3" />
                <h3 className="font-semibold text-foreground">No completed courses yet</h3>
                <p className="text-sm text-muted-foreground mt-1">
                  Keep learning! Your completed courses and certificates will show up here.
                </p>
              </div>
            ) : (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {completed.map((e: any) => renderEnrollmentCard(e, cancelEnrollment))}
              </div>
            )}
          </TabsContent>

          {/* SAVED / FAVORITES TAB */}
          <TabsContent value="saved">
            {favoritesQuery.isLoading ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {[1, 2, 3].map((i) => <Skeleton key={i} className="h-40" />)}
              </div>
            ) : favorites.length === 0 ? (
              <div className="rounded-lg border border-dashed border-border p-10 text-center">
                <Heart className="mx-auto h-8 w-8 text-rose-400 mb-3" />
                <h3 className="font-semibold text-foreground">Your wishlist is empty</h3>
                <p className="text-sm text-muted-foreground mt-1 mb-4">
                  Save courses you're interested in by clicking the heart icon on any course card.
                </p>
                <Button size="sm" onClick={() => (window.location.href = "/")}>
                  Browse Courses
                </Button>
              </div>
            ) : (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {favorites.map((c: any) => (
                  <Card key={c.courseId} className="hover:shadow-md transition flex flex-col justify-between">
                    <div>
                      {c.coverUrl && (
                        <div className="h-36 w-full overflow-hidden rounded-t-lg bg-muted">
                          <img
                            src={mediaUrl(c.coverUrl)}
                            alt={c.title}
                            className="h-full w-full object-cover"
                          />
                        </div>
                      )}
                      <CardHeader className="pb-2">
                        <div className="flex items-center justify-between gap-2 mb-1">
                          <Badge variant="outline" className="text-xs">
                            {c.level || "ALL LEVELS"}
                          </Badge>
                          {c.rating != null && Number(c.rating) > 0 && (
                            <span className="flex items-center gap-1 text-xs font-medium text-amber-600">
                              <Star className="h-3 w-3 fill-amber-500 text-amber-500" />
                              {Number(c.rating).toFixed(1)}
                            </span>
                          )}
                        </div>
                        <CardTitle className="text-base line-clamp-1">{c.title}</CardTitle>
                      </CardHeader>
                      <CardContent className="pb-2 text-sm text-muted-foreground">
                        <div className="flex flex-wrap items-center gap-3">
                          {c.instructor && (
                            <span className="flex items-center gap-1">
                              <User className="h-3.5 w-3.5" /> {c.instructor}
                            </span>
                          )}
                          {c.overallDuration && (
                            <span className="flex items-center gap-1">
                              <Clock className="h-3.5 w-3.5" /> {c.overallDuration}
                            </span>
                          )}
                        </div>
                        <p className="mt-2 text-xs font-semibold text-foreground">
                          {!c.price || Number(c.price) === 0 ? "Free" : `$${Number(c.price).toFixed(2)}`}
                        </p>
                      </CardContent>
                    </div>
                    <div className="p-4 pt-0 flex items-center justify-between gap-2">
                      <Button
                        size="sm"
                        className="flex-1"
                        onClick={() => (window.location.href = `/courses/${c.courseId}`)}
                      >
                        View Course
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        className="text-rose-600 hover:text-rose-700 hover:bg-rose-50"
                        disabled={removeFavorite.isPending}
                        onClick={() => removeFavorite.mutate(c.courseId)}
                      >
                        <Heart className="h-4 w-4 fill-rose-500 mr-1" /> Remove
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

function renderEnrollmentCard(e: any, cancelMutation: any) {
  const course = e.course;
  const courseId = course?.courseId ?? e.courseId;
  const title = course?.title || e.courseTitle || `Course #${courseId}`;
  const inst = e.instructor || course?.instructor?.username || e.instructorName;
  const enrolledDate = e.enrolledAt || e.createdAt;

  return (
    <Card key={e.enrollmentId || e.id || courseId} className="hover:shadow-md transition">
      <CardHeader>
        <CardTitle className="text-base line-clamp-1">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
          {inst && (
            <span className="flex items-center gap-1 font-medium text-foreground">
              <User className="h-3.5 w-3.5 text-muted-foreground" /> {inst}
            </span>
          )}
          <Badge variant={e.status === "ACTIVE" ? "default" : "secondary"}>{e.status}</Badge>
          {enrolledDate && (
            <span className="flex items-center gap-1 text-xs">
              <Calendar className="h-3.5 w-3.5" />
              Enrolled {new Date(enrolledDate).toLocaleDateString()}
            </span>
          )}
        </div>
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
            disabled={cancelMutation.isPending}
            onClick={() => cancelMutation.mutate(courseId)}
          >
            Cancel
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
