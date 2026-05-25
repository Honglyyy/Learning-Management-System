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
import { Clock, User, Star, PlayCircle } from "lucide-react";
import { useState } from "react";
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
    },

    onError: () => toast.error("Failed to submit review"),
  });

  // COURSE
  const course = useQuery<any>({
    queryKey: ["course", id],
    queryFn: () => api(`/api/courses/${id}`, { auth: true }),
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
    queryFn: () => api(`/api/courses/${id}/reviews`, { auth: true }),
  });

  // FIXED ENROLLMENT CHECK
  const isEnrolled =
      enrollments.data?.some(
          (e: any) =>
              String(e.course?.courseId ?? e.courseId) === String(id) &&
              e.status === "ACTIVE"
      ) || false;

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
    return (
        <div className="min-h-screen bg-background">
          <SiteHeader />
          <div className="container mx-auto px-4 py-10">
            {/*<ApiAlert error={course.error || "Course not found"} />*/}
            <h1 className={"text-center text-xl"}>Please log in to continue</h1>
            <div className={"flex gap-4 justify-center mt-3"}>
              <Button variant="ghost" size="sm" onClick={() => nav({ to: "/login" })}>Sign in</Button>
              <Button size="sm" onClick={() => nav({ to: "/register" })}>Get started</Button>
            </div>
          </div>
        </div>
    );
  }

  const c = course.data;

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

              {typeof c.rating === "number" && (
                  <span className="flex items-center gap-1">
                <Star className="h-4 w-4 fill-current" />
                    {c.rating.toFixed(1)}
              </span>
              )}
            </div>

            {/* Cover */}
            {c.coverDir && (
                <img
                    src={mediaUrl(c.coverDir)}
                    className="mt-6 aspect-video w-full rounded-lg object-cover"
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

                            {section.lessons?.map((lesson: any) => (
                                <div
                                    key={lesson.lessonId}
                                    className="flex items-center justify-between py-3"
                                >
                                  <div className="flex items-center gap-2">
                                    <PlayCircle className="h-4 w-4 text-primary" />
                                    <span className="text-sm">
                    {lesson.title}
                  </span>
                                  </div>

                                  {isEnrolled ? (
                                      <Button
                                          size="sm"
                                          onClick={() => setActiveLesson(lesson)}
                                      >
                                        Play
                                      </Button>
                                  ) : (
                                      <Button size="sm" disabled>
                                        Enroll to watch
                                      </Button>
                                  )}
                                </div>
                            ))}

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

            {/* ================= VIDEO PLAYER ================= */}
            {activeLesson && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70">

                  {/* MODAL BOX */}
                  <div className="relative w-full max-w-4xl rounded-lg bg-background shadow-xl">

                    {/* CLOSE BUTTON */}
                    <button
                        onClick={() => setActiveLesson(null)}
                        className="absolute right-3 top-3 z-10 rounded-full bg-black/60 px-3 py-1 text-white hover:bg-black"
                    >
                      ✕
                    </button>

                    {/* TITLE */}
                    <div className="border-b p-4">
                      <h3 className="font-semibold">
                        {activeLesson.title}
                      </h3>
                    </div>

                    {/* VIDEO */}
                    <div className="p-4">
                      <video
                          src={mediaUrl(activeLesson.videoDir)}
                          controls
                          autoPlay
                          className="w-full rounded-md"
                      />
                    </div>

                  </div>
                </div>
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
                      ⭐ Reviews ({reviews.data?.length || 0})
                    </AccordionTrigger>

                    <AccordionContent>
                      {reviews.isLoading ? (
                          <p className="text-sm text-muted-foreground">
                            Loading reviews...
                          </p>
                      ) : (reviews.data?.length ?? 0) > 0 ? (
                          <div className="space-y-3">
                            {reviews.data?.map((r: any) => (
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