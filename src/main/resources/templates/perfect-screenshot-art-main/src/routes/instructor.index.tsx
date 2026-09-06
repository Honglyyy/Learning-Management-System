import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";

import { api, mediaUrl } from "@/lib/api";

import { SiteHeader } from "@/components/SiteHeader";
import { RequireRole } from "@/components/RequireRole";
import { ApiAlert } from "@/components/ApiAlert";
import { MultiSelect } from "@/components/MultiSelect";

import {
    Card,
    CardContent,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";

import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";

import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";

import {
    Accordion,
    AccordionContent,
    AccordionItem,
    AccordionTrigger,
} from "@/components/ui/accordion";

import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";

import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";

import {
    Tabs,
    TabsContent,
    TabsList,
    TabsTrigger,
} from "@/components/ui/tabs";

import { toast } from "sonner";

import {
    Plus,
    Upload,
    FileVideo,
    ListChecks,
    HelpCircle,
    CheckSquare,
    Pencil,
    Trash2,
    Play,
    Users,
    Camera,
    User,
    Lock,
    Save,
    Star,
    BookOpen,
    Mail,
    Paperclip,
    FileText,
    Download,
    ExternalLink,
} from "lucide-react";

export const Route = createFileRoute("/instructor/")({
    head: () => ({
        meta: [{ title: "Instructor dashboard — Lumen LMS" }],
    }),
    component: () => (
        <RequireRole roles={["INSTRUCTOR", "ADMIN"]}>
            <Page />
        </RequireRole>
    ),
});

function Page() {
    return (
        <div className="min-h-screen bg-background">
            <SiteHeader />
            <div className="container mx-auto px-4 py-10">
                <h1 className="mb-6 text-3xl font-bold">Instructor Dashboard</h1>
                <Tabs defaultValue="courses">
                    <TabsList>
                        <TabsTrigger value="courses">My Courses</TabsTrigger>
                        <TabsTrigger value="assignments">Assignments & Grading</TabsTrigger>
                        <TabsTrigger value="enrollments">Enrollments</TabsTrigger>
                        <TabsTrigger value="profile">Profile & Bio</TabsTrigger>
                        <TabsTrigger value="security">Security</TabsTrigger>
                    </TabsList>
                    <TabsContent value="courses">
                        <MyCourses />
                    </TabsContent>
                    <TabsContent value="assignments">
                        <InstructorAssignmentsTab />
                    </TabsContent>
                    <TabsContent value="enrollments">
                        <InstructorEnrollments />
                    </TabsContent>
                    <TabsContent value="profile">
                        <InstructorProfileTab />
                    </TabsContent>
                    <TabsContent value="security">
                        <InstructorSecurityTab />
                    </TabsContent>
                </Tabs>
            </div>
        </div>
    );
}

// ---------------------------------------------------------------------------
// MyCourses
// ---------------------------------------------------------------------------

function MyCourses() {
    const qc = useQueryClient();

    const { data, isLoading, error } = useQuery<any[]>({
        queryKey: ["instructor-courses"],
        queryFn: () => api("/api/courses/instructor/me"),
    });

    const invalidate = () =>
        qc.invalidateQueries({ queryKey: ["instructor-courses"] });

    return (
        <div className="space-y-6">
            <div className="flex justify-end">
                <CreateCourseDialog onCreated={invalidate} />
            </div>

            <ApiAlert error={error} />

            {isLoading ? (
                <Skeleton className="h-40 w-full" />
            ) : !data || data.length === 0 ? (
                <p className="text-muted-foreground">
                    You haven't created any courses yet.
                </p>
            ) : (
                <div className="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
                    {data.map((c: any) => (
                        <CourseCard key={c.courseId} course={c} onMutated={invalidate} />
                    ))}
                </div>
            )}
        </div>
    );
}

// ---------------------------------------------------------------------------
// CourseCard
// ---------------------------------------------------------------------------

function CourseCard({
                        course,
                        onMutated,
                    }: {
    course: any;
    onMutated: () => void;
}) {
    const qc = useQueryClient();

    const invalidateSections = () =>
        qc.invalidateQueries({ queryKey: ["instructor-sections", course.courseId] });

    const { data: sections, isLoading: sectionsLoading } = useQuery<any[]>({
        queryKey: ["instructor-sections", course.courseId],
        queryFn: () =>
            api(`/api/sections/instructor/me?courseId=${course.courseId}`),
    });

    const deleteCourse = useMutation({
        mutationFn: () =>
            api(`/api/courses/instructor/me/${course.courseId}`, {
                method: "DELETE",
            }),
        onSuccess: () => {
            toast.success("Course deleted");
            onMutated();
        },
        onError: () => toast.error("Failed to delete course"),
    });

    const toggleStatus = useMutation({
        mutationFn: (newStatus: string) =>
            api(`/api/courses/${course.courseId}/status`, {
                method: "PATCH",
                body: { status: newStatus },
            }),
        onSuccess: (updated: any) => {
            toast.success(`Course status updated to ${updated.status}`);
            onMutated();
        },
        onError: (e: any) => toast.error(e.message || "Failed to update status"),
    });

    return (
        <Card className="overflow-hidden border shadow-sm flex flex-col">
            {(course.coverUrl || course.coverDir) && (
                <img
                    src={mediaUrl(course.coverUrl || course.coverDir)}
                    alt={course.title}
                    className="aspect-video w-full object-cover"
                />
            )}

            <CardHeader className="pb-2">
                <div className="flex items-start justify-between gap-2">
                    <div className="space-y-1 flex-1">
                        <CardTitle className="text-lg leading-snug">{course.title}</CardTitle>
                        <div className="flex flex-wrap items-center gap-1.5 mt-1">
                            <Badge variant={course.status === "PUBLISHED" ? "default" : "secondary"} className="text-[10px] px-1.5 py-0">
                                {course.status || "PUBLISHED"}
                            </Badge>
                            <Badge variant="outline" className="text-[10px] px-1.5 py-0">
                                {course.level ? course.level.replace("_", " ") : "ALL LEVELS"}
                            </Badge>
                            <Button
                                size="sm"
                                variant="outline"
                                className="h-5 text-[10px] px-1.5 py-0 font-normal"
                                disabled={toggleStatus.isPending}
                                onClick={() =>
                                    toggleStatus.mutate(course.status === "PUBLISHED" ? "DRAFT" : "PUBLISHED")
                                }
                            >
                                {course.status === "PUBLISHED" ? "Unpublish" : "Publish"}
                            </Button>
                        </div>
                        {course.categories && course.categories.length > 0 && (
                            <div className="flex flex-wrap gap-1 mt-1">
                                {course.categories.map((cat: any) => {
                                    const name = typeof cat === "string" ? cat : cat?.category;
                                    return name ? (
                                        <Badge key={name} variant="secondary" className="text-[10px] px-1.5 py-0">
                                            {name}
                                        </Badge>
                                    ) : null;
                                })}
                            </div>
                        )}
                    </div>
                    <div className="flex shrink-0 gap-1">
                        <EditCourseDialog course={course} onSaved={onMutated} />
                        <Button
                            size="icon"
                            variant="ghost"
                            className="h-7 w-7 text-destructive"
                            onClick={() => {
                                if (confirm("Delete this course?")) deleteCourse.mutate();
                            }}
                        >
                            <Trash2 className="h-4 w-4" />
                        </Button>
                    </div>
                </div>
            </CardHeader>

            <CardContent className="space-y-4 flex-1 flex flex-col">
                <p className="line-clamp-2 text-sm text-muted-foreground">
                    {course.description}
                </p>

                <div className="flex items-center justify-between text-sm">
                    <span className="font-semibold">
                        ${Number(course.price ?? 0).toFixed(2)}
                    </span>
                    <div className="flex items-center gap-3">
                        {course.rating != null && Number(course.rating) > 0 && (
                            <span className="flex items-center gap-1 text-xs font-medium text-amber-600">
                                <Star className="h-3.5 w-3.5 fill-amber-500 text-amber-500" />
                                {Number(course.rating).toFixed(1)}
                            </span>
                        )}
                        {course.overallDuration && (
                            <span className="text-muted-foreground">
                                {course.overallDuration}
                            </span>
                        )}
                    </div>
                </div>

                {/* ── Sections ─────────────────────────────────────────────────── */}
                <div className="flex-1 space-y-2">
                    <div className="flex items-center justify-between">
                        <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                            Sections
                        </p>
                        <CreateSectionDialog
                            courseId={course.courseId}
                            onSaved={invalidateSections}
                        />
                    </div>

                    {sectionsLoading ? (
                        <Skeleton className="h-16 w-full" />
                    ) : !sections || sections.length === 0 ? (
                        <p className="text-xs text-muted-foreground">No sections yet.</p>
                    ) : (
                        <Accordion type="multiple" className="w-full">
                            {sections.map((s: any) => (
                                <SectionItem
                                    key={s.sectionId}
                                    section={s}
                                    courseId={course.courseId}
                                    onSectionMutated={invalidateSections}
                                />
                            ))}
                        </Accordion>
                    )}
                </div>

                {/* ── Quiz / Question / Answer ──────────────────────────────────── */}
                <QuizContentManager courseId={course.courseId} />
            </CardContent>
        </Card>
    );
}

function QuizContentManager({ courseId }: { courseId: number }) {
    const qc = useQueryClient();

    const { data: lessons } = useQuery<any[]>({
        queryKey: ["instructor-lessons-all", courseId],
        queryFn: () => api(`/api/lessons/instructor/me?courseId=${courseId}`),
    });
    const { data: quizzes } = useQuery<any[]>({
        queryKey: ["instructor-quizzes", courseId],
        queryFn: () => api("/api/quizzes"),
    });
    const { data: questions } = useQuery<any[]>({
        queryKey: ["instructor-questions", courseId],
        queryFn: () => api("/api/questions"),
    });
    const { data: answers } = useQuery<any[]>({
        queryKey: ["instructor-answers", courseId],
        queryFn: () => api("/api/answers"),
    });

    const lessonIds = new Set((lessons ?? []).map((lesson) => lesson.lessonId));
    const courseQuizzes = (quizzes ?? []).filter((quiz) => lessonIds.has(quiz.lessonId));
    const quizIds = new Set(courseQuizzes.map((quiz) => quiz.quizId));
    const courseQuestions = (questions ?? []).filter((question) => quizIds.has(question.quizId));
    const questionIds = new Set(courseQuestions.map((question) => question.questionId));
    const courseAnswers = (answers ?? []).filter((answer) => questionIds.has(answer.questionId));

    const invalidateQuizContent = () => {
        qc.invalidateQueries({ queryKey: ["instructor-quizzes"] });
        qc.invalidateQueries({ queryKey: ["instructor-questions"] });
        qc.invalidateQueries({ queryKey: ["instructor-answers"] });
    };

    const deleteQuiz = useMutation({
        mutationFn: (quizId: number) => api(`/api/quizzes/${quizId}`, { method: "DELETE" }),
        onSuccess: () => {
            toast.success("Quiz deleted");
            invalidateQuizContent();
        },
        onError: () => toast.error("Failed to delete quiz"),
    });
    const deleteQuestion = useMutation({
        mutationFn: (questionId: number) => api(`/api/questions/${questionId}`, { method: "DELETE" }),
        onSuccess: () => {
            toast.success("Question deleted");
            invalidateQuizContent();
        },
        onError: () => toast.error("Failed to delete question"),
    });
    const deleteAnswer = useMutation({
        mutationFn: (answerId: number) => api(`/api/answers/${answerId}`, { method: "DELETE" }),
        onSuccess: () => {
            toast.success("Answer deleted");
            invalidateQuizContent();
        },
        onError: () => toast.error("Failed to delete answer"),
    });

    return (
        <div className="border-t pt-3">
            <Accordion type="single" collapsible>
                <AccordionItem value="quiz-content" className="border-none">
                    <AccordionTrigger className="py-2 text-sm font-semibold">
                        Manage quizzes, questions, answers
                    </AccordionTrigger>
                    <AccordionContent className="space-y-4">
                        <div className="grid gap-2 sm:grid-cols-3">
                            <QuizEditDialog
                                mode="create"
                                lessons={lessons ?? []}
                                onSaved={invalidateQuizContent}
                            />
                            <QuestionEditDialog
                                mode="create"
                                quizzes={courseQuizzes}
                                onSaved={invalidateQuizContent}
                            />
                            <AnswerEditDialog
                                mode="create"
                                questions={courseQuestions}
                                onSaved={invalidateQuizContent}
                            />
                        </div>

                        {courseQuizzes.length === 0 ? (
                            <p className="text-xs text-muted-foreground">
                                No quizzes yet. Create one for a lesson first.
                            </p>
                        ) : (
                            <div className="space-y-3">
                                {courseQuizzes.map((quiz) => {
                                    const quizQuestions = courseQuestions.filter(
                                        (question) => question.quizId === quiz.quizId,
                                    );

                                    return (
                                        <div key={quiz.quizId} className="rounded-lg border p-3">
                                            <div className="flex items-start justify-between gap-2">
                                                <div>
                                                    <p className="text-sm font-semibold">{quiz.title}</p>
                                                    <p className="text-xs text-muted-foreground">
                                                        {quiz.totalPoints} points ·{" "}
                                                        {lessons?.find((lesson) => lesson.lessonId === quiz.lessonId)
                                                            ?.title ?? "Lesson"}
                                                    </p>
                                                </div>
                                                <div className="flex gap-1">
                                                    <QuizEditDialog
                                                        mode="edit"
                                                        initial={quiz}
                                                        lessons={lessons ?? []}
                                                        onSaved={invalidateQuizContent}
                                                    />
                                                    <Button
                                                        size="icon"
                                                        variant="ghost"
                                                        className="h-7 w-7 text-destructive"
                                                        onClick={() => {
                                                            if (confirm("Delete this quiz?")) {
                                                                deleteQuiz.mutate(quiz.quizId);
                                                            }
                                                        }}
                                                    >
                                                        <Trash2 className="h-4 w-4" />
                                                    </Button>
                                                </div>
                                            </div>

                                            <div className="mt-3 space-y-2">
                                                {quizQuestions.length === 0 ? (
                                                    <p className="text-xs text-muted-foreground">
                                                        No questions yet.
                                                    </p>
                                                ) : (
                                                    quizQuestions.map((question) => {
                                                        const questionAnswers = courseAnswers.filter(
                                                            (answer) => answer.questionId === question.questionId,
                                                        );

                                                        return (
                                                            <div
                                                                key={question.questionId}
                                                                className="rounded-md bg-muted/40 p-2"
                                                            >
                                                                <div className="flex items-start justify-between gap-2">
                                                                    <div>
                                                                        <p className="text-xs font-medium">
                                                                            {question.questionText}
                                                                        </p>
                                                                        <p className="text-[11px] text-muted-foreground">
                                                                            {question.point ?? "Auto"} points
                                                                        </p>
                                                                    </div>
                                                                    <div className="flex gap-1">
                                                                        <QuestionEditDialog
                                                                            mode="edit"
                                                                            initial={question}
                                                                            quizzes={courseQuizzes}
                                                                            onSaved={invalidateQuizContent}
                                                                        />
                                                                        <Button
                                                                            size="icon"
                                                                            variant="ghost"
                                                                            className="h-7 w-7 text-destructive"
                                                                            onClick={() => {
                                                                                if (confirm("Delete this question?")) {
                                                                                    deleteQuestion.mutate(question.questionId);
                                                                                }
                                                                            }}
                                                                        >
                                                                            <Trash2 className="h-4 w-4" />
                                                                        </Button>
                                                                    </div>
                                                                </div>

                                                                <div className="mt-2 space-y-1">
                                                                    {questionAnswers.length === 0 ? (
                                                                        <p className="text-[11px] text-muted-foreground">
                                                                            No answers yet.
                                                                        </p>
                                                                    ) : (
                                                                        questionAnswers.map((answer) => (
                                                                            <div
                                                                                key={answer.answerId}
                                                                                className="flex items-center justify-between gap-2 rounded border bg-background px-2 py-1"
                                                                            >
                                                                                <span className="text-[11px]">
                                                                                    {answer.answerText}
                                                                                    {answer.isCorrect && (
                                                                                        <span className="ml-2 font-semibold text-green-700">
                                                                                            Correct
                                                                                        </span>
                                                                                    )}
                                                                                </span>
                                                                                <div className="flex gap-1">
                                                                                    <AnswerEditDialog
                                                                                        mode="edit"
                                                                                        initial={answer}
                                                                                        questions={courseQuestions}
                                                                                        onSaved={invalidateQuizContent}
                                                                                    />
                                                                                    <Button
                                                                                        size="icon"
                                                                                        variant="ghost"
                                                                                        className="h-6 w-6 text-destructive"
                                                                                        onClick={() => {
                                                                                            if (confirm("Delete this answer?")) {
                                                                                                deleteAnswer.mutate(answer.answerId);
                                                                                            }
                                                                                        }}
                                                                                    >
                                                                                        <Trash2 className="h-3.5 w-3.5" />
                                                                                    </Button>
                                                                                </div>
                                                                            </div>
                                                                        ))
                                                                    )}
                                                                </div>
                                                            </div>
                                                        );
                                                    })
                                                )}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </AccordionContent>
                </AccordionItem>
            </Accordion>
        </div>
    );
}

function QuizEditDialog({
                            mode,
                            initial,
                            lessons,
                            onSaved,
                        }: {
    mode: "create" | "edit";
    initial?: any;
    lessons: any[];
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [error, setError] = useState<any>(null);
    const [form, setForm] = useState<any>(
        initial ?? { title: "", totalPoints: 10, lessonId: "" },
    );

    const save = useMutation({
        mutationFn: () => {
            const body = {
                title: form.title,
                totalPoints: Number(form.totalPoints),
                lessonId: Number(form.lessonId),
            };
            return api(mode === "create" ? "/api/quizzes" : `/api/quizzes/${initial.quizId}`, {
                method: mode === "create" ? "POST" : "PUT",
                body,
            });
        },
        onSuccess: () => {
            toast.success(mode === "create" ? "Quiz created" : "Quiz updated");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog
            open={open}
            onOpenChange={(next) => {
                setOpen(next);
                if (next) {
                    setError(null);
                    setForm(initial ?? { title: "", totalPoints: 10, lessonId: "" });
                }
            }}
        >
            <DialogTrigger asChild>
                {mode === "create" ? (
                    <Button size="sm" variant="outline" className="w-full">
                        <ListChecks className="mr-1 h-3.5 w-3.5" />
                        New quiz
                    </Button>
                ) : (
                    <Button size="icon" variant="ghost" className="h-7 w-7">
                        <Pencil className="h-4 w-4" />
                    </Button>
                )}
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{mode === "create" ? "Create Quiz" : "Edit Quiz"}</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <ApiAlert error={error} />
                    <div className="space-y-1">
                        <Label>Title</Label>
                        <Input
                            value={form.title}
                            onChange={(e) => setForm({ ...form, title: e.target.value })}
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Total Points</Label>
                        <Input
                            type="number"
                            min={0}
                            value={form.totalPoints}
                            onChange={(e) => setForm({ ...form, totalPoints: e.target.value })}
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Lesson</Label>
                        <Select
                            value={String(form.lessonId)}
                            onValueChange={(v) => setForm({ ...form, lessonId: parseInt(v) })}
                        >
                            <SelectTrigger>
                                <SelectValue placeholder="Choose lesson" />
                            </SelectTrigger>
                            <SelectContent>
                                {lessons.map((lesson) => (
                                    <SelectItem key={lesson.lessonId} value={String(lesson.lessonId)}>
                                        {lesson.title}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                </div>
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function QuestionEditDialog({
                                mode,
                                initial,
                                quizzes,
                                onSaved,
                            }: {
    mode: "create" | "edit";
    initial?: any;
    quizzes: any[];
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [error, setError] = useState<any>(null);
    const [form, setForm] = useState<any>(
        initial ?? { questionText: "", point: "", quizId: "" },
    );

    const save = useMutation({
        mutationFn: () => {
            const body = {
                questionText: form.questionText,
                point: form.point === "" ? null : Number(form.point),
                quizId: Number(form.quizId),
            };
            return api(
                mode === "create" ? "/api/questions" : `/api/questions/${initial.questionId}`,
                {
                    method: mode === "create" ? "POST" : "PUT",
                    body,
                },
            );
        },
        onSuccess: () => {
            toast.success(mode === "create" ? "Question created" : "Question updated");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog
            open={open}
            onOpenChange={(next) => {
                setOpen(next);
                if (next) {
                    setError(null);
                    setForm(initial ?? { questionText: "", point: "", quizId: "" });
                }
            }}
        >
            <DialogTrigger asChild>
                {mode === "create" ? (
                    <Button size="sm" variant="outline" className="w-full">
                        <HelpCircle className="mr-1 h-3.5 w-3.5" />
                        New question
                    </Button>
                ) : (
                    <Button size="icon" variant="ghost" className="h-7 w-7">
                        <Pencil className="h-4 w-4" />
                    </Button>
                )}
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>
                        {mode === "create" ? "Create Question" : "Edit Question"}
                    </DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <ApiAlert error={error} />
                    <div className="space-y-1">
                        <Label>Question</Label>
                        <Textarea
                            value={form.questionText}
                            onChange={(e) => setForm({ ...form, questionText: e.target.value })}
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Points</Label>
                        <Input
                            type="number"
                            min={0}
                            placeholder="Auto split if empty"
                            value={form.point ?? ""}
                            onChange={(e) => setForm({ ...form, point: e.target.value })}
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Quiz</Label>
                        <Select
                            value={String(form.quizId)}
                            onValueChange={(v) => setForm({ ...form, quizId: parseInt(v) })}
                        >
                            <SelectTrigger>
                                <SelectValue placeholder="Choose quiz" />
                            </SelectTrigger>
                            <SelectContent>
                                {quizzes.map((quiz) => (
                                    <SelectItem key={quiz.quizId} value={String(quiz.quizId)}>
                                        {quiz.title}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                </div>
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function AnswerEditDialog({
                              mode,
                              initial,
                              questions,
                              onSaved,
                          }: {
    mode: "create" | "edit";
    initial?: any;
    questions: any[];
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [error, setError] = useState<any>(null);
    const [form, setForm] = useState<any>(
        initial ?? { answerText: "", isCorrect: false, questionId: "" },
    );

    const save = useMutation({
        mutationFn: () => {
            const body = {
                answerText: form.answerText,
                isCorrect: Boolean(form.isCorrect),
                questionId: Number(form.questionId),
            };
            return api(mode === "create" ? "/api/answers" : `/api/answers/${initial.answerId}`, {
                method: mode === "create" ? "POST" : "PUT",
                body,
            });
        },
        onSuccess: () => {
            toast.success(mode === "create" ? "Answer created" : "Answer updated");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog
            open={open}
            onOpenChange={(next) => {
                setOpen(next);
                if (next) {
                    setError(null);
                    setForm(initial ?? { answerText: "", isCorrect: false, questionId: "" });
                }
            }}
        >
            <DialogTrigger asChild>
                {mode === "create" ? (
                    <Button size="sm" variant="outline" className="w-full">
                        <CheckSquare className="mr-1 h-3.5 w-3.5" />
                        New answer
                    </Button>
                ) : (
                    <Button size="icon" variant="ghost" className="h-6 w-6">
                        <Pencil className="h-3.5 w-3.5" />
                    </Button>
                )}
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{mode === "create" ? "Create Answer" : "Edit Answer"}</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <ApiAlert error={error} />
                    <div className="space-y-1">
                        <Label>Answer</Label>
                        <Textarea
                            value={form.answerText}
                            onChange={(e) => setForm({ ...form, answerText: e.target.value })}
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Question</Label>
                        <Select
                            value={String(form.questionId)}
                            onValueChange={(v) => setForm({ ...form, questionId: parseInt(v) })}
                        >
                            <SelectTrigger>
                                <SelectValue placeholder="Choose question" />
                            </SelectTrigger>
                            <SelectContent>
                                {questions.map((question) => (
                                    <SelectItem
                                        key={question.questionId}
                                        value={String(question.questionId)}
                                    >
                                        {question.questionText}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="flex items-center gap-2">
                        <Checkbox
                            checked={Boolean(form.isCorrect)}
                            onCheckedChange={(checked) =>
                                setForm({ ...form, isCorrect: checked === true })
                            }
                        />
                        <Label>Correct answer</Label>
                    </div>
                </div>
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

// ---------------------------------------------------------------------------
// SectionItem
// ---------------------------------------------------------------------------

function SectionItem({
                         section,
                         courseId,
                         onSectionMutated,
                     }: {
    section: any;
    courseId: number;
    onSectionMutated: () => void;
}) {
    const qc = useQueryClient();

    const invalidateLessons = () =>
        qc.invalidateQueries({
            queryKey: ["instructor-lessons", section.sectionId],
        });

    const { data: lessons } = useQuery<any[]>({
        queryKey: ["instructor-lessons", section.sectionId],
        queryFn: () =>
            api(`/api/lessons/instructor/me?sectionId=${section.sectionId}`),
    });

    const deleteSection = useMutation({
        mutationFn: () =>
            api(`/api/sections/instructor/me/${section.sectionId}`, {
                method: "DELETE",
            }),
        onSuccess: () => {
            toast.success("Section deleted");
            onSectionMutated();
        },
        onError: () => toast.error("Failed to delete section"),
    });

    return (
        <AccordionItem
            value={String(section.sectionId)}
            className="border rounded-md px-3"
        >
            <AccordionTrigger className="py-2 hover:no-underline">
                <div className="flex w-full items-center justify-between pr-2">
                    <span className="text-sm font-medium">{section.title}</span>
                    <div className="flex items-center gap-1 text-muted-foreground text-xs">
                        {section.duration && <span>{section.duration}</span>}
                        <span className="ml-2">
              {lessons?.length ?? 0} lesson
                            {lessons?.length !== 1 ? "s" : ""}
            </span>
                    </div>
                </div>
            </AccordionTrigger>

            <AccordionContent className="pb-3 space-y-3">
                {/* Section actions */}
                <div className="flex gap-2">
                    <EditSectionDialog
                        section={section}
                        courseId={courseId}
                        onSaved={onSectionMutated}
                    />
                    <Button
                        size="sm"
                        variant="ghost"
                        className="text-destructive"
                        onClick={() => {
                            if (confirm("Delete this section and all its lessons?"))
                                deleteSection.mutate();
                        }}
                    >
                        <Trash2 className="mr-1 h-3.5 w-3.5" />
                        Delete section
                    </Button>
                </div>

                {/* Lessons list */}
                <div className="space-y-1">
                    {lessons && lessons.length > 0 ? (
                        lessons.map((l: any) => (
                            <LessonRow
                                key={l.lessonId}
                                lesson={l}
                                sectionId={section.sectionId}
                                onMutated={invalidateLessons}
                            />
                        ))
                    ) : (
                        <p className="text-xs text-muted-foreground">No lessons yet.</p>
                    )}
                </div>

                {/* Add lesson */}
                <CreateLessonDialog
                    sectionId={section.sectionId}
                    courseId={courseId}
                    onSaved={invalidateLessons}
                />
            </AccordionContent>
        </AccordionItem>
    );
}

// ---------------------------------------------------------------------------
// LessonRow — with video preview modal
// ---------------------------------------------------------------------------

function LessonRow({
                       lesson,
                       sectionId,
                       onMutated,
                   }: {
    lesson: any;
    sectionId: number;
    onMutated: () => void;
}) {
    const [videoOpen, setVideoOpen] = useState(false);

    const deleteLesson = useMutation({
        mutationFn: () =>
            api(`/api/lessons/instructor/me/${lesson.lessonId}`, {
                method: "DELETE",
            }),
        onSuccess: () => {
            toast.success("Lesson deleted");
            onMutated();
        },
        onError: () => toast.error("Failed to delete lesson"),
    });

    return (
        <>
            <div className="flex items-center justify-between rounded px-2 py-1 hover:bg-muted/50">
                <div className="flex items-center gap-2 text-sm">
                    <FileVideo className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
                    <span className="line-clamp-1">{lesson.title}</span>
                    {lesson.isFree && (
                        <Badge variant="outline" className="text-[10px] px-1.5 py-0 bg-emerald-50 text-emerald-700 border-emerald-300">
                            Free Trial
                        </Badge>
                    )}
                </div>

                <div className="flex shrink-0 gap-1">
                    {/* Play video */}
                    {lesson.videoDir && (
                        <Button
                            size="icon"
                            variant="ghost"
                            className="h-6 w-6"
                            onClick={() => setVideoOpen(true)}
                            title="Preview video"
                        >
                            <Play className="h-3.5 w-3.5" />
                        </Button>
                    )}

                    <ManageMaterialsDialog lesson={lesson} />

                    <EditLessonDialog
                        lesson={lesson}
                        sectionId={sectionId}
                        onSaved={onMutated}
                    />

                    <Button
                        size="icon"
                        variant="ghost"
                        className="h-6 w-6 text-destructive"
                        onClick={() => {
                            if (confirm("Delete this lesson?")) deleteLesson.mutate();
                        }}
                    >
                        <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                </div>
            </div>

            {/* Video preview modal */}
            {lesson.videoDir && (
                <Dialog open={videoOpen} onOpenChange={setVideoOpen}>
                    <DialogContent className="sm:max-w-3xl p-0 overflow-hidden">
                        <DialogHeader className="px-6 pt-5 pb-2">
                            <DialogTitle>{lesson.title}</DialogTitle>
                        </DialogHeader>
                        <div className="px-6 pb-6">
                            <video
                                src={mediaUrl(lesson.videoDir)}
                                controls
                                autoPlay
                                className="w-full rounded-md bg-black aspect-video"
                            />
                        </div>
                    </DialogContent>
                </Dialog>
            )}
        </>
    );
}

// ---------------------------------------------------------------------------
// Course CRUD dialogs
// ---------------------------------------------------------------------------

function CreateCourseDialog({ onCreated }: { onCreated: () => void }) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState<any>({
        title: "",
        description: "",
        price: 0,
        overallDuration: "",
        coverUrl: "",
        coverPublicId: "",
        coverDir: "",
        categoryId: [],
        level: "ALL_LEVELS",
        status: "PUBLISHED",
        learningOutcomes: "",
        requirements: "",
    });
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState<any>(null);

    const { data: categories } = useQuery<any[]>({
        queryKey: ["admin-categories"],
        queryFn: () => api("/api/categories"),
    });

    const catOptions =
        categories?.map((c) => ({
            id: c.categoryId,
            label: c.category?.toLowerCase(),
        })) || [];

    async function uploadCover(file: File) {
        setUploading(true);
        try {
            const fd = new FormData();
            fd.append("file", file);
            const res = await api<any>("/api/uploads/course-cover", {
                method: "POST",
                formData: fd,
            });
            setForm((f: any) => ({
                ...f,
                coverUrl: res.url,
                coverPublicId: res.publicId,
                coverDir: res.url,
            }));
            toast.success("Cover uploaded");
        } catch (e) {
            setError(e);
        } finally {
            setUploading(false);
        }
    }

    const create = useMutation({
        mutationFn: () =>
            api("/api/courses/instructor/me", {
                method: "POST",
                body: { ...form, instructor: null },
            }),
        onSuccess: () => {
            toast.success("Course created");
            setOpen(false);
            setForm({
                title: "",
                description: "",
                price: 0,
                overallDuration: "",
                coverUrl: "",
                coverPublicId: "",
                coverDir: "",
                categoryId: [],
            });
            onCreated();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    New Course
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-2xl">
                <DialogHeader>
                    <DialogTitle>Create Course</DialogTitle>
                </DialogHeader>
                <CourseFormFields
                    form={form}
                    setForm={setForm}
                    catOptions={catOptions}
                    uploading={uploading}
                    onUpload={uploadCover}
                    error={error}
                />
                <DialogFooter>
                    <Button onClick={() => create.mutate()} disabled={create.isPending}>
                        Create Course
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function EditCourseDialog({
                              course,
                              onSaved,
                          }: {
    course: any;
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState<any>({
        title: course.title,
        description: course.description,
        price: course.price,
        overallDuration: course.overallDuration ?? "",
        coverUrl: course.coverUrl ?? course.coverDir ?? "",
        coverPublicId: course.coverPublicId ?? "",
        coverDir: course.coverUrl ?? course.coverDir ?? "",
        categoryId: course.categoryId ?? course.categoryIds ?? [],
        level: course.level ?? "ALL_LEVELS",
        status: course.status ?? "PUBLISHED",
        learningOutcomes: course.learningOutcomes ?? "",
        requirements: course.requirements ?? "",
    });
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState<any>(null);

    const { data: categories } = useQuery<any[]>({
        queryKey: ["admin-categories"],
        queryFn: () => api("/api/categories"),
    });

    const catOptions =
        categories?.map((c) => ({
            id: c.categoryId,
            label: c.category?.toLowerCase(),
        })) || [];

    async function uploadCover(file: File) {
        setUploading(true);
        try {
            const fd = new FormData();
            fd.append("file", file);
            const res = await api<any>("/api/uploads/course-cover", {
                method: "POST",
                formData: fd,
            });
            setForm((f: any) => ({
                ...f,
                coverUrl: res.url,
                coverPublicId: res.publicId,
                coverDir: res.url,
            }));
            toast.success("Cover uploaded");
        } catch (e) {
            setError(e);
        } finally {
            setUploading(false);
        }
    }

    const update = useMutation({
        mutationFn: () =>
            api(`/api/courses/instructor/me/${course.courseId}`, {
                method: "PUT",
                body: { ...form, instructor: null },
            }),
        onSuccess: () => {
            toast.success("Course updated");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="icon" variant="ghost" className="h-7 w-7">
                    <Pencil className="h-4 w-4" />
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-2xl">
                <DialogHeader>
                    <DialogTitle>Edit Course</DialogTitle>
                </DialogHeader>
                <CourseFormFields
                    form={form}
                    setForm={setForm}
                    catOptions={catOptions}
                    uploading={uploading}
                    onUpload={uploadCover}
                    error={error}
                />
                <DialogFooter>
                    <Button onClick={() => update.mutate()} disabled={update.isPending}>
                        Save Changes
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function CourseFormFields({
                              form,
                              setForm,
                              catOptions,
                              uploading,
                              onUpload,
                              error,
                          }: any) {
    return (
        <div className="space-y-4">
            <ApiAlert error={error} />
            <div className="space-y-1">
                <Label>Title</Label>
                <Input
                    value={form.title}
                    onChange={(e) => setForm({ ...form, title: e.target.value })}
                />
            </div>
            <div className="space-y-1">
                <Label>Description</Label>
                <Textarea
                    value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
            </div>
            <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                    <Label>Price</Label>
                    <Input
                        type="number"
                        value={form.price}
                        onChange={(e) =>
                            setForm({ ...form, price: parseFloat(e.target.value) || 0 })
                        }
                    />
                </div>
                <div className="space-y-1">
                    <Label>Duration</Label>
                    <Input
                        value={form.overallDuration}
                        onChange={(e) =>
                            setForm({ ...form, overallDuration: e.target.value })
                        }
                    />
                </div>
            </div>
            <div className="space-y-1">
                <Label>Categories</Label>
                <MultiSelect
                    options={catOptions}
                    selected={form.categoryId}
                    onChange={(ids: any) => setForm({ ...form, categoryId: ids })}
                    placeholder="Select categories..."
                />
            </div>
            <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                    <Label>Level</Label>
                    <select
                        className="w-full h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm"
                        value={form.level || "ALL_LEVELS"}
                        onChange={(e) => setForm({ ...form, level: e.target.value })}
                    >
                        <option value="ALL_LEVELS">All Levels</option>
                        <option value="BEGINNER">Beginner</option>
                        <option value="INTERMEDIATE">Intermediate</option>
                        <option value="ADVANCED">Advanced</option>
                    </select>
                </div>
                <div className="space-y-1">
                    <Label>Status</Label>
                    <select
                        className="w-full h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm"
                        value={form.status || "PUBLISHED"}
                        onChange={(e) => setForm({ ...form, status: e.target.value })}
                    >
                        <option value="PUBLISHED">Published</option>
                        <option value="DRAFT">Draft</option>
                        <option value="ARCHIVED">Archived</option>
                    </select>
                </div>
            </div>
            <div className="space-y-1">
                <Label>Learning Outcomes</Label>
                <Textarea
                    placeholder="What students will learn (e.g. Master Spring Boot, REST APIs)..."
                    value={form.learningOutcomes || ""}
                    onChange={(e) => setForm({ ...form, learningOutcomes: e.target.value })}
                />
            </div>
            <div className="space-y-1">
                <Label>Requirements & Prerequisites</Label>
                <Textarea
                    placeholder="Prerequisites (e.g. Basic Java knowledge)..."
                    value={form.requirements || ""}
                    onChange={(e) => setForm({ ...form, requirements: e.target.value })}
                />
            </div>
            <div className="space-y-1">
                <Label>Cover Image</Label>
                <div className="flex items-center gap-2">
                    <Input
                        type="file"
                        accept="image/*"
                        onChange={(e) =>
                            e.target.files?.[0] && onUpload(e.target.files[0])
                        }
                    />
                    {uploading && <Upload className="h-4 w-4 animate-pulse" />}
                </div>
                {(form.coverUrl || form.coverDir) && (
                    <img
                        src={mediaUrl(form.coverUrl || form.coverDir)}
                        alt="Cover"
                        className="mt-2 h-20 rounded object-cover"
                    />
                )}
            </div>
        </div>
    );
}

// ---------------------------------------------------------------------------
// Section CRUD dialogs
// ---------------------------------------------------------------------------

function CreateSectionDialog({
                                 courseId,
                                 onSaved,
                             }: {
    courseId: number;
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState({ title: "", duration: "" });
    const [error, setError] = useState<any>(null);

    const save = useMutation({
        mutationFn: () =>
            api("/api/sections/instructor/me", {
                method: "POST",
                body: { ...form, courseId },
            }),
        onSuccess: () => {
            toast.success("Section created");
            setOpen(false);
            setForm({ title: "", duration: "" });
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="sm" variant="outline" className="h-7 text-xs">
                    <Plus className="mr-1 h-3 w-3" />
                    Add section
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-lg">
                <DialogHeader>
                    <DialogTitle>Create Section</DialogTitle>
                </DialogHeader>
                <SectionFormFields form={form} setForm={setForm} error={error} />
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function EditSectionDialog({
                               section,
                               courseId,
                               onSaved,
                           }: {
    section: any;
    courseId: number;
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState({
        title: section.title,
        duration: section.duration ?? "",
    });
    const [error, setError] = useState<any>(null);

    const save = useMutation({
        mutationFn: () =>
            api(`/api/sections/instructor/me/${section.sectionId}`, {
                method: "PUT",
                body: { ...form, courseId },
            }),
        onSuccess: () => {
            toast.success("Section updated");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="sm" variant="outline">
                    <Pencil className="mr-1 h-3.5 w-3.5" />
                    Edit section
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-lg">
                <DialogHeader>
                    <DialogTitle>Edit Section</DialogTitle>
                </DialogHeader>
                <SectionFormFields form={form} setForm={setForm} error={error} />
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save Changes
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function SectionFormFields({ form, setForm, error }: any) {
    return (
        <div className="space-y-4">
            <ApiAlert error={error} />
            <div className="space-y-1">
                <Label>Title</Label>
                <Input
                    value={form.title}
                    onChange={(e) => setForm({ ...form, title: e.target.value })}
                />
            </div>
            <div className="space-y-1">
                <Label>Duration</Label>
                <Input
                    value={form.duration}
                    onChange={(e) => setForm({ ...form, duration: e.target.value })}
                />
            </div>
        </div>
    );
}

// ---------------------------------------------------------------------------
// Lesson CRUD dialogs
// ---------------------------------------------------------------------------

function CreateLessonDialog({
                                sectionId,
                                courseId,
                                onSaved,
                            }: {
    sectionId: number;
    courseId: number;
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState({
        title: "",
        videoUrl: "",
        videoPublicId: "",
        videoDir: "",
        orderIndex: 0,
        duration: "",
        description: "",
        textContent: "",
        isFree: false,
    });
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState<any>(null);

    async function uploadVideo(file: File) {
        setUploading(true);
        try {
            const fd = new FormData();
            fd.append("file", file);
            const res = await api<any>("/api/uploads/lesson-video", {
                method: "POST",
                formData: fd,
            });
            setForm((f) => ({
                ...f,
                videoUrl: res.url,
                videoPublicId: res.publicId,
                videoDir: res.url,
            }));
            toast.success("Video uploaded");
        } catch (e) {
            setError(e);
        } finally {
            setUploading(false);
        }
    }

    const save = useMutation({
        mutationFn: () =>
            api("/api/lessons/instructor/me", {
                method: "POST",
                body: { ...form, sectionId },
            }),
        onSuccess: () => {
            toast.success("Lesson created");
            setOpen(false);
            setForm({
                title: "",
                videoUrl: "",
                videoPublicId: "",
                videoDir: "",
                orderIndex: 0,
                duration: "",
                description: "",
                textContent: "",
                isFree: false,
            });
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="sm" variant="secondary" className="w-full">
                    <Plus className="mr-1 h-3.5 w-3.5" />
                    Add lesson
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-xl">
                <DialogHeader>
                    <DialogTitle>Create Lesson</DialogTitle>
                </DialogHeader>
                <LessonFormFields
                    form={form}
                    setForm={setForm}
                    uploading={uploading}
                    onUpload={uploadVideo}
                    error={error}
                />
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function EditLessonDialog({
                              lesson,
                              sectionId,
                              onSaved,
                          }: {
    lesson: any;
    sectionId: number;
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState({
        title: lesson.title,
        videoUrl: lesson.videoUrl ?? lesson.videoDir ?? "",
        videoPublicId: lesson.videoPublicId ?? "",
        videoDir: lesson.videoUrl ?? lesson.videoDir ?? "",
        orderIndex: lesson.orderIndex ?? 0,
        duration: lesson.duration ?? "",
        description: lesson.description ?? "",
        textContent: lesson.textContent ?? "",
        isFree: lesson.isFree ?? false,
    });
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState<any>(null);

    async function uploadVideo(file: File) {
        setUploading(true);
        try {
            const fd = new FormData();
            fd.append("file", file);
            const res = await api<any>("/api/uploads/lesson-video", {
                method: "POST",
                formData: fd,
            });
            setForm((f) => ({
                ...f,
                videoUrl: res.url,
                videoPublicId: res.publicId,
                videoDir: res.url,
            }));
            toast.success("Video uploaded");
        } catch (e) {
            setError(e);
        } finally {
            setUploading(false);
        }
    }

    const save = useMutation({
        mutationFn: () =>
            api(`/api/lessons/instructor/me/${lesson.lessonId}`, {
                method: "PUT",
                body: { ...form, sectionId },
            }),
        onSuccess: () => {
            toast.success("Lesson updated");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="icon" variant="ghost" className="h-6 w-6">
                    <Pencil className="h-3.5 w-3.5" />
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-xl">
                <DialogHeader>
                    <DialogTitle>Edit Lesson</DialogTitle>
                </DialogHeader>
                <LessonFormFields
                    form={form}
                    setForm={setForm}
                    uploading={uploading}
                    onUpload={uploadVideo}
                    error={error}
                />
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save Changes
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function LessonFormFields({ form, setForm, uploading, onUpload, error }: any) {
    return (
        <div className="space-y-4 max-h-[70vh] overflow-y-auto px-1">
            <ApiAlert error={error} />
            <div className="space-y-1">
                <Label>Title</Label>
                <Input
                    value={form.title}
                    onChange={(e) => setForm({ ...form, title: e.target.value })}
                    placeholder="Lesson title"
                />
            </div>
            <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                    <Label>Order Index</Label>
                    <Input
                        type="number"
                        value={form.orderIndex ?? 0}
                        onChange={(e) => setForm({ ...form, orderIndex: parseInt(e.target.value) || 0 })}
                    />
                </div>
                <div className="space-y-1">
                    <Label>Duration</Label>
                    <Input
                        value={form.duration ?? ""}
                        onChange={(e) => setForm({ ...form, duration: e.target.value })}
                        placeholder="e.g. 15:30"
                    />
                </div>
            </div>
            <div className="space-y-1">
                <Label>Video</Label>
                <div className="flex items-center gap-2">
                    <Input
                        type="file"
                        accept="video/*"
                        onChange={(e) =>
                            e.target.files?.[0] && onUpload(e.target.files[0])
                        }
                    />
                    {uploading && <Upload className="h-4 w-4 animate-pulse" />}
                </div>
                {(form.videoUrl || form.videoDir) && (
                    <p className="text-xs text-muted-foreground truncate">
                        {form.videoUrl || form.videoDir}
                    </p>
                )}
            </div>
            <div className="space-y-1">
                <Label>Description</Label>
                <Textarea
                    rows={2}
                    value={form.description ?? ""}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                    placeholder="Brief description of this lesson"
                />
            </div>
            <div className="space-y-1">
                <Label>Reading Text / Notes</Label>
                <Textarea
                    rows={3}
                    value={form.textContent ?? ""}
                    onChange={(e) => setForm({ ...form, textContent: e.target.value })}
                    placeholder="Supplemental text, instructions, or notes"
                />
            </div>
            <div className="flex items-start space-x-2 rounded-md border p-3 bg-muted/20">
                <Checkbox
                    id="instructorLessonIsFree"
                    checked={Boolean(form.isFree)}
                    onCheckedChange={(checked) =>
                        setForm({ ...form, isFree: checked === true })
                    }
                />
                <div className="grid gap-1 leading-none">
                    <Label htmlFor="instructorLessonIsFree" className="font-medium cursor-pointer">
                        Free Trial / Preview Lesson
                    </Label>
                    <p className="text-xs text-muted-foreground">
                        Allow prospective students to watch this video without enrolling in the course.
                    </p>
                </div>
            </div>
        </div>
    );
}

function ManageMaterialsDialog({ lesson }: { lesson: any }) {
    const [open, setOpen] = useState(false);
    const [title, setTitle] = useState("");
    const [fileType, setFileType] = useState("PDF");
    const [fileUrl, setFileUrl] = useState("");
    const [filePublicId, setFilePublicId] = useState("");
    const [fileSize, setFileSize] = useState<number | undefined>(undefined);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState<any>(null);

    const qc = useQueryClient();

    const materialsQuery = useQuery<any[]>({
        queryKey: ["instructor-materials", lesson.lessonId],
        queryFn: () => api(`/api/lessons/${lesson.lessonId}/materials`, { auth: true }),
        enabled: open,
    });

    async function handleUploadMaterial(file: File) {
        setUploading(true);
        setError(null);
        try {
            const fd = new FormData();
            fd.append("file", file);
            const res = await api<any>("/api/uploads/material", {
                method: "POST",
                formData: fd,
            });
            setFileUrl(res.url);
            setFilePublicId(res.publicId);
            setFileSize(res.size);
            if (!title) {
                setTitle(file.name.replace(/\.[^/.]+$/, ""));
            }
            const ext = file.name.split(".").pop()?.toUpperCase() || "DOC";
            setFileType(ext === "PDF" ? "PDF" : ext.startsWith("PPT") ? "SLIDE" : "DOC");
            toast.success("File uploaded to Cloudinary");
        } catch (e) {
            setError(e);
            toast.error("Failed to upload file");
        } finally {
            setUploading(false);
        }
    }

    const attachMutation = useMutation({
        mutationFn: () =>
            api(`/api/lessons/${lesson.lessonId}/materials`, {
                method: "POST",
                body: {
                    title,
                    fileUrl,
                    filePublicId,
                    fileType,
                    fileSize,
                },
            }),
        onSuccess: () => {
            toast.success("Material attached");
            setTitle("");
            setFileUrl("");
            setFilePublicId("");
            setFileSize(undefined);
            qc.invalidateQueries({ queryKey: ["instructor-materials", lesson.lessonId] });
        },
        onError: (e) => {
            setError(e);
            toast.error("Failed to attach material");
        },
    });

    const deleteMutation = useMutation({
        mutationFn: (materialId: number) =>
            api(`/api/materials/${materialId}`, {
                method: "DELETE",
            }),
        onSuccess: () => {
            toast.success("Material deleted");
            qc.invalidateQueries({ queryKey: ["instructor-materials", lesson.lessonId] });
        },
        onError: () => toast.error("Failed to delete material"),
    });

    const materials = materialsQuery.data || [];

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="icon" variant="ghost" className="h-6 w-6" title="Manage materials">
                    <Paperclip className="h-3.5 w-3.5" />
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-xl">
                <DialogHeader>
                    <DialogTitle>Materials for: {lesson.title}</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <ApiAlert error={error} />

                    {/* Existing materials list */}
                    <div className="space-y-2">
                        <Label className="text-sm font-semibold">Current Attachments ({materials.length})</Label>
                        {materialsQuery.isLoading ? (
                            <Skeleton className="h-16 w-full" />
                        ) : materials.length > 0 ? (
                            <div className="divide-y rounded-md border max-h-48 overflow-y-auto">
                                {materials.map((m: any) => (
                                    <div key={m.materialId} className="flex items-center justify-between p-2.5 hover:bg-muted/40 text-sm">
                                        <div className="flex items-center gap-2 min-w-0 pr-2">
                                            <FileText className="h-4 w-4 shrink-0 text-primary" />
                                            <div className="min-w-0">
                                                <p className="font-medium line-clamp-1">{m.title}</p>
                                                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                                                    <Badge variant="secondary" className="text-[10px] px-1 py-0">{m.fileType || "DOC"}</Badge>
                                                    {m.fileSize && <span>{(m.fileSize / 1024).toFixed(0)} KB</span>}
                                                </div>
                                            </div>
                                        </div>
                                        <div className="flex items-center gap-1 shrink-0">
                                            <Button size="icon" variant="ghost" className="h-7 w-7" asChild>
                                                <a href={mediaUrl(m.fileUrl)} target="_blank" rel="noreferrer" download>
                                                    <Download className="h-3.5 w-3.5" />
                                                </a>
                                            </Button>
                                            <Button
                                                size="icon"
                                                variant="ghost"
                                                className="h-7 w-7 text-destructive"
                                                onClick={() => {
                                                    if (confirm("Delete this material?")) {
                                                        deleteMutation.mutate(m.materialId);
                                                    }
                                                }}
                                            >
                                                <Trash2 className="h-3.5 w-3.5" />
                                            </Button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p className="text-xs text-muted-foreground">No materials attached yet.</p>
                        )}
                    </div>

                    {/* Attach new material form */}
                    <div className="rounded-lg border p-4 bg-muted/20 space-y-3">
                        <h4 className="text-sm font-semibold">Attach New Material</h4>
                        <div className="space-y-1">
                            <Label>Title</Label>
                            <Input
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                placeholder="e.g. Chapter Slides or Assignment Brief"
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-2">
                            <div className="space-y-1">
                                <Label>Type</Label>
                                <Select value={fileType} onValueChange={setFileType}>
                                    <SelectTrigger><SelectValue /></SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="PDF">PDF Document</SelectItem>
                                        <SelectItem value="SLIDE">Presentation (Slide)</SelectItem>
                                        <SelectItem value="DOC">Word / Text Document</SelectItem>
                                        <SelectItem value="LINK">External Link</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                            <div className="space-y-1">
                                <Label>Upload File</Label>
                                <div className="flex items-center gap-2">
                                    <Input
                                        type="file"
                                        accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip"
                                        onChange={(e) => e.target.files?.[0] && handleUploadMaterial(e.target.files[0])}
                                    />
                                    {uploading && <Upload className="h-4 w-4 animate-pulse shrink-0" />}
                                </div>
                            </div>
                        </div>

                        {fileUrl && (
                            <p className="text-xs text-muted-foreground truncate">Uploaded: {fileUrl}</p>
                        )}

                        <Button
                            size="sm"
                            className="w-full"
                            disabled={attachMutation.isPending || !title || !fileUrl}
                            onClick={() => attachMutation.mutate()}
                        >
                            <Plus className="mr-1 h-3.5 w-3.5" /> Attach Material
                        </Button>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
}

// ---------------------------------------------------------------------------
// Quiz / Question / Answer dialogs
// ---------------------------------------------------------------------------

function InstructorQuizDialog({
                                  courseId,
                                  onSaved,
                              }: {
    courseId: number;
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState<any>({
        title: "",
        totalPoints: 10,
        lessonId: "",
    });
    const [error, setError] = useState<any>(null);

    const { data: lessons } = useQuery<any[]>({
        queryKey: ["instructor-lessons-all", courseId],
        queryFn: () =>
            api(`/api/lessons/instructor/me?courseId=${courseId}`),
    });

    const save = useMutation({
        mutationFn: () =>
            api("/api/quizzes", {
                method: "POST",
                body: { ...form, lessonId: Number(form.lessonId) },
            }),
        onSuccess: () => {
            toast.success("Quiz created");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="sm" variant="outline" className="w-full">
                    <ListChecks className="mr-1 h-3.5 w-3.5" />
                    Quiz
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create Quiz</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <ApiAlert error={error} />
                    <div className="space-y-1">
                        <Label>Title</Label>
                        <Input
                            value={form.title}
                            onChange={(e) => setForm({ ...form, title: e.target.value })}
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Total Points</Label>
                        <Input
                            type="number"
                            value={form.totalPoints}
                            onChange={(e) =>
                                setForm({ ...form, totalPoints: Number(e.target.value) })
                            }
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Lesson</Label>
                        <Select
                            value={String(form.lessonId)}
                            onValueChange={(v) =>
                                setForm({ ...form, lessonId: parseInt(v) })
                            }
                        >
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                {lessons?.map((l) => (
                                    <SelectItem key={l.lessonId} value={String(l.lessonId)}>
                                        {l.title}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                </div>
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function InstructorQuestionDialog({
                                      courseId,
                                      onSaved,
                                  }: {
    courseId: number;
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState<any>({ questionText: "", quizId: "" });
    const [error, setError] = useState<any>(null);

    const { data: quizzes } = useQuery<any[]>({
        queryKey: ["instructor-quizzes", courseId],
        queryFn: () => api("/api/quizzes"),
    });

    const save = useMutation({
        mutationFn: () =>
            api("/api/questions", {
                method: "POST",
                body: { ...form, quizId: Number(form.quizId) },
            }),
        onSuccess: () => {
            toast.success("Question created");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="sm" variant="outline" className="w-full">
                    <HelpCircle className="mr-1 h-3.5 w-3.5" />
                    Question
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create Question</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <ApiAlert error={error} />
                    <div className="space-y-1">
                        <Label>Question</Label>
                        <Textarea
                            value={form.questionText}
                            onChange={(e) =>
                                setForm({ ...form, questionText: e.target.value })
                            }
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Quiz</Label>
                        <Select
                            value={String(form.quizId)}
                            onValueChange={(v) =>
                                setForm({ ...form, quizId: parseInt(v) })
                            }
                        >
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                {quizzes?.map((q) => (
                                    <SelectItem key={q.quizId} value={String(q.quizId)}>
                                        {q.title}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                </div>
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

function InstructorAnswerDialog({
                                    courseId,
                                    onSaved,
                                }: {
    courseId: number;
    onSaved: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState<any>({
        answerText: "",
        isCorrect: false,
        questionId: "",
    });
    const [error, setError] = useState<any>(null);

    const { data: questions } = useQuery<any[]>({
        queryKey: ["instructor-questions", courseId],
        queryFn: () => api("/api/questions"),
    });

    const save = useMutation({
        mutationFn: () =>
            api("/api/answers", {
                method: "POST",
                body: { ...form, questionId: Number(form.questionId) },
            }),
        onSuccess: () => {
            toast.success("Answer created");
            setOpen(false);
            onSaved();
        },
        onError: (e) => setError(e),
    });

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button size="sm" variant="ghost" className="w-full border">
                    <CheckSquare className="mr-1 h-3.5 w-3.5" />
                    Answer
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create Answer</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <ApiAlert error={error} />
                    <div className="space-y-1">
                        <Label>Answer</Label>
                        <Textarea
                            value={form.answerText}
                            onChange={(e) =>
                                setForm({ ...form, answerText: e.target.value })
                            }
                        />
                    </div>
                    <div className="space-y-1">
                        <Label>Question</Label>
                        <Select
                            value={String(form.questionId)}
                            onValueChange={(v) =>
                                setForm({ ...form, questionId: parseInt(v) })
                            }
                        >
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                {questions?.map((q) => (
                                    <SelectItem key={q.questionId} value={String(q.questionId)}>
                                        {q.questionText}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="flex items-center gap-2">
                        <input
                            type="checkbox"
                            checked={form.isCorrect}
                            onChange={(e) =>
                                setForm({ ...form, isCorrect: e.target.checked })
                            }
                        />
                        <Label>Correct Answer</Label>
                    </div>
                </div>
                <DialogFooter>
                    <Button onClick={() => save.mutate()} disabled={save.isPending}>
                        Save
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}

// ---------------------------------------------------------------------------
// Enrollments
// ---------------------------------------------------------------------------

function InstructorEnrollments() {
    const { data, isLoading, error } = useQuery<any[]>({
        queryKey: ["instructor-enrollments"],
        queryFn: () => api("/api/enrollments"),
    });

    return (
        <Card className="overflow-hidden">
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <Users className="h-5 w-5" />
                    Students enrolled in your courses
                </CardTitle>
            </CardHeader>
            <CardContent>
                <ApiAlert error={error} />

                {isLoading ? (
                    <Skeleton className="h-40 w-full" />
                ) : !data || data.length === 0 ? (
                    <div className="rounded-md border border-dashed p-8 text-center">
                        <p className="text-sm text-muted-foreground">
                            No students have joined your courses yet.
                        </p>
                    </div>
                ) : (
                    <div className="rounded-md border">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Student</TableHead>
                                    <TableHead>Email</TableHead>
                                    <TableHead>Course</TableHead>
                                    <TableHead>Status</TableHead>
                                    <TableHead>Joined</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {data.map((enrollment: any) => (
                                    <TableRow key={enrollment.enrollmentId}>
                                        <TableCell className="font-medium">
                                            {enrollment.username || `User #${enrollment.userId}`}
                                        </TableCell>
                                        <TableCell className="text-muted-foreground">
                                            {enrollment.userEmail || "-"}
                                        </TableCell>
                                        <TableCell>
                                            {enrollment.courseTitle || `Course #${enrollment.courseId}`}
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant={enrollment.status === "ACTIVE" ? "default" : "secondary"}>
                                                {enrollment.status}
                                            </Badge>
                                        </TableCell>
                                        <TableCell className="text-muted-foreground">
                                            {formatDate(enrollment.enrolledAt)}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}

function formatDate(value?: string | number | null) {
    if (!value) return "-";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "-";

    return date.toLocaleDateString();
}

// ---------------------------------------------------------------------------
// Instructor Profile & Bio
// ---------------------------------------------------------------------------

function InstructorProfileTab() {
    const qc = useQueryClient();
    const { data: profile, isLoading, error } = useQuery<any>({
        queryKey: ["instructor-me-profile"],
        queryFn: () => api("/api/instructors/me"),
    });

    const [form, setForm] = useState<any>({
        fullName: "",
        phoneNumber: "",
        biography: "",
        expertise: "",
        gender: "NOT_SPECIFIC",
        dateOfBirth: "",
    });

    const [uploadingPhoto, setUploadingPhoto] = useState(false);
    const [photoError, setPhotoError] = useState<unknown>(null);

    // Sync loaded profile into form
    const [initialized, setInitialized] = useState(false);
    if (profile && !initialized) {
        setForm({
            fullName: profile.fullName || "",
            phoneNumber: profile.phoneNumber || "",
            biography: profile.biography || "",
            expertise: profile.expertise || "",
            gender: profile.gender || "NOT_SPECIFIC",
            dateOfBirth: profile.dateOfBirth || "",
        });
        setInitialized(true);
    }

    const updateProfile = useMutation({
        mutationFn: () =>
            api("/api/instructors/me", {
                method: "PUT",
                body: form,
            }),
        onSuccess: (updated) => {
            qc.setQueryData(["instructor-me-profile"], updated);
            toast.success("Instructor profile updated!");
        },
        onError: (err: any) => {
            toast.error(err.message || "Failed to update profile");
        },
    });

    async function handlePhotoUpload(e: React.ChangeEvent<HTMLInputElement>) {
        const file = e.target.files?.[0];
        if (!file) return;
        setUploadingPhoto(true);
        setPhotoError(null);
        try {
            const fd = new FormData();
            fd.append("file", file);
            const updated = await api<any>("/api/instructors/me/photo", {
                method: "POST",
                formData: fd,
            });
            qc.setQueryData(["instructor-me-profile"], updated);
            toast.success("Profile photo updated!");
        } catch (err) {
            setPhotoError(err);
            toast.error("Failed to upload photo");
        } finally {
            setUploadingPhoto(false);
        }
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <User className="h-5 w-5 text-primary" /> Instructor Profile
                </CardTitle>
                <p className="text-sm text-muted-foreground">
                    This public bio and expertise will appear to students on course pages and the instructors directory.
                </p>
            </CardHeader>
            <CardContent className="space-y-6">
                <ApiAlert error={error || photoError} />

                {isLoading ? (
                    <div className="space-y-4">
                        <Skeleton className="h-20 w-20 rounded-full" />
                        <Skeleton className="h-10 w-full" />
                        <Skeleton className="h-10 w-full" />
                    </div>
                ) : (
                    <>
                        <div className="flex items-center gap-6">
                            <div className="relative group">
                                <div className="h-24 w-24 rounded-full overflow-hidden border-2 border-border bg-muted flex items-center justify-center">
                                    {profile?.profilePhotoUrl ? (
                                        <img
                                            src={mediaUrl(profile.profilePhotoUrl)}
                                            alt="Instructor avatar"
                                            className="h-full w-full object-cover"
                                        />
                                    ) : (
                                        <User className="h-10 w-10 text-muted-foreground" />
                                    )}
                                </div>
                                <label className="absolute bottom-0 right-0 bg-primary text-primary-foreground p-1.5 rounded-full cursor-pointer shadow hover:bg-primary/90 transition-colors">
                                    <Camera className="h-4 w-4" />
                                    <input
                                        type="file"
                                        accept="image/*"
                                        className="hidden"
                                        onChange={handlePhotoUpload}
                                        disabled={uploadingPhoto}
                                    />
                                </label>
                            </div>
                            <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                    <h3 className="font-semibold text-lg">{profile?.fullName || profile?.username}</h3>
                                    {profile?.username && (
                                        <span className="text-xs text-muted-foreground font-mono">@{profile.username}</span>
                                    )}
                                </div>
                                {profile?.email && (
                                    <p className="text-xs text-muted-foreground flex items-center gap-1">
                                        <Mail className="h-3 w-3" /> {profile.email}
                                    </p>
                                )}
                                <div className="flex flex-wrap items-center gap-2 pt-1">
                                    {profile?.totalCourses != null && (
                                        <Badge variant="outline" className="gap-1 text-xs">
                                            <BookOpen className="h-3 w-3" /> {profile.totalCourses} {profile.totalCourses === 1 ? "Course" : "Courses"}
                                        </Badge>
                                    )}
                                    {profile?.averageRating != null && profile.averageRating > 0 && (
                                        <Badge variant="secondary" className="gap-1 text-xs text-amber-600">
                                            <Star className="h-3 w-3 fill-amber-500 text-amber-500" /> {profile.averageRating.toFixed(1)} Rating
                                        </Badge>
                                    )}
                                </div>
                                <p className="text-xs text-muted-foreground pt-1">
                                    {uploadingPhoto ? "Uploading photo..." : "Upload your official instructor avatar."}
                                </p>
                            </div>
                        </div>

                        <form
                            onSubmit={(e) => {
                                e.preventDefault();
                                updateProfile.mutate();
                            }}
                            className="grid grid-cols-1 md:grid-cols-2 gap-4"
                        >
                            <div className="space-y-1">
                                <Label>Full Name</Label>
                                <Input
                                    value={form.fullName}
                                    onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                                />
                            </div>

                            <div className="space-y-1">
                                <Label>Phone Number</Label>
                                <Input
                                    value={form.phoneNumber}
                                    onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
                                />
                            </div>

                            <div className="space-y-1 md:col-span-2">
                                <Label>Teaching Expertise / Title</Label>
                                <Input
                                    placeholder="e.g. Senior Software Architect, Machine Learning Specialist"
                                    value={form.expertise}
                                    onChange={(e) => setForm({ ...form, expertise: e.target.value })}
                                />
                            </div>

                            <div className="space-y-1 md:col-span-2">
                                <Label>Biography</Label>
                                <Textarea
                                    rows={4}
                                    placeholder="Share your career highlights, academic background, and teaching philosophy..."
                                    value={form.biography}
                                    onChange={(e) => setForm({ ...form, biography: e.target.value })}
                                />
                            </div>

                            <div className="md:col-span-2 flex justify-end">
                                <Button type="submit" disabled={updateProfile.isPending}>
                                    <Save className="mr-2 h-4 w-4" />
                                    {updateProfile.isPending ? "Saving..." : "Save Profile"}
                                </Button>
                            </div>
                        </form>
                    </>
                )}
            </CardContent>
        </Card>
    );
}

// ---------------------------------------------------------------------------
// Instructor Security & Password
// ---------------------------------------------------------------------------

function InstructorSecurityTab() {
    const [pwForm, setPwForm] = useState({ oldPassword: "", newPassword: "", confirmPassword: "" });
    const [pwError, setPwError] = useState<unknown>(null);

    const changePw = useMutation({
        mutationFn: () => {
            if (pwForm.newPassword !== pwForm.confirmPassword) {
                throw new Error("New passwords do not match");
            }
            return api<string>("/api/users/change-password", {
                method: "POST",
                body: {
                    oldPassword: pwForm.oldPassword,
                    newPassword: pwForm.newPassword,
                },
            });
        },
        onSuccess: () => {
            toast.success("Password changed successfully");
            setPwForm({ oldPassword: "", newPassword: "", confirmPassword: "" });
            setPwError(null);
        },
        onError: (err: any) => {
            setPwError(err);
        },
    });

    return (
        <Card className="max-w-xl">
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <Lock className="h-5 w-5 text-primary" /> Change Password
                </CardTitle>
                <p className="text-sm text-muted-foreground">
                    Update your account password to protect your teaching studio.
                </p>
            </CardHeader>
            <CardContent>
                <ApiAlert error={pwError} />
                <form
                    onSubmit={(e) => {
                        e.preventDefault();
                        changePw.mutate();
                    }}
                    className="space-y-4"
                >
                    <div className="space-y-1">
                        <Label>Current Password</Label>
                        <Input
                            type="password"
                            required
                            value={pwForm.oldPassword}
                            onChange={(e) => setPwForm({ ...pwForm, oldPassword: e.target.value })}
                        />
                    </div>

                    <div className="space-y-1">
                        <Label>New Password</Label>
                        <Input
                            type="password"
                            required
                            minLength={6}
                            value={pwForm.newPassword}
                            onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })}
                        />
                    </div>

                    <div className="space-y-1">
                        <Label>Confirm New Password</Label>
                        <Input
                            type="password"
                            required
                            minLength={6}
                            value={pwForm.confirmPassword}
                            onChange={(e) => setPwForm({ ...pwForm, confirmPassword: e.target.value })}
                        />
                    </div>

                    <Button type="submit" disabled={changePw.isPending}>
                        <Lock className="mr-2 h-4 w-4" />
                        {changePw.isPending ? "Updating Password..." : "Change Password"}
                    </Button>
                </form>
            </CardContent>
        </Card>
    );
}

// ---------------------------------------------------------------------------
// Assignments & Grading System (Stage 3)
// ---------------------------------------------------------------------------

function InstructorAssignmentsTab() {
    const qc = useQueryClient();
    const [selectedCourseId, setSelectedCourseId] = useState<string>("");
    const [createDialogOpen, setCreateDialogOpen] = useState(false);
    const [editAssignment, setEditAssignment] = useState<any>(null);
    const [submissionsAssignment, setSubmissionsAssignment] = useState<any>(null);

    const { data: courses, isLoading: loadingCourses } = useQuery<any[]>({
        queryKey: ["instructor-courses"],
        queryFn: () => api("/api/courses/instructor/me"),
    });

    const activeCourseId = selectedCourseId || (courses && courses.length > 0 ? String(courses[0].courseId) : "");

    const { data: assignments, isLoading: loadingAssignments } = useQuery<any[]>({
        queryKey: ["course-assignments", activeCourseId],
        queryFn: () => api(`/api/courses/${activeCourseId}/assignments`),
        enabled: Boolean(activeCourseId),
    });

    const selectedCourse = courses?.find((c) => String(c.courseId) === activeCourseId);

    const deleteMutation = useMutation({
        mutationFn: (assignmentId: number) =>
            api(`/api/assignments/${assignmentId}`, { method: "DELETE" }),
        onSuccess: () => {
            toast.success("Assignment deleted successfully");
            qc.invalidateQueries({ queryKey: ["course-assignments", activeCourseId] });
        },
        onError: (err: any) => toast.error(err.message || "Failed to delete assignment"),
    });

    const handleDelete = (id: number) => {
        if (window.confirm("Are you sure you want to delete this assignment and all student submissions?")) {
            deleteMutation.mutate(id);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                    <Label className="text-sm font-semibold whitespace-nowrap">Select Course:</Label>
                    <Select
                        value={activeCourseId}
                        onValueChange={(val) => setSelectedCourseId(val)}
                        disabled={loadingCourses || !courses || courses.length === 0}
                    >
                        <SelectTrigger className="w-[280px]">
                            <SelectValue placeholder="Choose a course..." />
                        </SelectTrigger>
                        <SelectContent>
                            {courses?.map((c) => (
                                <SelectItem key={c.courseId} value={String(c.courseId)}>
                                    {c.title}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                {activeCourseId && (
                    <Button onClick={() => setCreateDialogOpen(true)} className="gap-1.5 shrink-0">
                        <Plus className="h-4 w-4" /> Create Assignment
                    </Button>
                )}
            </div>

            {loadingCourses ? (
                <Skeleton className="h-32 w-full" />
            ) : !courses || courses.length === 0 ? (
                <Card>
                    <CardContent className="py-10 text-center text-muted-foreground">
                        You have no courses yet. Create a course first to add assignments.
                    </CardContent>
                </Card>
            ) : !activeCourseId ? (
                <Card>
                    <CardContent className="py-10 text-center text-muted-foreground">
                        Please select a course above to view its assignments.
                    </CardContent>
                </Card>
            ) : loadingAssignments ? (
                <Skeleton className="h-40 w-full" />
            ) : !assignments || assignments.length === 0 ? (
                <Card>
                    <CardContent className="py-12 text-center space-y-3">
                        <FileText className="h-10 w-10 mx-auto text-muted-foreground/60" />
                        <h3 className="font-semibold text-lg">No Assignments Found</h3>
                        <p className="text-sm text-muted-foreground max-w-sm mx-auto">
                            Add tasks, homework, or projects with due dates and grading criteria for your students.
                        </p>
                        <Button onClick={() => setCreateDialogOpen(true)} variant="outline" className="gap-1.5 mt-2">
                            <Plus className="h-4 w-4" /> Create First Assignment
                        </Button>
                    </CardContent>
                </Card>
            ) : (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {assignments.map((assign: any) => {
                        const isPastDue = assign.dueDate && new Date(assign.dueDate).getTime() < Date.now();
                        return (
                            <Card key={assign.assignmentId} className="flex flex-col justify-between">
                                <CardHeader className="pb-3">
                                    <div className="flex items-start justify-between gap-2">
                                        <CardTitle className="text-base font-bold line-clamp-1">
                                            {assign.title}
                                        </CardTitle>
                                        <Badge
                                            variant={isPastDue ? "destructive" : "secondary"}
                                            className="text-[10px] shrink-0"
                                        >
                                            {assign.dueDate
                                                ? new Date(assign.dueDate).toLocaleDateString()
                                                : "No Due Date"}
                                        </Badge>
                                    </div>
                                    {assign.sectionTitle && (
                                        <p className="text-xs text-muted-foreground">
                                            Section: {assign.sectionTitle}
                                        </p>
                                    )}
                                </CardHeader>

                                <CardContent className="space-y-3 flex-1 pb-3">
                                    <p className="text-xs text-muted-foreground line-clamp-2">
                                        {assign.description || "No description provided."}
                                    </p>

                                    <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground pt-1 border-t">
                                        <span className="font-medium text-foreground">Max: {assign.maxScore} pts</span>
                                        <span>•</span>
                                        <span>{assign.allowResubmission ? "Resubmission On" : "Single Submission"}</span>
                                    </div>

                                    {assign.supportingFileUrl && (
                                        <div className="pt-1">
                                            <a
                                                href={mediaUrl(assign.supportingFileUrl)}
                                                target="_blank"
                                                rel="noreferrer"
                                                download
                                                className="text-xs text-primary underline flex items-center gap-1 hover:text-primary/80"
                                            >
                                                <Paperclip className="h-3 w-3" /> Attached Document
                                            </a>
                                        </div>
                                    )}
                                </CardContent>

                                <div className="p-4 pt-0 border-t flex items-center justify-between gap-2 mt-auto">
                                    <Button
                                        size="sm"
                                        variant="default"
                                        className="gap-1 text-xs flex-1"
                                        onClick={() => setSubmissionsAssignment(assign)}
                                    >
                                        <Users className="h-3.5 w-3.5" /> Submissions
                                    </Button>
                                    <Button
                                        size="sm"
                                        variant="outline"
                                        className="h-8 w-8 p-0"
                                        title="Edit assignment"
                                        onClick={() => setEditAssignment(assign)}
                                    >
                                        <Pencil className="h-3.5 w-3.5" />
                                    </Button>
                                    <Button
                                        size="sm"
                                        variant="outline"
                                        className="h-8 w-8 p-0 text-destructive hover:bg-destructive/10"
                                        title="Delete assignment"
                                        onClick={() => handleDelete(assign.assignmentId)}
                                    >
                                        <Trash2 className="h-3.5 w-3.5" />
                                    </Button>
                                </div>
                            </Card>
                        );
                    })}
                </div>
            )}

            {/* Create Assignment Dialog */}
            {createDialogOpen && selectedCourse && (
                <AssignmentFormDialog
                    course={selectedCourse}
                    onClose={() => setCreateDialogOpen(false)}
                    onSaved={() => {
                        setCreateDialogOpen(false);
                        qc.invalidateQueries({ queryKey: ["course-assignments", activeCourseId] });
                    }}
                />
            )}

            {/* Edit Assignment Dialog */}
            {editAssignment && selectedCourse && (
                <AssignmentFormDialog
                    course={selectedCourse}
                    assignment={editAssignment}
                    onClose={() => setEditAssignment(null)}
                    onSaved={() => {
                        setEditAssignment(null);
                        qc.invalidateQueries({ queryKey: ["course-assignments", activeCourseId] });
                    }}
                />
            )}

            {/* Submissions Inspection Dialog */}
            {submissionsAssignment && (
                <AssignmentSubmissionsDialog
                    assignment={submissionsAssignment}
                    onClose={() => setSubmissionsAssignment(null)}
                />
            )}
        </div>
    );
}

function AssignmentFormDialog({
    course,
    assignment,
    onClose,
    onSaved,
}: {
    course: any;
    assignment?: any;
    onClose: () => void;
    onSaved: () => void;
}) {
    const isEdit = Boolean(assignment);
    const [title, setTitle] = useState(assignment?.title || "");
    const [sectionId, setSectionId] = useState<string>(assignment?.sectionId ? String(assignment.sectionId) : "none");
    const [description, setDescription] = useState(assignment?.description || "");
    const [instructions, setInstructions] = useState(assignment?.instructions || "");
    const [startDate, setStartDate] = useState(
        assignment?.startDate ? new Date(assignment.startDate).toISOString().slice(0, 16) : ""
    );
    const [dueDate, setDueDate] = useState(
        assignment?.dueDate ? new Date(assignment.dueDate).toISOString().slice(0, 16) : ""
    );
    const [maxScore, setMaxScore] = useState(String(assignment?.maxScore ?? 100));
    const [allowResubmission, setAllowResubmission] = useState(assignment?.allowResubmission ?? true);
    const [file, setFile] = useState<File | null>(null);
    const [uploading, setUploading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!title.trim()) {
            toast.error("Assignment title is required");
            return;
        }

        setSubmitting(true);
        try {
            let supportingFileUrl = assignment?.supportingFileUrl || null;
            let supportingFilePublicId = assignment?.supportingFilePublicId || null;

            if (file) {
                setUploading(true);
                const formData = new FormData();
                formData.append("file", file);
                const res = await api<any>("/api/uploads/assignment-file", {
                    method: "POST",
                    formData,
                });
                supportingFileUrl = res.url;
                supportingFilePublicId = res.publicId;
                setUploading(false);
            }

            const body: any = {
                courseId: course.courseId,
                sectionId: sectionId === "none" ? null : Number(sectionId),
                title: title.trim(),
                description: description.trim() || null,
                instructions: instructions.trim() || null,
                startDate: startDate ? new Date(startDate).toISOString() : null,
                dueDate: dueDate ? new Date(dueDate).toISOString() : null,
                maxScore: Number(maxScore) > 0 ? Number(maxScore) : 100,
                supportingFileUrl,
                supportingFilePublicId,
                allowResubmission,
            };

            if (isEdit) {
                await api(`/api/assignments/${assignment.assignmentId}`, {
                    method: "PUT",
                    body,
                });
                toast.success("Assignment updated successfully");
            } else {
                await api("/api/assignments", {
                    method: "POST",
                    body,
                });
                toast.success("Assignment created successfully");
            }

            onSaved();
        } catch (err: any) {
            toast.error(err.message || "Failed to save assignment");
        } finally {
            setSubmitting(false);
            setUploading(false);
        }
    };

    return (
        <Dialog open onOpenChange={() => onClose()}>
            <DialogContent className="max-w-xl max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>{isEdit ? "Edit Assignment" : "Create New Assignment"}</DialogTitle>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-4 py-2">
                    <div>
                        <Label>Title *</Label>
                        <Input
                            placeholder="e.g. Final Project: Full Stack LMS"
                            required
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            className="mt-1"
                        />
                    </div>

                    {course.sections && course.sections.length > 0 && (
                        <div>
                            <Label>Course Section (Optional)</Label>
                            <Select value={sectionId} onValueChange={(val) => setSectionId(val)}>
                                <SelectTrigger className="mt-1">
                                    <SelectValue placeholder="No specific section" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="none">No specific section</SelectItem>
                                    {course.sections.map((s: any) => (
                                        <SelectItem key={s.sectionId} value={String(s.sectionId)}>
                                            {s.title}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    )}

                    <div>
                        <Label>Description</Label>
                        <Textarea
                            placeholder="Brief summary of the assignment goal..."
                            rows={2}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            className="mt-1"
                        />
                    </div>

                    <div>
                        <Label>Detailed Instructions</Label>
                        <Textarea
                            placeholder="Step-by-step instructions, submission guidelines, grading rubrics..."
                            rows={4}
                            value={instructions}
                            onChange={(e) => setInstructions(e.target.value)}
                            className="mt-1"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <Label>Start Date</Label>
                            <Input
                                type="datetime-local"
                                value={startDate}
                                onChange={(e) => setStartDate(e.target.value)}
                                className="mt-1"
                            />
                        </div>
                        <div>
                            <Label>Due Date</Label>
                            <Input
                                type="datetime-local"
                                value={dueDate}
                                onChange={(e) => setDueDate(e.target.value)}
                                className="mt-1"
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 items-center">
                        <div>
                            <Label>Maximum Score (pts)</Label>
                            <Input
                                type="number"
                                min={1}
                                value={maxScore}
                                onChange={(e) => setMaxScore(e.target.value)}
                                className="mt-1"
                            />
                        </div>
                        <div className="flex items-center gap-2 pt-6">
                            <Checkbox
                                id="allowResub"
                                checked={allowResubmission}
                                onCheckedChange={(val) => setAllowResubmission(Boolean(val))}
                            />
                            <Label htmlFor="allowResub" className="text-sm font-normal cursor-pointer">
                                Allow Resubmissions
                            </Label>
                        </div>
                    </div>

                    <div>
                        <Label>Supporting Document / Starter Files (Optional)</Label>
                        <Input
                            type="file"
                            onChange={(e) => setFile(e.target.files?.[0] || null)}
                            className="mt-1"
                        />
                        {assignment?.supportingFileUrl && !file && (
                            <p className="text-xs text-muted-foreground mt-1">
                                Current file attached:{" "}
                                <a
                                    href={mediaUrl(assignment.supportingFileUrl)}
                                    target="_blank"
                                    rel="noreferrer"
                                    download
                                    className="text-primary underline"
                                >
                                    Download Attachment
                                </a>
                            </p>
                        )}
                    </div>

                    <DialogFooter className="gap-2 pt-2">
                        <Button type="button" variant="outline" onClick={onClose}>
                            Cancel
                        </Button>
                        <Button type="submit" disabled={submitting || uploading}>
                            {uploading ? "Uploading File..." : submitting ? "Saving..." : isEdit ? "Update Assignment" : "Create Assignment"}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}

function AssignmentSubmissionsDialog({
    assignment,
    onClose,
}: {
    assignment: any;
    onClose: () => void;
}) {
    const qc = useQueryClient();
    const [gradingSubmission, setGradingSubmission] = useState<any>(null);

    const { data: submissions, isLoading } = useQuery<any[]>({
        queryKey: ["assignment-submissions", assignment.assignmentId],
        queryFn: () => api(`/api/assignments/${assignment.assignmentId}/submissions`),
    });

    const formatDate = (ts: any) => {
        if (!ts) return "N/A";
        return new Date(ts).toLocaleString();
    };

    return (
        <Dialog open onOpenChange={() => onClose()}>
            <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                    <div className="flex items-center justify-between pr-6">
                        <div>
                            <DialogTitle className="text-xl font-bold">
                                Submissions: {assignment.title}
                            </DialogTitle>
                            <p className="text-xs text-muted-foreground mt-0.5">
                                Max Score: {assignment.maxScore} pts • Due:{" "}
                                {assignment.dueDate ? formatDate(assignment.dueDate) : "No due date"}
                            </p>
                        </div>
                    </div>
                </DialogHeader>

                <div className="py-2">
                    {isLoading ? (
                        <Skeleton className="h-40 w-full" />
                    ) : !submissions || submissions.length === 0 ? (
                        <div className="py-12 text-center text-muted-foreground border rounded-lg">
                            <Users className="h-8 w-8 mx-auto mb-2 opacity-50" />
                            <p className="text-sm">No students have submitted this assignment yet.</p>
                        </div>
                    ) : (
                        <div className="border rounded-lg overflow-hidden">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Student</TableHead>
                                        <TableHead>Submitted At</TableHead>
                                        <TableHead>Submission</TableHead>
                                        <TableHead>Status</TableHead>
                                        <TableHead>Score & Grade</TableHead>
                                        <TableHead className="text-right">Action</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {submissions.map((sub) => (
                                        <TableRow key={sub.submissionId}>
                                            <TableCell>
                                                <p className="font-semibold text-sm">{sub.studentName || "Student"}</p>
                                                <p className="text-xs text-muted-foreground">{sub.studentEmail}</p>
                                            </TableCell>
                                            <TableCell className="text-xs text-muted-foreground">
                                                {formatDate(sub.submittedAt)}
                                            </TableCell>
                                            <TableCell className="max-w-xs">
                                                {sub.textSubmission && (
                                                    <p className="text-xs line-clamp-2 text-foreground font-mono bg-muted/40 p-1 rounded">
                                                        {sub.textSubmission}
                                                    </p>
                                                )}
                                                {sub.fileUrl && (
                                                    <a
                                                        href={mediaUrl(sub.fileUrl)}
                                                        target="_blank"
                                                        rel="noreferrer"
                                                        download
                                                        className="text-xs text-primary underline flex items-center gap-1 mt-1 hover:text-primary/80"
                                                    >
                                                        <FileText className="h-3 w-3" /> Download File <ExternalLink className="h-2.5 w-2.5" />
                                                    </a>
                                                )}
                                            </TableCell>
                                            <TableCell>
                                                <Badge
                                                    variant={
                                                        sub.status === "GRADED"
                                                            ? "default"
                                                            : sub.status === "LATE"
                                                            ? "destructive"
                                                            : "secondary"
                                                    }
                                                >
                                                    {sub.status}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>
                                                {sub.score != null ? (
                                                    <div>
                                                        <span className="font-bold text-sm">
                                                            {sub.score} / {assignment.maxScore}
                                                        </span>
                                                        {sub.grade && (
                                                            <Badge className="ml-1.5 text-[10px] bg-emerald-600">
                                                                {sub.grade}
                                                            </Badge>
                                                        )}
                                                        {sub.feedback && (
                                                            <p className="text-[11px] text-muted-foreground line-clamp-1 mt-0.5">
                                                                "{sub.feedback}"
                                                            </p>
                                                        )}
                                                    </div>
                                                ) : (
                                                    <span className="text-xs text-muted-foreground italic">Not graded</span>
                                                )}
                                            </TableCell>
                                            <TableCell className="text-right">
                                                <Button
                                                    size="sm"
                                                    variant={sub.status === "GRADED" ? "outline" : "default"}
                                                    className="text-xs"
                                                    onClick={() => setGradingSubmission(sub)}
                                                >
                                                    {sub.status === "GRADED" ? "Update Grade" : "Grade"}
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                    )}
                </div>

                <DialogFooter>
                    <Button variant="outline" size="sm" onClick={onClose}>
                        Close
                    </Button>
                </DialogFooter>

                {gradingSubmission && (
                    <GradeSubmissionDialog
                        submission={gradingSubmission}
                        maxScore={assignment.maxScore}
                        onClose={() => setGradingSubmission(null)}
                        onGraded={() => {
                            setGradingSubmission(null);
                            qc.invalidateQueries({ queryKey: ["assignment-submissions", assignment.assignmentId] });
                        }}
                    />
                )}
            </DialogContent>
        </Dialog>
    );
}

function GradeSubmissionDialog({
    submission,
    maxScore,
    onClose,
    onGraded,
}: {
    submission: any;
    maxScore: number;
    onClose: () => void;
    onGraded: () => void;
}) {
    const [score, setScore] = useState<string>(submission.score != null ? String(submission.score) : "");
    const [grade, setGrade] = useState<string>(submission.grade || "");
    const [feedback, setFeedback] = useState<string>(submission.feedback || "");
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const numScore = score !== "" ? Number(score) : null;
        if (numScore != null && (numScore < 0 || numScore > maxScore)) {
            toast.error(`Score must be between 0 and ${maxScore}`);
            return;
        }

        setSubmitting(true);
        try {
            await api(`/api/assignments/submissions/${submission.submissionId}/grade`, {
                method: "PUT",
                body: {
                    score: numScore,
                    grade: grade.trim() || null,
                    feedback: feedback.trim() || null,
                },
            });
            toast.success("Submission graded successfully");
            onGraded();
        } catch (err: any) {
            toast.error(err.message || "Failed to grade submission");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog open onOpenChange={() => onClose()}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                    <DialogTitle>Grade Submission</DialogTitle>
                    <p className="text-xs text-muted-foreground">
                        Student: <span className="font-semibold text-foreground">{submission.studentName}</span> (
                        {submission.studentEmail})
                    </p>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-4 py-2">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <Label>Score (Max: {maxScore})</Label>
                            <Input
                                type="number"
                                step="any"
                                min={0}
                                max={maxScore}
                                required
                                value={score}
                                onChange={(e) => setScore(e.target.value)}
                                className="mt-1"
                            />
                        </div>
                        <div>
                            <Label>Letter Grade (Optional)</Label>
                            <Input
                                placeholder="A, B, C, D, F"
                                value={grade}
                                onChange={(e) => setGrade(e.target.value.toUpperCase())}
                                className="mt-1"
                            />
                            <p className="text-[10px] text-muted-foreground mt-0.5">
                                Auto-calculated if blank
                            </p>
                        </div>
                    </div>

                    <div>
                        <Label>Instructor Feedback & Comments</Label>
                        <Textarea
                            placeholder="Provide constructive feedback, praise strengths, or indicate areas for improvement..."
                            rows={4}
                            value={feedback}
                            onChange={(e) => setFeedback(e.target.value)}
                            className="mt-1"
                        />
                    </div>

                    <DialogFooter className="gap-2 pt-2">
                        <Button type="button" variant="outline" onClick={onClose}>
                            Cancel
                        </Button>
                        <Button type="submit" disabled={submitting}>
                            {submitting ? "Saving Grade..." : "Submit Grade"}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}

