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
                    </TabsList>
                    <TabsContent value="courses">
                        <MyCourses />
                    </TabsContent>
                    <TabsContent value="enrollments">
                        <InstructorEnrollments />
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
            {course.coverDir && (
                <img
                    src={mediaUrl(course.coverDir)}
                    alt={course.title}
                    className="aspect-video w-full object-cover"
                />
            )}

            <CardHeader className="pb-2">
                <div className="flex items-start justify-between gap-2">
                    <CardTitle className="text-lg leading-snug">{course.title}</CardTitle>
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
                    {course.overallDuration && (
                        <span className="text-muted-foreground">
              {course.overallDuration}
            </span>
                    )}
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
                <div className="grid grid-cols-3 gap-2 border-t pt-3">
                    <InstructorQuizDialog
                        courseId={course.courseId}
                        onSaved={() =>
                            qc.invalidateQueries({ queryKey: ["instructor-quizzes"] })
                        }
                    />
                    <InstructorQuestionDialog
                        courseId={course.courseId}
                        onSaved={() =>
                            qc.invalidateQueries({ queryKey: ["instructor-questions"] })
                        }
                    />
                    <InstructorAnswerDialog
                        courseId={course.courseId}
                        onSaved={() =>
                            qc.invalidateQueries({ queryKey: ["instructor-answers"] })
                        }
                    />
                </div>
            </CardContent>
        </Card>
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
            setForm((f: any) => ({ ...f, coverDir: res.url }));
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
        coverDir: course.coverDir ?? "",
        categoryId: course.categoryId ?? [],
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
            setForm((f: any) => ({ ...f, coverDir: res.url }));
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
    const [form, setForm] = useState({ title: "", videoDir: "" });
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
            setForm((f) => ({ ...f, videoDir: res.url }));
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
            setForm({ title: "", videoDir: "" });
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
        videoDir: lesson.videoDir ?? "",
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
            setForm((f) => ({ ...f, videoDir: res.url }));
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
                {form.videoDir && (
                    <p className="text-xs text-muted-foreground truncate">
                        {form.videoDir}
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
// Enrollments placeholder
// ---------------------------------------------------------------------------

function InstructorEnrollments() {
    return (
        <Card>
            <CardContent className="py-10">
                <p className="text-center text-muted-foreground">
                    Enrollment management here...
                </p>
            </CardContent>
        </Card>
    );
}