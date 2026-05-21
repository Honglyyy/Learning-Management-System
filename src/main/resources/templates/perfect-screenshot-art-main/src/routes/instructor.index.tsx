import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api, mediaUrl } from "@/lib/api";
import { SiteHeader } from "@/components/SiteHeader";
import { RequireRole } from "@/components/RequireRole";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "@/components/ui/dialog";
import { ApiAlert } from "@/components/ApiAlert";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { toast } from "sonner";
import { Plus, Upload, Trash2, BookOpen, Layers, FileVideo, ListChecks, HelpCircle, CheckSquare } from "lucide-react";

export const Route = createFileRoute("/instructor/")({
  head: () => ({ meta: [{ title: "Instructor dashboard — Lumen LMS" }] }),
  component: () => <RequireRole roles={["INSTRUCTOR", "ADMIN"]}><Page /></RequireRole>,
});

function Page() {
  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <div className="container mx-auto px-4 py-10">
        <h1 className="mb-6 text-2xl font-semibold text-foreground">Instructor</h1>
        <Tabs defaultValue="courses">
          <TabsList><TabsTrigger value="courses">My courses</TabsTrigger><TabsTrigger value="manage">Manage content</TabsTrigger><TabsTrigger value="enrollments">Enrollments</TabsTrigger></TabsList>
          <TabsContent value="courses"><MyCourses /></TabsContent>
          <TabsContent value="manage"><ManageContent /></TabsContent>
          <TabsContent value="enrollments"><InstructorEnrollments /></TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

function ManageContent() {
  const { data: courses, isLoading } = useQuery<any[]>({
    queryKey: ["instructor-courses"],
    queryFn: () => api("/api/courses/instructor/me"),
  });
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
  const [section, setSection] = useState<"sections" | "lessons" | "quizzes" | "questions" | "answers">("sections");

  if (isLoading) return <Skeleton className="h-40 w-full" />;
  if (!courses || courses.length === 0) return <p className="text-muted-foreground">No courses yet.</p>;

  const coursId = selectedCourseId || courses[0]?.id;

  return (
    <div className="space-y-4">
      <div className="flex gap-4 items-center">
        <Label>Select Course:</Label>
        <Select value={String(coursId || "")} onValueChange={(v) => setSelectedCourseId(parseInt(v))}>
          <SelectTrigger className="w-80"><SelectValue /></SelectTrigger>
          <SelectContent>{courses.map((c) => <SelectItem key={c.id} value={String(c.id)}>{c.title}</SelectItem>)}</SelectContent>
        </Select>
      </div>
      <Tabs value={section} onValueChange={(v) => setSection(v as any)}>
        <TabsList><TabsTrigger value="sections"><Layers className="mr-2 h-4 w-4" />Sections</TabsTrigger><TabsTrigger value="lessons"><FileVideo className="mr-2 h-4 w-4" />Lessons</TabsTrigger><TabsTrigger value="quizzes"><ListChecks className="mr-2 h-4 w-4" />Quizzes</TabsTrigger><TabsTrigger value="questions"><HelpCircle className="mr-2 h-4 w-4" />Questions</TabsTrigger><TabsTrigger value="answers"><CheckSquare className="mr-2 h-4 w-4" />Answers</TabsTrigger></TabsList>
        <TabsContent value="sections"><InstructorSections courseId={coursId!} /></TabsContent>
        <TabsContent value="lessons"><InstructorLessons courseId={coursId!} /></TabsContent>
        <TabsContent value="quizzes"><InstructorQuizzes courseId={coursId!} /></TabsContent>
        <TabsContent value="questions"><InstructorQuestions courseId={coursId!} /></TabsContent>
        <TabsContent value="answers"><InstructorAnswers courseId={coursId!} /></TabsContent>
      </Tabs>
    </div>
  );
}

function InstructorSections({ courseId }: { courseId: number }) {
  const qc = useQueryClient();
  const { data: sections } = useQuery<any[]>({
    queryKey: ["instructor-sections", courseId],
    queryFn: () => api(`/api/sections?courseId=${courseId}`),
  });
  const del = useMutation({
    mutationFn: (id: number) => api(`/api/sections/${id}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["instructor-sections"] }); },
  });

  return (
    <div className="space-y-4">
      <div className="flex justify-end"><InstructorSectionDialog courseId={courseId} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-sections"] })} /></div>
      {!sections || sections.length === 0 ? (
        <p className="text-muted-foreground">No sections yet.</p>
      ) : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>Title</TableHead><TableHead>Duration</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {sections.map((s: any) => (
                <TableRow key={s.id}>
                  <TableCell>{s.title}</TableCell><TableCell>{s.duration}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <InstructorSectionDialog courseId={courseId} initial={s} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-sections"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(s.id); }}><Trash2 className="h-4 w-4" /></Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

function InstructorSectionDialog({ courseId, initial, onSaved }: { courseId: number; initial?: any; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { title: "", duration: "", courseId });
  const [error, setError] = useState<unknown>(null);

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, courseId };
      if (!initial) return api("/api/sections", { method: "POST", body });
      return api(`/api/sections/${initial.id}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { title: "", duration: "", courseId }); setError(null); } }}>
      <DialogTrigger asChild>{!initial ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New section</Button> : <Button size="icon" variant="ghost"><Layers className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{!initial ? "New section" : "Edit section"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Title</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
          <div className="space-y-1"><Label>Duration</Label><Input value={form.duration} onChange={(e) => setForm({ ...form, duration: e.target.value })} placeholder="e.g. 1 hour" /></div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function InstructorLessons({ courseId }: { courseId: number }) {
  const qc = useQueryClient();
  const { data: sections } = useQuery<any[]>({
    queryKey: ["instructor-sections", courseId],
    queryFn: () => api(`/api/sections?courseId=${courseId}`),
  });
  const { data: lessons } = useQuery<any[]>({
    queryKey: ["instructor-lessons", courseId],
    queryFn: () => api("/api/lessons"),
  });
  const del = useMutation({
    mutationFn: (id: number) => api(`/api/lessons/${id}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["instructor-lessons"] }); },
  });

  const courseLessons = lessons?.filter((l: any) => sections?.some((s) => s.id === l.sectionId)) || [];

  return (
    <div className="space-y-4">
      <div className="flex justify-end"><InstructorLessonDialog sections={sections || []} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-lessons"] })} /></div>
      {courseLessons.length === 0 ? (
        <p className="text-muted-foreground">No lessons yet.</p>
      ) : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>Title</TableHead><TableHead>Section</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {courseLessons.map((l: any) => (
                <TableRow key={l.id}>
                  <TableCell>{l.title}</TableCell><TableCell>{sections?.find((s) => s.id === l.sectionId)?.title}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <InstructorLessonDialog sections={sections || []} initial={l} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-lessons"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(l.id); }}><Trash2 className="h-4 w-4" /></Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

function InstructorLessonDialog({ sections, initial, onSaved }: { sections: any[]; initial?: any; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { title: "", videoDir: "", sectionId: "" });
  const [error, setError] = useState<unknown>(null);
  const [uploading, setUploading] = useState(false);

  async function uploadVideo(file: File) {
    setUploading(true);
    try {
      const fd = new FormData(); fd.append("file", file);
      const res = await api<any>("/api/uploads/lesson-video", { method: "POST", formData: fd });
      setForm((f: any) => ({ ...f, videoDir: res.url }));
      toast.success("Video uploaded");
    } catch (e) { setError(e); } finally { setUploading(false); }
  }

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, sectionId: Number(form.sectionId) };
      if (!initial) return api("/api/lessons", { method: "POST", body });
      return api(`/api/lessons/${initial.id}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { title: "", videoDir: "", sectionId: "" }); setError(null); } }}>
      <DialogTrigger asChild>{!initial ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New lesson</Button> : <Button size="icon" variant="ghost"><FileVideo className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{!initial ? "New lesson" : "Edit lesson"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Title</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
          <div className="space-y-1"><Label>Section</Label>
            <Select value={String(form.sectionId)} onValueChange={(v) => setForm({ ...form, sectionId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{sections.map((s) => <SelectItem key={s.id} value={String(s.id)}>{s.title}</SelectItem>)}</SelectContent>
            </Select>
          </div>
          <div className="space-y-1"><Label>Video</Label>
            <div className="flex items-center gap-2"><Input type="file" accept="video/*" onChange={(e) => e.target.files?.[0] && uploadVideo(e.target.files[0])} />{uploading && <Upload className="h-4 w-4 animate-pulse" />}</div>
            {form.videoDir && <p className="text-xs text-muted-foreground truncate">{form.videoDir}</p>}
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function InstructorQuizzes({ courseId }: { courseId: number }) {
  const qc = useQueryClient();
  const { data: lessons } = useQuery<any[]>({
    queryKey: ["instructor-lessons", courseId],
    queryFn: () => api("/api/lessons"),
  });
  const { data: quizzes } = useQuery<any[]>({
    queryKey: ["instructor-quizzes", courseId],
    queryFn: () => api("/api/quizzes"),
  });
  const del = useMutation({
    mutationFn: (id: number) => api(`/api/quizzes/${id}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["instructor-quizzes"] }); },
  });

  const courseQuizzes = quizzes?.filter((q: any) => lessons?.some((l) => l.id === q.lessonId)) || [];

  return (
    <div className="space-y-4">
      <div className="flex justify-end"><InstructorQuizDialog lessons={lessons || []} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-quizzes"] })} /></div>
      {courseQuizzes.length === 0 ? (
        <p className="text-muted-foreground">No quizzes yet.</p>
      ) : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>Title</TableHead><TableHead>Points</TableHead><TableHead>Lesson</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {courseQuizzes.map((q: any) => (
                <TableRow key={q.id}>
                  <TableCell>{q.title}</TableCell><TableCell>{q.totalPoints}</TableCell><TableCell>{lessons?.find((l) => l.id === q.lessonId)?.title}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <InstructorQuizDialog lessons={lessons || []} initial={q} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-quizzes"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(q.id); }}><Trash2 className="h-4 w-4" /></Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

function InstructorQuizDialog({ lessons, initial, onSaved }: { lessons: any[]; initial?: any; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { title: "", totalPoints: 10, lessonId: "" });
  const [error, setError] = useState<unknown>(null);

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, lessonId: Number(form.lessonId) };
      if (!initial) return api("/api/quizzes", { method: "POST", body });
      return api(`/api/quizzes/${initial.id}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { title: "", totalPoints: 10, lessonId: "" }); setError(null); } }}>
      <DialogTrigger asChild>{!initial ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New quiz</Button> : <Button size="icon" variant="ghost"><ListChecks className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{!initial ? "New quiz" : "Edit quiz"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Title</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
          <div className="space-y-1"><Label>Total Points</Label><Input type="number" value={form.totalPoints} onChange={(e) => setForm({ ...form, totalPoints: Number(e.target.value) })} /></div>
          <div className="space-y-1"><Label>Lesson</Label>
            <Select value={String(form.lessonId)} onValueChange={(v) => setForm({ ...form, lessonId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{lessons.map((l) => <SelectItem key={l.id} value={String(l.id)}>{l.title}</SelectItem>)}</SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function InstructorQuestions({ courseId }: { courseId: number }) {
  const qc = useQueryClient();
  const { data: quizzes } = useQuery<any[]>({
    queryKey: ["instructor-quizzes", courseId],
    queryFn: () => api("/api/quizzes"),
  });
  const { data: questions } = useQuery<any[]>({
    queryKey: ["instructor-questions", courseId],
    queryFn: () => api("/api/questions"),
  });
  const del = useMutation({
    mutationFn: (id: number) => api(`/api/questions/${id}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["instructor-questions"] }); },
  });

  const courseQuestions = questions?.filter((q: any) => quizzes?.some((z) => z.id === q.quizId)) || [];

  return (
    <div className="space-y-4">
      <div className="flex justify-end"><InstructorQuestionDialog quizzes={quizzes || []} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-questions"] })} /></div>
      {courseQuestions.length === 0 ? (
        <p className="text-muted-foreground">No questions yet.</p>
      ) : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>Question</TableHead><TableHead>Quiz</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {courseQuestions.map((q: any) => (
                <TableRow key={q.id}>
                  <TableCell className="max-w-xs truncate">{q.questionText}</TableCell><TableCell>{quizzes?.find((z) => z.id === q.quizId)?.title}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <InstructorQuestionDialog quizzes={quizzes || []} initial={q} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-questions"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(q.id); }}><Trash2 className="h-4 w-4" /></Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

function InstructorQuestionDialog({ quizzes, initial, onSaved }: { quizzes: any[]; initial?: any; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { questionText: "", quizId: "" });
  const [error, setError] = useState<unknown>(null);

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, quizId: Number(form.quizId) };
      if (!initial) return api("/api/questions", { method: "POST", body });
      return api(`/api/questions/${initial.id}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { questionText: "", quizId: "" }); setError(null); } }}>
      <DialogTrigger asChild>{!initial ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New question</Button> : <Button size="icon" variant="ghost"><HelpCircle className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{!initial ? "New question" : "Edit question"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Question</Label><Textarea value={form.questionText} onChange={(e) => setForm({ ...form, questionText: e.target.value })} /></div>
          <div className="space-y-1"><Label>Quiz</Label>
            <Select value={String(form.quizId)} onValueChange={(v) => setForm({ ...form, quizId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{quizzes.map((z) => <SelectItem key={z.id} value={String(z.id)}>{z.title}</SelectItem>)}</SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function InstructorAnswers({ courseId }: { courseId: number }) {
  const qc = useQueryClient();
  const { data: questions } = useQuery<any[]>({
    queryKey: ["instructor-questions", courseId],
    queryFn: () => api("/api/questions"),
  });
  const { data: answers } = useQuery<any[]>({
    queryKey: ["instructor-answers", courseId],
    queryFn: () => api("/api/answers"),
  });
  const del = useMutation({
    mutationFn: (id: number) => api(`/api/answers/${id}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["instructor-answers"] }); },
  });

  const courseAnswers = answers?.filter((a: any) => questions?.some((q) => q.id === a.questionId)) || [];

  return (
    <div className="space-y-4">
      <div className="flex justify-end"><InstructorAnswerDialog questions={questions || []} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-answers"] })} /></div>
      {courseAnswers.length === 0 ? (
        <p className="text-muted-foreground">No answers yet.</p>
      ) : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>Answer</TableHead><TableHead>Correct</TableHead><TableHead>Question</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {courseAnswers.map((a: any) => (
                <TableRow key={a.id}>
                  <TableCell className="max-w-xs truncate">{a.answerText}</TableCell><TableCell>{a.isCorrect ? <Badge>Yes</Badge> : <Badge variant="secondary">No</Badge>}</TableCell><TableCell className="max-w-xs truncate">{questions?.find((q) => q.id === a.questionId)?.questionText?.substring(0, 30)}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <InstructorAnswerDialog questions={questions || []} initial={a} onSaved={() => qc.invalidateQueries({ queryKey: ["instructor-answers"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(a.id); }}><Trash2 className="h-4 w-4" /></Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

function InstructorAnswerDialog({ questions, initial, onSaved }: { questions: any[]; initial?: any; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { answerText: "", isCorrect: false, questionId: "" });
  const [error, setError] = useState<unknown>(null);

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, questionId: Number(form.questionId) };
      if (!initial) return api("/api/answers", { method: "POST", body });
      return api(`/api/answers/${initial.id}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { answerText: "", isCorrect: false, questionId: "" }); setError(null); } }}>
      <DialogTrigger asChild>{!initial ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New answer</Button> : <Button size="icon" variant="ghost"><CheckSquare className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{!initial ? "New answer" : "Edit answer"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Answer</Label><Textarea value={form.answerText} onChange={(e) => setForm({ ...form, answerText: e.target.value })} /></div>
          <div className="space-y-1"><Label>Question</Label>
            <Select value={String(form.questionId)} onValueChange={(v) => setForm({ ...form, questionId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{questions.map((q) => <SelectItem key={q.id} value={String(q.id)}>{q.questionText?.substring(0, 50)}</SelectItem>)}</SelectContent>
            </Select>
          </div>
          <div className="flex items-center gap-2">
            <input type="checkbox" checked={form.isCorrect} onChange={(e) => setForm({ ...form, isCorrect: e.target.checked })} id="correct" />
            <Label htmlFor="correct">Mark as correct answer</Label>
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function MyCourses() {
  const qc = useQueryClient();
  const { data, isLoading, error } = useQuery<any[]>({
    queryKey: ["instructor-courses"],
    queryFn: () => api("/api/courses/instructor/me"),
  });

  return (
    <div className="space-y-4">
      <div className="flex justify-end"><CreateCourseDialog onCreated={() => qc.invalidateQueries({ queryKey: ["instructor-courses"] })} /></div>
      <ApiAlert error={error} />
      {isLoading ? <Skeleton className="h-40 w-full" /> : !data || data.length === 0 ? (
        <p className="text-muted-foreground">You haven't created any courses yet.</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {data.map((c: any) => (
            <Card key={c.id}>
              {c.coverDir && <img src={mediaUrl(c.coverDir)} alt={c.title} className="aspect-video w-full rounded-t-lg object-cover" />}
              <CardHeader><CardTitle className="text-base">{c.title}</CardTitle></CardHeader>
              <CardContent>
                <p className="line-clamp-2 text-sm text-muted-foreground">{c.description}</p>
                <div className="mt-2 flex items-center justify-between text-sm">
                  <span className="font-semibold">${Number(c.price ?? 0).toFixed(2)}</span>
                  {c.overallDuration && <span className="text-muted-foreground">{c.overallDuration}</span>}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

function CreateCourseDialog({ onCreated }: { onCreated: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ title: "", description: "", price: 0, overallDuration: "", coverDir: "" });
  const [error, setError] = useState<unknown>(null);
  const [uploading, setUploading] = useState(false);

  async function uploadCover(file: File) {
    setUploading(true);
    try {
      const fd = new FormData(); fd.append("file", file);
      const res = await api<any>("/api/uploads/course-cover", { method: "POST", formData: fd });
      setForm((f) => ({ ...f, coverDir: res.url }));
      toast.success("Cover uploaded.");
    } catch (e) { setError(e); } finally { setUploading(false); }
  }

  const create = useMutation({
    mutationFn: () => api("/api/courses/instructor/me", { method: "POST", body: { ...form, instructor: null, categoryId: [] } }),
    onSuccess: () => { toast.success("Course created."); setOpen(false); onCreated(); setForm({ title: "", description: "", price: 0, overallDuration: "", coverDir: "" }); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild><Button><Plus className="mr-1 h-4 w-4" />New course</Button></DialogTrigger>
      <DialogContent className="max-w-lg">
        <DialogHeader><DialogTitle>Create course</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Title</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
          <div className="space-y-1"><Label>Description</Label><Textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1"><Label>Price (USD)</Label><Input type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: parseFloat(e.target.value) || 0 })} /></div>
            <div className="space-y-1"><Label>Duration</Label><Input value={form.overallDuration} onChange={(e) => setForm({ ...form, overallDuration: e.target.value })} placeholder="e.g. 8 hours" /></div>
          </div>
          <div className="space-y-1">
            <Label>Cover image</Label>
            <div className="flex items-center gap-2">
              <Input type="file" accept="image/*" onChange={(e) => e.target.files?.[0] && uploadCover(e.target.files[0])} />
              {uploading && <span className="text-xs text-muted-foreground"><Upload className="inline h-3 w-3" /> uploading...</span>}
            </div>
            {form.coverDir && <p className="text-xs text-muted-foreground truncate">{form.coverDir}</p>}
          </div>
        </div>
        <DialogFooter><Button onClick={() => create.mutate()} disabled={create.isPending || !form.title}>{create.isPending ? "Creating..." : "Create"}</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function InstructorEnrollments() {
  const qc = useQueryClient();
  const { data, isLoading, error } = useQuery<any[]>({
    queryKey: ["instructor-enrollments"],
    queryFn: () => api("/api/enrollments"),
  });
  const update = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      api(`/api/enrollments/${id}/status`, { method: "PATCH", body: { status } }),
    onSuccess: () => { toast.success("Updated"); qc.invalidateQueries({ queryKey: ["instructor-enrollments"] }); },
  });
  const del = useMutation({
    mutationFn: (id: number) => api(`/api/enrollments/${id}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Removed"); qc.invalidateQueries({ queryKey: ["instructor-enrollments"] }); },
  });

  return (
    <div className="space-y-4">
      <ApiAlert error={error} />
      {isLoading ? <Skeleton className="h-40" /> : !data || data.length === 0 ? (
        <p className="text-muted-foreground">No enrollments.</p>
      ) : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>Student</TableHead><TableHead>Course</TableHead><TableHead>Status</TableHead><TableHead></TableHead></TableRow></TableHeader>
            <TableBody>
              {data.map((e: any) => (
                <TableRow key={e.id}>
                  <TableCell>{e.username || e.userEmail || `#${e.userId}`}</TableCell>
                  <TableCell>{e.courseTitle || `#${e.courseId}`}</TableCell>
                  <TableCell><Badge variant="secondary">{e.status}</Badge></TableCell>
                  <TableCell className="space-x-2 text-right">
                    {e.status !== "COMPLETED" && <Button size="sm" variant="outline" onClick={() => update.mutate({ id: e.id, status: "COMPLETED" })}>Mark complete</Button>}
                    <Button size="sm" variant="ghost" onClick={() => del.mutate(e.id)}>Remove</Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}
