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
} from "lucide-react";
import { useState, useRef, useEffect } from "react";
import { toast } from "sonner";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";

export const Route = createFileRoute("/courses/$id")({
  component: CourseDetail,
});

function CourseDetail() {
  const { id } = useParams({ from: "/courses/$id" });
  const nav = useNavigate();
  const { isAuthenticated, role, email, logout } = useAuth();

  const [checkoutError, setCheckoutError] = useState<unknown>(null);
  const [activeLesson, setActiveLesson] = useState<any>(null);

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

  // ENROLL (payment flow kept same)
  const buy = useMutation({
    mutationFn: async () => {
      const checkout = await api<any>(`/api/payments/checkout`, {
        method: "POST",
        body: { courseId: Number(id), provider: "MANUAL" },
      });

      await api(`/api/payments/${checkout.paymentId}/confirm`, {
        method: "POST",
      });

      return checkout;
    },
    onSuccess: () => {
      toast.success("You are now enrolled!");
      nav({ to: "/my/enrollments" });
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

            {/* Categories */}
            <div className="mb-4 flex flex-wrap gap-2">
              {cats.map((cat: string) => (
                  <Badge key={cat} variant="secondary">
                    {cat}
                  </Badge>
              ))}
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

                <Button
                    className="w-full"
                    disabled={buy.isPending || isEnrolled}
                    onClick={() => {
                      if (!isAuthenticated) {
                        nav({ to: "/login" });
                        return;
                      }

                      setCheckoutError(null);
                      buy.mutate();
                    }}
                >
                  {isEnrolled
                      ? "Already Enrolled"
                      : buy.isPending
                          ? "Processing..."
                          : "Enroll Now"}
                </Button>

                <p className="text-xs text-center text-muted-foreground">
                  You must enroll to access lessons
                </p>
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
