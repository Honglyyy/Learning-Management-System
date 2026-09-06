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
                        <TabsTrigger value="enrollments">Enrollments</TabsTrigger>
                        <TabsTrigger value="profile">Profile & Bio</TabsTrigger>
                        <TabsTrigger value="security">Security</TabsTrigger>
                    </TabsList>
                    <TabsContent value="courses">
                        <MyCourses />
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
    const [form, setForm] = useState({ title: "", videoUrl: "", videoPublicId: "", videoDir: "" });
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
            setForm({ title: "", videoUrl: "", videoPublicId: "", videoDir: "" });
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
        </div>
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
