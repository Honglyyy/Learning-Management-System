import {
  createFileRoute,
  useNavigate,
  useParams,
} from "@tanstack/react-router";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import { api, mediaUrl } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { SiteHeader } from "@/components/SiteHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ApiAlert } from "@/components/ApiAlert";
import {
  Clock,
  User,
  Star,
  Play,
  PlayCircle,
  ListChecks,
  Layers,
  CheckCircle,
  Lock,
  ChevronLeft,
  ChevronRight,
  FileText,
  Download,
  Check,
  Paperclip,
  CreditCard,
  ExternalLink,
  Heart,
} from "lucide-react";
import { useState, useRef, useEffect } from "react";
import { toast } from "sonner";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";

export const Route = createFileRoute("/courses/$id")({
  component: CourseDetail,
});

function CourseDetail() {
  const { id } = useParams({ from: "/courses/$id" });
  const nav = useNavigate();
  const { isAuthenticated, role, email, logout } = useAuth();

  const [checkoutError, setCheckoutError] = useState<unknown>(null);
  const [activeLesson, setActiveLesson] = useState<any>(null);
  const [checkoutPendingModal, setCheckoutPendingModal] = useState<any>(null);

  const [reviewText, setReviewText] = useState("");
  const [rating, setRating] = useState(5);

  const qc = useQueryClient();

  const submitReview = useMutation({
    mutationFn: async () =>
        api(`/api/courses/${id}/review`, {
          method: "POST",
          body: {
            rating,
            reviewText
          },
          auth: true
        }),

    onSuccess: () => {
      toast.success("Review submitted");
      setReviewText("");
      qc.invalidateQueries({ queryKey: ["reviews", id] });
      qc.invalidateQueries({ queryKey: ["course", id] });
    },

    onError: () => toast.error("Failed to submit review"),
  });

  // COURSE
  const course = useQuery<any>({
    queryKey: ["course", id],
    queryFn: () => api(`/api/courses/${id}`),
  });

  // ENROLLMENTS
  const enrollments = useQuery<any[]>({
    queryKey: ["my-enrollments"],
    queryFn: () => api("/api/enrollments/me", { auth: true }).catch(() => []),
    enabled: isAuthenticated,
  });

  // PAYMENTS
  const myPayments = useQuery<any[]>({
    queryKey: ["my-payments"],
    queryFn: () => api("/api/payments/me", { auth: true }).catch(() => []),
    enabled: isAuthenticated,
  });

  const pendingPayment = myPayments.data?.find(
    (p: any) => String(p.courseId) === String(id) && p.status === "PENDING"
  );

  const reviews = useQuery({
    queryKey: ["reviews", id],
    enabled: !!id,
    queryFn: () => api(`/api/courses/${id}/reviews`, { auth: false }),
  });

  const isEnrolled =
      enrollments.data?.some(
          (e: any) =>
              String(e.course?.courseId ?? e.courseId) === String(id) &&
              e.status === "ACTIVE"
      ) || false;

  const lessonsStatusQuery = useQuery<any[]>({
    queryKey: ["lessons-status", id],
    queryFn: () => api(`/api/courses/${id}/lessons-status`, { auth: true }).catch(() => []),
    enabled: isAuthenticated && isEnrolled,
  });

  const favCheckQuery = useQuery({
    queryKey: ["favorite-check", id],
    queryFn: () => api<{ isFavorite: boolean }>(`/api/favorites/check/${id}`),
    enabled: isAuthenticated && !!id,
  });
  const isFav = favCheckQuery.data?.isFavorite ?? course.data?.isFavorite ?? false;

  const toggleFavorite = useMutation({
    mutationFn: async () => {
      if (isFav) {
        await api(`/api/favorites/${id}`, { method: "DELETE" });
      } else {
        await api(`/api/favorites/${id}`, { method: "POST" });
      }
    },
    onSuccess: () => {
      toast.success(isFav ? "Course removed from wishlist" : "Course saved to wishlist");
      qc.invalidateQueries({ queryKey: ["favorite-check", id] });
      qc.invalidateQueries({ queryKey: ["my-favorites"] });
      qc.invalidateQueries({ queryKey: ["course", id] });
      qc.invalidateQueries({ queryKey: ["courses"] });
    },
    onError: (e: any) => toast.error(e.message || "Failed to update wishlist"),
  });

  const toggleStatus = useMutation({
    mutationFn: (newStatus: string) =>
      api(`/api/courses/${id}/status`, {
        method: "PATCH",
        body: { status: newStatus },
      }),
    onSuccess: (updated: any) => {
      toast.success(`Course status updated to ${updated.status}`);
      qc.invalidateQueries({ queryKey: ["course", id] });
      qc.invalidateQueries({ queryKey: ["courses"] });
    },
    onError: (e: any) => toast.error(e.message || "Failed to change status"),
  });

  const [selectedAssignment, setSelectedAssignment] = useState<any>(null);

  const assignmentsQuery = useQuery<any[]>({
    queryKey: ["course-assignments", id],
    queryFn: () => api(`/api/courses/${id}/assignments`, { auth: true }).catch(() => []),
    enabled: isAuthenticated && isEnrolled,
  });
  const assignments = assignmentsQuery.data || [];

  const getLessonStatus = (lesson: any) => {
    if (lesson?.isFree) return "INCOMPLETE";
    if (!isEnrolled) return "LOCKED";
    const found = lessonsStatusQuery.data?.find((s: any) => s.lessonId === lesson?.lessonId);
    return found?.status || "INCOMPLETE";
  };

  const findLessonById = (lessonId: number) => {
    for (const sec of course.data?.sections || []) {
      for (const les of sec.lessons || []) {
        if (les.lessonId === lessonId) return les;
      }
    }
    return { lessonId, title: `Lesson ${lessonId}` };
  };

  // ENROLL VIA ABA PAYWAY
  const buy = useMutation({
    mutationFn: async () => {
      const isFreeCourse = Number(course.data?.price ?? 0) === 0;

      const checkout = await api<any>(`/api/payments/checkout`, {
        method: "POST",
        body: { courseId: Number(id), provider: "ABA_PAYWAY" },
        auth: true,
      });

      if (isFreeCourse) {
        // Free course can be confirmed immediately
        await api(`/api/payments/${checkout.paymentId}/confirm`, {
          method: "POST",
          auth: true,
        });
        return { ...checkout, isFreeCourse: true };
      }

      return { ...checkout, isFreeCourse: false };
    },
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ["my-enrollments"] });
      qc.invalidateQueries({ queryKey: ["my-payments"] });

      if (res.isFreeCourse) {
        toast.success("You are now enrolled in this free course!");
        nav({ to: "/my/enrollments" });
      } else {
        if (res.paymentUrl) {
          window.open(res.paymentUrl, "_blank", "noopener,noreferrer");
        }
        setCheckoutPendingModal(res);
      }
    },
    onError: (e) => setCheckoutError(e),
  });

  if (course.isLoading) {
    return (
        <div className="min-h-screen bg-background">
          <SiteHeader />
          <div className="container mx-auto px-4 py-10">
            <Skeleton className="h-64 w-full" />
          </div>
        </div>
    );
  }

  if (course.error || !course.data) {
    const isAuthError = (course.error as any)?.status === 401;
    return (
        <div className="min-h-screen bg-background">
          <SiteHeader />
          <div className="container mx-auto px-4 py-10 max-w-lg text-center">
            {isAuthError && !isAuthenticated ? (
              <>
                <h1 className="text-xl font-semibold mb-2">Please log in to continue</h1>
                <p className="text-sm text-muted-foreground mb-4">You need to sign in to access this page.</p>
                <div className="flex gap-4 justify-center">
                  <Button variant="ghost" size="sm" onClick={() => nav({ to: "/login" })}>Sign in</Button>
                  <Button size="sm" onClick={() => nav({ to: "/register" })}>Get started</Button>
                </div>
              </>
            ) : (
              <>
                <ApiAlert error={course.error || "Course not found"} />
                <div className="mt-4 flex justify-center gap-3">
                  <Button variant="outline" size="sm" onClick={() => nav({ to: "/" })}>
                    Back to courses
                  </Button>
                  <Button size="sm" onClick={() => course.refetch()}>
                    Retry
                  </Button>
                </div>
              </>
            )}
          </div>
        </div>
    );
  }

  const c = course.data;
  const reviewList = reviews.data ?? c?.reviews ?? [];

  const inst =
      typeof c.instructor === "object"
          ? c.instructor?.username || c.instructor?.email
          : c.instructor;

  const cats = c.categories || [];

  // @ts-ignore
  return (
      <div className="min-h-screen bg-background">
        <SiteHeader />

        <div className="container mx-auto grid gap-8 px-4 py-10 lg:grid-cols-3">
          {/* LEFT */}
          <div className="lg:col-span-2">

            {/* Categories, Level & Status */}
            <div className="mb-4 flex flex-wrap items-center gap-2">
              {cats.map((cat: string) => (
                  <Badge key={cat} variant="secondary">
                    {cat}
                  </Badge>
              ))}
              <Badge variant="outline" className="font-semibold text-primary">
                {c.level || "ALL LEVELS"}
              </Badge>
              {c.status && (
                <Badge variant={c.status === "PUBLISHED" ? "default" : "secondary"}>
                  {c.status}
                </Badge>
              )}
              {(role === "ADMIN" || role === "INSTRUCTOR") && (
                <Button
                  variant="outline"
                  size="sm"
                  className="h-6 text-xs px-2"
                  disabled={toggleStatus.isPending}
                  onClick={() =>
                    toggleStatus.mutate(c.status === "PUBLISHED" ? "DRAFT" : "PUBLISHED")
                  }
                >
                  {c.status === "PUBLISHED" ? "Unpublish (Draft)" : "Publish Course"}
                </Button>
              )}
            </div>

            {/* Title */}
            <h1 className="text-3xl font-semibold">{c.title}</h1>

            <p className="mt-2 text-muted-foreground">{c.description}</p>

            {/* Meta */}
            <div className="mt-4 flex flex-wrap gap-4 text-sm text-muted-foreground">
              {inst && (
                  <span className="flex items-center gap-1">
                <User className="h-4 w-4" />
                    {inst}
              </span>
              )}

              {c.overallDuration && (
                  <span className="flex items-center gap-1">
                <Clock className="h-4 w-4" />
                    {c.overallDuration}
              </span>
              )}

              {(c.sectionCount != null || c.sections?.length > 0) && (
                  <span className="flex items-center gap-1">
                <Layers className="h-4 w-4" />
                    {c.sectionCount ?? c.sections?.length} {Number(c.sectionCount ?? c.sections?.length) === 1 ? "Section" : "Sections"}
              </span>
              )}

              {typeof c.rating === "number" && (
                  <span className="flex items-center gap-1">
                <Star className="h-4 w-4 fill-current text-amber-500" />
                    {c.rating.toFixed(1)}
              </span>
              )}
            </div>

            {/* Cover */}
            {(c.coverUrl || c.coverDir) && (
                <img
                    src={mediaUrl(c.coverUrl || c.coverDir)}
                    alt={c.title}
                    className="mt-6 rounded-lg object-cover max-h-72 w-full shadow-sm"
                />
            )}

            {/* ================= LEARNING OUTCOMES & REQUIREMENTS ================= */}
            {c.learningOutcomes && (
              <div className="mt-8 rounded-lg border border-border bg-card p-5">
                <h3 className="font-semibold text-lg flex items-center gap-2 mb-3 text-foreground">
                  <CheckCircle className="h-5 w-5 text-emerald-600" /> What you'll learn
                </h3>
                <div className="text-sm text-foreground/90 whitespace-pre-line leading-relaxed">
                  {c.learningOutcomes}
                </div>
              </div>
            )}

            {c.requirements && (
              <div className="mt-4 rounded-lg border border-border bg-card p-5">
                <h3 className="font-semibold text-lg flex items-center gap-2 mb-3 text-foreground">
                  <ListChecks className="h-5 w-5 text-primary" /> Requirements & Prerequisites
                </h3>
                <div className="text-sm text-foreground/90 whitespace-pre-line leading-relaxed">
                  {c.requirements}
                </div>
              </div>
            )}

            {/* ================= CURRICULUM ================= */}
            <h2 className="mt-10 mb-3 text-xl font-semibold">
              Curriculum
            </h2>

            {c.sections?.length > 0 ? (
                <Accordion type="multiple" className="space-y-3">

                  {c.sections.map((section: any) => (
                      <AccordionItem
                          key={section.sectionId}
                          value={`section-${section.sectionId}`}
                          className="border rounded-lg bg-card"
                      >

                        {/* SECTION HEADER */}
                        <AccordionTrigger className="px-4 py-3">
                          <div className="flex justify-between w-full pr-4">
                            <div className="text-left">
                              <p className="font-semibold">{section.title}</p>
                              <p className="text-xs text-muted-foreground">
                                {section.duration}
                              </p>
                            </div>

                            <Badge variant="secondary">
                              {section.lessonCount} lessons
                            </Badge>
                          </div>
                        </AccordionTrigger>

                        {/* LESSONS */}
                        <AccordionContent className="px-4 pb-4">
                          <div className="divide-y">

                            {section.lessons?.map((lesson: any) => {
                              const status = getLessonStatus(lesson);
                              const isFree = Boolean(lesson.isFree);
                              const isLocked = isEnrolled ? status === "LOCKED" : !isFree;
                              const isCompleted = isEnrolled && status === "COMPLETED";

                              return (
                                <div
                                    key={lesson.lessonId}
                                    className="flex items-center justify-between py-3"
                                >
                                  <div className="flex items-center gap-2.5 min-w-0 pr-2">
                                    {isCompleted ? (
                                      <CheckCircle className="h-4 w-4 shrink-0 text-emerald-500" />
                                    ) : isLocked ? (
                                      <Lock className="h-4 w-4 shrink-0 text-muted-foreground" />
                                    ) : (
                                      <PlayCircle className="h-4 w-4 shrink-0 text-primary" />
                                    )}
                                    <div className="flex items-center gap-2 min-w-0">
                                      <span className={`text-sm font-medium line-clamp-1 ${isLocked ? "text-muted-foreground" : ""}`}>
                                        {lesson.title}
                                      </span>
                                      {lesson.duration && (
                                        <span className="text-xs text-muted-foreground shrink-0">
                                          ({lesson.duration})
                                        </span>
                                      )}
                                      {isFree && (
                                        <Badge variant="outline" className="text-[10px] px-1.5 py-0 bg-emerald-50 text-emerald-700 border-emerald-300 shrink-0 font-medium">
                                          Free Trial
                                        </Badge>
                                      )}
                                      {isCompleted && (
                                        <Badge variant="outline" className="text-[10px] px-1.5 py-0 bg-emerald-50 text-emerald-700 border-emerald-300 shrink-0">
                                          Completed
                                        </Badge>
                                      )}
                                    </div>
                                  </div>

                                  {isEnrolled ? (
                                      <div className="flex items-center gap-2 shrink-0">
                                        {isLocked ? (
                                          <Button size="sm" variant="outline" disabled className="gap-1 opacity-60">
                                            <Lock className="h-3.5 w-3.5" /> Locked
                                          </Button>
                                        ) : (
                                          <Button
                                              size="sm"
                                              variant={isCompleted ? "outline" : "default"}
                                              onClick={() => setActiveLesson(lesson)}
                                          >
                                            {isCompleted ? "Review" : "Play"}
                                          </Button>
                                        )}
                                        <Button
                                            size="sm"
                                            variant="outline"
                                            className="gap-1"
                                            disabled={isLocked}
                                            onClick={() =>
                                                nav({
                                                  to: "/lessons/$lessonId/quiz",
                                                  params: { lessonId: String(lesson.lessonId) },
                                                })
                                            }
                                        >
                                          <ListChecks className="h-4 w-4" />
                                          Quiz
                                        </Button>
                                      </div>
                                  ) : isFree ? (
                                      <div className="flex items-center gap-2 shrink-0">
                                        <Button
                                          size="sm"
                                          variant="default"
                                          className="bg-emerald-600 hover:bg-emerald-700 text-white gap-1 font-medium"
                                          onClick={() => setActiveLesson(lesson)}
                                        >
                                          <Play className="h-3.5 w-3.5 fill-white" />
                                          Free Preview
                                        </Button>
                                      </div>
                                  ) : (
                                      <Button size="sm" disabled>
                                        Enroll to watch
                                      </Button>
                                  )}
                                </div>
                              );
                            })}

                          </div>
                        </AccordionContent>

                      </AccordionItem>
                  ))}

                </Accordion>
            ) : (
                <p className="text-sm text-muted-foreground">
                  No sections yet.
                </p>
            )}

            {/* ================= ASSIGNMENTS ================= */}
            {isEnrolled && (
              <div className="mt-10">
                <div className="flex items-center justify-between mb-3">
                  <h2 className="text-xl font-semibold flex items-center gap-2">
                    <FileText className="h-5 w-5 text-primary" /> Course Assignments ({assignments.length})
                  </h2>
                </div>

                {assignments.length > 0 ? (
                  <div className="grid gap-3 sm:grid-cols-2">
                    {assignments.map((assign: any) => {
                      const isPastDue = assign.dueDate && new Date(assign.dueDate).getTime() < Date.now();
                      return (
                        <Card
                          key={assign.assignmentId}
                          className="hover:border-primary/50 transition-colors cursor-pointer"
                          onClick={() => setSelectedAssignment(assign)}
                        >
                          <CardHeader className="p-4 pb-2">
                            <div className="flex items-start justify-between gap-2">
                              <CardTitle className="text-base line-clamp-1">{assign.title}</CardTitle>
                              <Badge variant={isPastDue ? "destructive" : "secondary"} className="shrink-0 text-[10px]">
                                {assign.dueDate ? new Date(assign.dueDate).toLocaleDateString() : "No Due Date"}
                              </Badge>
                            </div>
                          </CardHeader>
                          <CardContent className="p-4 pt-1 space-y-2">
                            <p className="text-xs text-muted-foreground line-clamp-2">
                              {assign.description || "No description provided."}
                            </p>
                            <div className="flex items-center justify-between text-xs text-muted-foreground pt-1">
                              <span>Max: {assign.maxScore} pts</span>
                              <Button size="sm" variant="ghost" className="h-7 text-xs text-primary px-2">
                                View & Submit &rarr;
                              </Button>
                            </div>
                          </CardContent>
                        </Card>
                      );
                    })}
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground">No assignments for this course yet.</p>
                )}
              </div>
            )}

            {selectedAssignment && (
              <StudentAssignmentModal
                assignment={selectedAssignment}
                isEnrolled={isEnrolled}
                onClose={() => setSelectedAssignment(null)}
              />
            )}

            {/* ================= LESSON PLAYER MODAL ================= */}
            {activeLesson && (
              <LessonPlayerModal
                courseId={id}
                lesson={activeLesson}
                isEnrolled={isEnrolled}
                onClose={() => setActiveLesson(null)}
                onSelectLesson={(targetId) => {
                  const target = findLessonById(targetId);
                  setActiveLesson(target);
                }}
              />
            )}
          </div>

          {/* RIGHT SIDEBAR */}
          <aside>
            <Card className="sticky top-20">
              <CardHeader>
                <CardTitle className="text-3xl">
                  ${Number(c.price ?? 0).toFixed(2)}
                </CardTitle>
              </CardHeader>

              <CardContent className="space-y-3">
                <ApiAlert error={checkoutError} />

                {isEnrolled ? (
                  <Button className="w-full" disabled>
                    Already Enrolled
                  </Button>
                ) : pendingPayment ? (
                  <div className="space-y-2.5">
                    <div className="rounded-md border border-amber-300 bg-amber-50 dark:bg-amber-950/40 p-3 text-xs text-amber-800 dark:text-amber-200">
                      <p className="font-semibold mb-1 flex items-center gap-1">
                        <Clock className="h-3.5 w-3.5 shrink-0" /> Payment Pending Approval
                      </p>
                      <p>Your payment via ABA Mobile is awaiting admin verification. Once confirmed, you will automatically be enrolled.</p>
                    </div>
                    {pendingPayment.paymentUrl && (
                      <Button
                        variant="outline"
                        size="sm"
                        className="w-full text-xs gap-1 border-primary text-primary"
                        onClick={() => window.open(pendingPayment.paymentUrl, "_blank", "noopener,noreferrer")}
                      >
                        <ExternalLink className="h-3.5 w-3.5" /> Re-open ABA PayWay
                      </Button>
                    )}
                    <Button
                      variant="ghost"
                      size="sm"
                      className="w-full text-xs text-muted-foreground"
                      onClick={() => nav({ to: "/my/payments" })}
                    >
                      View in My Payments
                    </Button>
                  </div>
                ) : (
                  <Button
                    className="w-full bg-[#005a87] hover:bg-[#00476a] text-white gap-1.5 font-medium"
                    disabled={buy.isPending}
                    onClick={() => {
                      if (!isAuthenticated) {
                        nav({ to: "/login" });
                        return;
                      }

                      setCheckoutError(null);
                      buy.mutate();
                    }}
                  >
                    <CreditCard className="h-4 w-4" />
                    {buy.isPending
                      ? "Generating Checkout..."
                      : Number(c.price ?? 0) === 0
                      ? "Enroll for Free"
                      : `Pay with ABA ($${Number(c.price ?? 0).toFixed(2)})`}
                  </Button>
                )}

                <p className="text-xs text-center text-muted-foreground">
                  {Number(c.price ?? 0) === 0
                    ? "Free course — instant enrollment"
                    : "Pay via ABA PayWay & enjoy full course upon admin confirmation"}
                </p>

                <Button
                  variant="outline"
                  className="w-full gap-2 text-sm border-border hover:bg-muted/80"
                  disabled={toggleFavorite.isPending}
                  onClick={() => {
                    if (!isAuthenticated) {
                      toast.info("Please sign in to save courses to your wishlist");
                      nav({ to: "/login" });
                      return;
                    }
                    toggleFavorite.mutate();
                  }}
                >
                  <Heart
                    className={`h-4 w-4 ${
                      isFav ? "fill-rose-500 text-rose-500" : "text-muted-foreground"
                    }`}
                  />
                  {isFav ? "Saved in Wishlist" : "Add to Wishlist"}
                </Button>
              </CardContent>
            </Card>

            {/* ================= REVIEWS ACCORDION ================= */}
            <Card className="mt-4">
              <CardContent className="p-4">
                <Accordion type="single" collapsible>
                  <AccordionItem value="reviews">

                    <AccordionTrigger>
                      ⭐ Reviews ({reviewList.length})
                    </AccordionTrigger>

                    <AccordionContent>
                      {reviews.isLoading && !c?.reviews ? (
                          <p className="text-sm text-muted-foreground">
                            Loading reviews...
                          </p>
                      ) : reviewList.length > 0 ? (
                          <div className="space-y-3">
                            {reviewList.map((r: any) => (
                                <div key={r.reviewId} className="rounded-md border p-3">

                                  <div className="text-sm font-medium flex items-center gap-2">
                                    ⭐ {r.rating}
                                  </div>

                                  <p className="text-sm text-muted-foreground mt-1">
                                    {r.reviewText}
                                  </p>

                                  <p className="text-xs text-muted-foreground mt-1">
                                    by {r.username}
                                  </p>

                                </div>
                            ))}
                          </div>
                      ) : (
                          <p className="text-sm text-muted-foreground">
                            No reviews yet.
                          </p>
                      )}
                    </AccordionContent>
                    {isEnrolled && (
                        <Card className="mt-4">
                          <CardContent className="p-4 space-y-3">

                            <h3 className="font-semibold">Write a review</h3>

                            {/* RATING */}
                            <select
                                value={rating}
                                onChange={(e) => setRating(Number(e.target.value))}
                                className="w-full rounded-md border p-2 text-sm"
                            >
                              {[5,4,3,2,1].map((r) => (
                                  <option key={r} value={r}>
                                    {r} Stars
                                  </option>
                              ))}
                            </select>

                            {/* COMMENT */}
                            <textarea
                                value={reviewText}
                                onChange={(e) => setReviewText(e.target.value)}
                                placeholder="Write your feedback..."
                                className="w-full rounded-md border p-2 text-sm"
                            />

                            {/* SUBMIT */}
                            <Button
                                className="w-full"
                                disabled={submitReview.isPending}
                                onClick={() => submitReview.mutate()}
                            >
                              Submit Review
                            </Button>

                          </CardContent>
                        </Card>
                    )}
                  </AccordionItem>
                </Accordion>
              </CardContent>
            </Card>
          </aside>
        </div>

        {/* ================= ABA PAYWAY CHECKOUT MODAL ================= */}
        {checkoutPendingModal && (
          <Dialog open={!!checkoutPendingModal} onOpenChange={() => setCheckoutPendingModal(null)}>
            <DialogContent className="sm:max-w-md">
              <DialogHeader>
                <DialogTitle className="flex items-center gap-2 text-lg">
                  <CreditCard className="h-5 w-5 text-primary" />
                  ABA PayWay Checkout
                </DialogTitle>
              </DialogHeader>

              <div className="space-y-4 py-2">
                <div className="rounded-lg border bg-muted/40 p-4 text-center space-y-1">
                  <p className="text-xs text-muted-foreground">Course</p>
                  <p className="font-semibold text-sm line-clamp-1">{c?.title}</p>
                  <p className="text-3xl font-bold text-primary mt-2">
                    ${Number(checkoutPendingModal.amount ?? c?.price ?? 0).toFixed(2)}
                  </p>
                </div>

                <div className="rounded-md border border-amber-200 bg-amber-50 dark:bg-amber-950/30 p-3.5 space-y-2 text-xs text-amber-900 dark:text-amber-200">
                  <p className="font-semibold text-sm">Next Steps:</p>
                  <ol className="list-decimal list-inside space-y-1 text-xs leading-relaxed">
                    <li>Open ABA Mobile using the button below to pay <strong>${Number(checkoutPendingModal.amount ?? c?.price ?? 0).toFixed(2)}</strong>.</li>
                    <li>The Admin will verify and confirm your payment.</li>
                    <li>Once confirmed, you will automatically have full access to the course!</li>
                  </ol>
                </div>

                {checkoutPendingModal.paymentUrl && (
                  <Button
                    className="w-full bg-[#005a87] hover:bg-[#00476a] text-white font-medium py-5 gap-2"
                    onClick={() => window.open(checkoutPendingModal.paymentUrl, "_blank", "noopener,noreferrer")}
                  >
                    <ExternalLink className="h-4 w-4" /> Open ABA Mobile / PayWay
                  </Button>
                )}

                <div className="flex gap-2 pt-1">
                  <Button
                    variant="outline"
                    className="w-full text-xs"
                    onClick={() => {
                      setCheckoutPendingModal(null);
                      nav({ to: "/my/payments" });
                    }}
                  >
                    View in My Payments
                  </Button>
                  <Button
                    variant="ghost"
                    className="w-full text-xs"
                    onClick={() => setCheckoutPendingModal(null)}
                  >
                    Close
                  </Button>
                </div>
              </div>
            </DialogContent>
          </Dialog>
        )}
      </div>
  );
}

function LessonPlayerModal({
  courseId,
  lesson,
  isEnrolled,
  onClose,
  onSelectLesson,
}: {
  courseId: string;
  lesson: any;
  isEnrolled: boolean;
  onClose: () => void;
  onSelectLesson: (lessonId: number) => void;
}) {
  const qc = useQueryClient();
  const videoRef = useRef<HTMLVideoElement>(null);
  const [lastSavedSecond, setLastSavedSecond] = useState<number>(0);

  const progressQuery = useQuery<any>({
    queryKey: ["lesson-progress", lesson.lessonId],
    queryFn: () => api(`/api/lessons/${lesson.lessonId}/progress`, { auth: true }).catch(() => null),
    enabled: isEnrolled,
  });

  const navigationQuery = useQuery<any>({
    queryKey: ["lesson-navigation", lesson.lessonId],
    queryFn: () => api(`/api/lessons/${lesson.lessonId}/navigation`, { auth: false }).catch(() => null),
  });

  const materialsQuery = useQuery<any[]>({
    queryKey: ["lesson-materials", lesson.lessonId],
    queryFn: () => api(`/api/lessons/${lesson.lessonId}/materials`, { auth: false }).catch(() => []),
  });

  const hasResumedRef = useRef(false);
  useEffect(() => {
    hasResumedRef.current = false;
  }, [lesson.lessonId]);

  const handleLoadedMetadata = () => {
    if (hasResumedRef.current) return;
    const resumeSeconds = progressQuery.data?.lastPlaybackPositionSeconds;
    if (videoRef.current && resumeSeconds && resumeSeconds > 1) {
      videoRef.current.currentTime = resumeSeconds;
      hasResumedRef.current = true;
    }
  };

  useEffect(() => {
    if (!hasResumedRef.current && videoRef.current && progressQuery.data?.lastPlaybackPositionSeconds > 1) {
      videoRef.current.currentTime = progressQuery.data.lastPlaybackPositionSeconds;
      hasResumedRef.current = true;
    }
  }, [progressQuery.data]);

  const savePlayback = async (seconds: number) => {
    if (!isEnrolled || seconds < 0) return;
    try {
      await api(`/api/lessons/${lesson.lessonId}/progress?seconds=${seconds.toFixed(1)}`, {
        method: "POST",
        auth: true,
      });
    } catch {
      // ignore
    }
  };

  const handleTimeUpdate = () => {
    if (!videoRef.current) return;
    const cur = Math.floor(videoRef.current.currentTime);
    if (Math.abs(cur - lastSavedSecond) >= 5) {
      setLastSavedSecond(cur);
      savePlayback(videoRef.current.currentTime);
    }
  };

  const handlePause = () => {
    if (videoRef.current) {
      savePlayback(videoRef.current.currentTime);
    }
  };

  const completeMutation = useMutation({
    mutationFn: () => api(`/api/lessons/${lesson.lessonId}/complete`, { method: "POST", auth: true }),
    onSuccess: () => {
      toast.success("Lesson marked as complete!");
      qc.invalidateQueries({ queryKey: ["lessons-status", courseId] });
      qc.invalidateQueries({ queryKey: ["lesson-progress", lesson.lessonId] });
      qc.invalidateQueries({ queryKey: ["course", courseId] });
    },
    onError: () => toast.error("Failed to mark lesson complete"),
  });

  const isCompleted = progressQuery.data?.isCompleted;

  const handleClose = () => {
    if (videoRef.current) {
      savePlayback(videoRef.current.currentTime);
    }
    onClose();
  };

  const handleNav = (targetLessonId: number) => {
    if (videoRef.current) {
      savePlayback(videoRef.current.currentTime);
    }
    onSelectLesson(targetLessonId);
  };

  const navData = navigationQuery.data;
  const materials = materialsQuery.data || [];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 overflow-y-auto">
      <div className="relative w-full max-w-4xl rounded-lg bg-background shadow-2xl overflow-hidden my-8">
        {/* Header */}
        <div className="flex items-center justify-between border-b px-6 py-4">
          <div className="flex items-center gap-3">
            <h3 className="font-semibold text-lg line-clamp-1">{lesson.title}</h3>
            {lesson.isFree && (
              <Badge variant="outline" className="bg-emerald-50 text-emerald-700 border-emerald-300 gap-1 text-xs font-medium">
                Free Trial
              </Badge>
            )}
            {isCompleted && (
              <Badge variant="outline" className="bg-emerald-50 text-emerald-700 border-emerald-300 gap-1 text-xs">
                <Check className="h-3 w-3" /> Completed
              </Badge>
            )}
          </div>
          <button
            onClick={handleClose}
            className="rounded-full p-1 text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
          >
            ✕
          </button>
        </div>

        {/* Free preview guest banner */}
        {!isEnrolled && (
          <div className="bg-emerald-600/10 border-b border-emerald-500/20 px-6 py-2.5 flex flex-wrap items-center justify-between gap-2 text-xs text-emerald-900 dark:text-emerald-300">
            <div className="flex items-center gap-1.5">
              <span className="font-semibold">Free Preview:</span>
              <span>You are previewing this lesson without enrolling.</span>
            </div>
            <span className="text-muted-foreground">Enroll in the full course to unlock all lessons, quizzes, and track progress.</span>
          </div>
        )}

        {/* Video Player */}
        <div className="bg-black">
          {lesson.videoUrl || lesson.videoDir ? (
            <video
              ref={videoRef}
              src={mediaUrl(lesson.videoUrl || lesson.videoDir)}
              controls
              autoPlay
              onLoadedMetadata={handleLoadedMetadata}
              onTimeUpdate={handleTimeUpdate}
              onPause={handlePause}
              className="w-full aspect-video max-h-[500px]"
            />
          ) : (
            <div className="flex h-64 items-center justify-center text-muted-foreground">
              No video available for this lesson
            </div>
          )}
        </div>

        {/* Lesson Controls Bar */}
        <div className="flex flex-wrap items-center justify-between gap-3 border-b bg-muted/30 px-6 py-3">
          {/* Navigation Controls */}
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              disabled={!navData?.hasPrevious}
              onClick={() => navData?.previousLessonId && handleNav(navData.previousLessonId)}
              className="gap-1"
            >
              <ChevronLeft className="h-4 w-4" /> Previous
            </Button>
            <Button
              size="sm"
              variant="outline"
              disabled={!navData?.hasNext}
              onClick={() => navData?.nextLessonId && handleNav(navData.nextLessonId)}
              className="gap-1"
            >
              Next <ChevronRight className="h-4 w-4" />
            </Button>
          </div>

          {/* Complete / Status button */}
          {isEnrolled ? (
            <Button
              size="sm"
              variant={isCompleted ? "secondary" : "default"}
              disabled={completeMutation.isPending || isCompleted}
              onClick={() => completeMutation.mutate()}
              className="gap-1.5"
            >
              <CheckCircle className="h-4 w-4" />
              {isCompleted ? "Completed" : completeMutation.isPending ? "Marking..." : "Mark as Complete"}
            </Button>
          ) : (
            <Badge variant="outline" className="bg-emerald-50 text-emerald-700 border-emerald-300 px-3 py-1.5 text-xs font-medium">
              Free Trial Video
            </Badge>
          )}
        </div>

        {/* Lesson Details & Materials Content */}
        <div className="p-6 space-y-6 max-h-72 overflow-y-auto">
          {/* Description & Text Content */}
          {(lesson.description || lesson.textContent) && (
            <div className="space-y-3">
              {lesson.description && (
                <div>
                  <h4 className="text-sm font-semibold text-foreground mb-1">About this lesson</h4>
                  <p className="text-sm text-muted-foreground whitespace-pre-wrap">{lesson.description}</p>
                </div>
              )}
              {lesson.textContent && (
                <div className="rounded-md border bg-muted/20 p-4">
                  <h4 className="text-sm font-semibold text-foreground mb-1">Notes & Instructions</h4>
                  <div className="text-sm text-muted-foreground whitespace-pre-wrap">{lesson.textContent}</div>
                </div>
              )}
            </div>
          )}

          {/* Learning Materials */}
          <div>
            <h4 className="text-sm font-semibold text-foreground mb-2 flex items-center gap-1.5">
              <Paperclip className="h-4 w-4 text-primary" /> Learning Materials ({materials.length})
            </h4>
            {materials.length > 0 ? (
              <div className="grid gap-2 sm:grid-cols-2">
                {materials.map((mat: any) => (
                  <div key={mat.materialId} className="flex items-center justify-between rounded-lg border p-3 bg-card hover:bg-muted/40 transition-colors">
                    <div className="flex items-center gap-2.5 min-w-0 pr-2">
                      <FileText className="h-5 w-5 shrink-0 text-primary" />
                      <div className="min-w-0">
                        <p className="text-sm font-medium line-clamp-1">{mat.title}</p>
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                          <Badge variant="secondary" className="text-[10px] px-1.5 py-0">{mat.fileType || "DOC"}</Badge>
                          {mat.fileSize && <span>{(mat.fileSize / 1024).toFixed(0)} KB</span>}
                        </div>
                      </div>
                    </div>
                    <Button size="sm" variant="ghost" asChild className="shrink-0">
                      <a href={mediaUrl(mat.fileUrl)} target="_blank" rel="noreferrer" download={mat.title}>
                        <Download className="h-4 w-4" />
                      </a>
                    </Button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-muted-foreground">No supplemental materials attached to this lesson.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function StudentAssignmentModal({
  assignment,
  isEnrolled,
  onClose,
}: {
  assignment: any;
  isEnrolled: boolean;
  onClose: () => void;
}) {
  const qc = useQueryClient();
  const [textSubmission, setTextSubmission] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const subQuery = useQuery<any>({
    queryKey: ["assignment-my-submission", assignment.assignmentId],
    queryFn: () => api(`/api/assignments/${assignment.assignmentId}/submission/me`, { auth: true }),
    enabled: isEnrolled,
  });

  const submission = subQuery.data;
  const isSubmitted = submission && submission.status && submission.status !== "NOT_SUBMITTED";
  const isGraded = submission && (submission.status === "GRADED" || (submission.score !== null && submission.score !== undefined));

  useEffect(() => {
    if (submission?.textSubmission) {
      setTextSubmission(submission.textSubmission);
    }
  }, [submission]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!textSubmission.trim() && !file && !submission?.fileUrl) {
      toast.error("Please enter text or upload a file");
      return;
    }

    setSubmitting(true);
    try {
      let fileUrl = submission?.fileUrl || null;
      let filePublicId = submission?.filePublicId || null;

      if (file) {
        setUploading(true);
        const formData = new FormData();
        formData.append("file", file);
        const res = await api<any>("/api/uploads/assignment-file", {
          method: "POST",
          formData,
          auth: true,
        });
        fileUrl = res.url;
        filePublicId = res.publicId;
        setUploading(false);
      }

      await api(`/api/assignments/${assignment.assignmentId}/submit`, {
        method: "POST",
        body: {
          textSubmission: textSubmission.trim() || null,
          fileUrl,
          filePublicId,
        },
        auth: true,
      });

      toast.success(isSubmitted ? "Assignment resubmitted successfully!" : "Assignment submitted successfully!");
      qc.invalidateQueries({ queryKey: ["assignment-my-submission", assignment.assignmentId] });
      qc.invalidateQueries({ queryKey: ["course-assignments"] });
    } catch (err: any) {
      toast.error(err.message || "Failed to submit assignment");
    } finally {
      setSubmitting(false);
      setUploading(false);
    }
  };

  const formatDate = (ts: any) => {
    if (!ts) return "No due date";
    return new Date(ts).toLocaleString();
  };

  const isPastDue = assignment.dueDate && new Date(assignment.dueDate).getTime() < Date.now();

  return (
    <Dialog open onOpenChange={() => onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <Badge variant="outline">{assignment.courseTitle || "Course Assignment"}</Badge>
            {assignment.dueDate && (
              <Badge variant={isPastDue ? "destructive" : "secondary"}>
                Due: {formatDate(assignment.dueDate)}
              </Badge>
            )}
          </div>
          <DialogTitle className="text-xl font-bold mt-1">{assignment.title}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-2">
          {assignment.description && (
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Description</p>
              <p className="text-sm mt-1 whitespace-pre-line">{assignment.description}</p>
            </div>
          )}

          {assignment.instructions && (
            <div className="rounded-md border p-3 bg-muted/40">
              <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Instructions</p>
              <p className="text-sm mt-1 whitespace-pre-line">{assignment.instructions}</p>
            </div>
          )}

          <div className="flex flex-wrap gap-4 text-xs text-muted-foreground">
            <div>
              <span className="font-medium text-foreground">Max Score:</span> {assignment.maxScore} pts
            </div>
            <div>
              <span className="font-medium text-foreground">Resubmissions:</span> {assignment.allowResubmission ? "Allowed" : "One-time only"}
            </div>
          </div>

          {assignment.supportingFileUrl && (
            <div className="flex items-center gap-2 pt-1">
              <Paperclip className="h-4 w-4 text-primary shrink-0" />
              <span className="text-xs font-medium">Assignment Attachment:</span>
              <a
                href={mediaUrl(assignment.supportingFileUrl)}
                target="_blank"
                rel="noreferrer"
                download
                className="text-xs text-primary underline font-medium hover:text-primary/80 flex items-center gap-1"
              >
                Download Document <ExternalLink className="h-3 w-3" />
              </a>
            </div>
          )}

          {/* Submission / Grade Status Banner */}
          {subQuery.isLoading ? (
            <Skeleton className="h-20 w-full" />
          ) : isSubmitted ? (
            <div className="rounded-lg border p-4 bg-card space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-sm font-semibold">Your Submission</span>
                <Badge
                  variant={
                    submission.status === "GRADED"
                      ? "default"
                      : submission.status === "LATE"
                      ? "destructive"
                      : "secondary"
                  }
                >
                  {submission.status}
                </Badge>
              </div>

              {submission.submittedAt && (
                <p className="text-xs text-muted-foreground">
                  Submitted on: {formatDate(submission.submittedAt)}
                </p>
              )}

              {submission.textSubmission && (
                <div className="text-xs mt-1">
                  <span className="font-medium text-muted-foreground">Submitted Text:</span>
                  <p className="mt-0.5 whitespace-pre-line bg-muted/30 p-2 rounded border">{submission.textSubmission}</p>
                </div>
              )}

              {submission.fileUrl && (
                <div className="text-xs pt-1">
                  <span className="font-medium">Submitted file:</span>{" "}
                  <a
                    href={mediaUrl(submission.fileUrl)}
                    target="_blank"
                    rel="noreferrer"
                    download
                    className="text-primary underline hover:text-primary/80 inline-flex items-center gap-1"
                  >
                    View / Download File <ExternalLink className="h-3 w-3" />
                  </a>
                </div>
              )}

              {submission.status === "GRADED" && (
                <div className="mt-3 pt-3 border-t space-y-1.5 bg-emerald-500/10 -mx-4 -mb-4 p-4 rounded-b-lg">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-bold text-emerald-700 dark:text-emerald-400">
                      Score: {submission.score} / {assignment.maxScore}
                    </span>
                    {submission.grade && (
                      <Badge className="text-sm font-bold bg-emerald-600">Grade: {submission.grade}</Badge>
                    )}
                  </div>
                  {submission.feedback && (
                    <div className="text-xs text-emerald-950 dark:text-emerald-100">
                      <span className="font-semibold">Instructor Feedback:</span>
                      <p className="mt-0.5 whitespace-pre-line">{submission.feedback}</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          ) : (
            <div className="rounded-md border border-dashed p-3 text-center text-xs text-muted-foreground">
              You have not submitted this assignment yet.
            </div>
          )}

          {/* Submission Form or Locked Notice */}
          {isGraded ? (
            <div className="rounded-lg border border-emerald-200 bg-emerald-50 dark:bg-emerald-950/20 p-4 text-center space-y-1.5 pt-3">
              <div className="flex items-center justify-center gap-1.5 text-sm font-semibold text-emerald-800 dark:text-emerald-300">
                <Lock className="h-4 w-4" /> Assignment Graded & Locked
              </div>
              <p className="text-xs text-muted-foreground">
                Your instructor has evaluated and graded this assignment. Further edits and resubmissions are closed.
              </p>
            </div>
          ) : (!isSubmitted || assignment.allowResubmission) ? (
            <form onSubmit={handleSubmit} className="space-y-3 pt-2 border-t">
              <h4 className="text-sm font-semibold">
                {isSubmitted ? "Resubmit Your Work" : "Submit Your Assignment"}
              </h4>

              <div>
                <Label htmlFor="text-sub" className="text-xs font-medium">
                  Text or Answer / Notes
                </Label>
                <Textarea
                  id="text-sub"
                  placeholder="Paste repository links, written answers, or submission notes here..."
                  rows={4}
                  value={textSubmission}
                  onChange={(e) => setTextSubmission(e.target.value)}
                  className="mt-1"
                />
              </div>

              <div>
                <Label htmlFor="file-sub" className="text-xs font-medium">
                  Upload File / Document / Code Archive
                </Label>
                <Input
                  id="file-sub"
                  type="file"
                  onChange={(e) => setFile(e.target.files?.[0] || null)}
                  className="mt-1"
                />
                <p className="text-[11px] text-muted-foreground mt-0.5">
                  PDF, DOCX, ZIP, images, code files up to 50MB
                </p>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="outline" size="sm" onClick={onClose}>
                  Close
                </Button>
                <Button type="submit" size="sm" disabled={submitting || uploading}>
                  {uploading ? "Uploading File..." : submitting ? "Submitting..." : isSubmitted ? "Resubmit Work" : "Submit Work"}
                </Button>
              </div>
            </form>
          ) : (
            <div className="rounded-lg border border-muted bg-muted/30 p-4 text-center space-y-1">
              <div className="flex items-center justify-center gap-1.5 text-xs font-medium text-muted-foreground">
                <Lock className="h-3.5 w-3.5" /> Resubmissions are not permitted for this assignment
              </div>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}

