import { createFileRoute } from "@tanstack/react-router";
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
import { Checkbox } from "@/components/ui/checkbox";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { ApiAlert } from "@/components/ApiAlert";
import { CrudPanel } from "@/components/CrudPanel";
import { MultiSelect } from "@/components/MultiSelect";
import { SelectFetch } from "@/components/SelectFetch";
import { toast } from "sonner";
import { LayoutDashboard, BookOpen, FolderTree, Layers, FileVideo, ListChecks, HelpCircle, CheckSquare, CreditCard, Users, Shield, Plus, Trash2, Upload, Check, FileText } from "lucide-react";

export const Route = createFileRoute("/admin/")({
  head: () => ({ meta: [{ title: "Admin dashboard — Lumen LMS" }] }),
  component: () => <RequireRole roles={["ADMIN"]}><AdminPage /></RequireRole>,
});

type Section = "overview" | "courses" | "categories" | "sections" | "lessons" | "assignments" | "quizzes" | "questions" | "answers" | "payments" | "enrollments" | "users" | "admins";

function AdminPage() {
  const [section, setSection] = useState<Section>("overview");
  const items: { key: Section; label: string; icon: any }[] = [
    { key: "overview", label: "Overview", icon: LayoutDashboard },
    { key: "users", label: "Users", icon: Users },
    { key: "categories", label: "Categories", icon: FolderTree },
    { key: "enrollments", label: "Enrollments", icon: Users },
    { key: "courses", label: "Courses", icon: BookOpen },
    { key: "sections", label: "Sections", icon: Layers },
    { key: "lessons", label: "Lessons", icon: FileVideo },
    { key: "assignments", label: "Assignments", icon: FileText },
    { key: "quizzes", label: "Quizzes", icon: ListChecks },
    { key: "questions", label: "Questions", icon: HelpCircle },
    { key: "answers", label: "Answers", icon: CheckSquare },
    { key: "payments", label: "Payments", icon: CreditCard },
    // { key: "admins", label: "Admin users", icon: Shield },
  ];

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <div className="container mx-auto flex gap-6 px-4 py-8">
        <aside className="hidden w-56 shrink-0 md:block">
          <nav className="sticky top-20 space-y-1">
            {items.map((it) => {
              const Icon = it.icon;
              const active = section === it.key;
              return (
                <button
                  key={it.key}
                  onClick={() => setSection(it.key)}
                  className={`flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition ${active ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-accent hover:text-foreground"}`}
                >
                  <Icon className="h-4 w-4" />{it.label}
                </button>
              );
            })}
          </nav>
        </aside>
        <main className="flex-1 min-w-0">
          {section === "overview" && <Overview />}
          {section === "courses" && <AdminCourses />}
          {section === "categories" && <CrudPanel cfg={{
            resource: "categories", title: "Categories", queryKey: "admin-categories",
            fields: [{ name: "category", label: "Name" }],
            columns: [{ key: "categoryId", label: "ID" }, { key: "category", label: "Name" }],
            emptyForm: { category: "" },
          }} />}
          {section === "sections" && <SectionPanel />}
          {section === "lessons" && <AdminLessons />}
          {section === "assignments" && <AdminAssignments />}
          {section === "quizzes" && <QuizPanel />}
          {section === "questions" && <QuestionPanel />}
          {section === "answers" && <AnswerPanel />}
          {section === "payments" && <AdminPayments />}
          {section === "enrollments" && <AdminEnrollments />}
          {section === "users" && <AdminUsers />}
          {/*{section === "admins" && <CreateAdmin />}*/}
        </main>
      </div>
    </div>
  );
}

function Overview() {
  const courses = useQuery<any[]>({ queryKey: ["o-courses"], queryFn: () => api("/api/courses", { auth: false }) });
  const payments = useQuery<any[]>({ queryKey: ["o-payments"], queryFn: () => api("/api/payments") });
  const enrollments = useQuery<any[]>({ queryKey: ["o-enrollments"], queryFn: () => api("/api/enrollments") });

  const stats = [
    { label: "Courses", value: courses.data?.length ?? "—" },
    { label: "Enrollments", value: enrollments.data?.length ?? "—" },
    { label: "Payments", value: payments.data?.length ?? "—" },
    { label: "Revenue (paid)", value: payments.data ? "$" + payments.data.filter((p: any) => p.status === "PAID").reduce((s: number, p: any) => s + Number(p.amount || 0), 0).toFixed(2) : "—" },
  ];

  return (
    <div className="space-y-6">
      <h2 className="text-lg font-semibold text-foreground">Overview</h2>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((s) => (
          <Card key={s.label}><CardHeader><p className="text-sm text-muted-foreground">{s.label}</p><CardTitle className="text-3xl">{s.value}</CardTitle></CardHeader></Card>
        ))}
      </div>
    </div>
  );
}

function AdminCourses() {
  const qc = useQueryClient();
  const { data, isLoading, error } = useQuery<any[]>({ queryKey: ["admin-courses"], queryFn: () => api("/api/courses", { auth: false }) });
  const del = useMutation({
    mutationFn: (courseId: number) => api(`/api/courses/${courseId}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["admin-courses"] }); },
    onError: (e: any) => toast.error(e.message),
  });

  console.log(data)
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Courses</h2>
        <CourseDialog mode="create" onSaved={() => qc.invalidateQueries({ queryKey: ["admin-courses"] })} />
      </div>
      <ApiAlert error={error} />
      {isLoading ? <Skeleton className="h-40" /> : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>Title</TableHead>
                <TableHead>Cover</TableHead>
                <TableHead>Categories</TableHead>
                <TableHead>Price</TableHead>
                <TableHead>Duration</TableHead>
                <TableHead>Instructor</TableHead>
                <TableHead>Rating</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data?.map((c: any) => (
                <TableRow key={c.courseId}>
                  <TableCell>{c.courseId}</TableCell>
                  <TableCell>{c.title}</TableCell>
                  <TableCell><img src={mediaUrl(c.coverUrl || c.coverDir)} alt="" width={"100px"} className="rounded object-cover max-h-16" /></TableCell>
                  <TableCell>{c.categories?.join(" , ") || "—"}</TableCell>
                  <TableCell>${Number(c.price ?? 0).toFixed(2)}</TableCell>
                  <TableCell>{c.overallDuration || "—"}</TableCell>
                  <TableCell>{c.instructor || "—"}</TableCell>
                  <TableCell>{c.rating != null && c.rating > 0 ? `⭐ ${Number(c.rating).toFixed(1)}` : "—"}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <CourseDialog mode="edit" initial={c} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-courses"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete course?")) del.mutate(c.courseId); }}><Trash2 className="h-4 w-4" /></Button>
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

function CourseDialog({ mode, initial, onSaved }: { mode: "create" | "edit"; initial?: any; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { title: "", description: "", price: 0, overallDuration: "", coverUrl: "", coverPublicId: "", coverDir: "", instructor: 1, categoryId: [] });
  const [error, setError] = useState<unknown>(null);
  const [uploading, setUploading] = useState(false);
  const { data: categories, isLoading: catsLoading } = useQuery<any[]>({
    queryKey: ["admin-categories"],
    queryFn: () => api("/api/categories"),
  });

  async function uploadCover(file: File) {
    setUploading(true);
    try {
      const fd = new FormData(); fd.append("file", file);
      const res = await api<any>("/api/uploads/course-cover", { method: "POST", formData: fd });
      setForm((f: any) => ({ ...f, coverUrl: res.url, coverPublicId: res.publicId, coverDir: res.url }));
    } catch (e) { setError(e); } finally { setUploading(false); }
  }

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form };
      if (mode === "create") return api("/api/courses", { method: "POST", body });
      const courseId = initial.courseId || initial.id;
      return api(`/api/courses/${courseId}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  const catOptions =
      categories?.map((c) => ({
        id: c.categoryId,
        label: c.category?.toLowerCase(),
      })) || [];

  return (
    <Dialog open={open}
        onOpenChange={(o) => { setOpen(o); if (o) { setForm(
          initial
              ? {
                ...initial,
                coverUrl: initial.coverUrl ?? initial.coverDir ?? "",
                coverPublicId: initial.coverPublicId ?? "",
                coverDir: initial.coverUrl ?? initial.coverDir ?? "",
                categoryId:
                    initial.categories?.map(
                        (cat: any) => {
                          const found = categories?.find(
                              (c) =>
                                  c.category === cat
                          );

                          return found?.categoryId;
                        }
                    ).filter(Boolean) || [],
              }
              : {
                title: "",
                description: "",
                price: 0,
                overallDuration: "",
                coverUrl: "",
                coverPublicId: "",
                coverDir: "",
                instructor: 1,
                categoryId: [],
              }
          );
          }
        }
      }
    >
      <DialogTrigger asChild>{mode === "create" ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New course</Button> : <Button size="icon" variant="ghost"><LayoutDashboard className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent className="max-w-lg">
        <DialogHeader><DialogTitle>{mode === "create" ? "New course" : "Edit course"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Title</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
          <div className="space-y-1"><Label>Description</Label><Textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1"><Label>Price</Label><Input type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: parseFloat(e.target.value) || 0 })} /></div>
            <div className="space-y-1"><Label>Duration</Label><Input value={form.overallDuration} onChange={(e) => setForm({ ...form, overallDuration: e.target.value })} /></div>
          </div>
          <div className="space-y-1"><Label>Instructor ID</Label><Input type="number" value={form.instructorId || form.instructor || ""} onChange={(e) => setForm({ ...form, instructor: parseInt(e.target.value) || null })} /></div>
          <div className="space-y-1"><Label>Categories</Label>{catsLoading ? <Skeleton className="h-10" /> : <MultiSelect options={catOptions} selected={form.categoryId||[]} onChange={(ids) => setForm({ ...form, categoryId: ids })} placeholder="Select categories..." />}</div>
          <div className="space-y-1">
            <Label>Cover</Label>
            <div className="flex items-center gap-2">
              <Input type="file" accept="image/*" onChange={(e) => e.target.files?.[0] && uploadCover(e.target.files[0])} />
              {uploading && <Upload className="h-4 w-4 animate-pulse" />}
            </div>
            {(form.coverUrl || form.coverDir) && <img src={mediaUrl(form.coverUrl || form.coverDir)} alt="cover" className="mt-2 h-24 rounded object-cover" />}
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>{save.isPending ? "Saving..." : "Save"}</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function AdminLessons() {
  const qc = useQueryClient();
  const list = useQuery<any[]>({ queryKey: ["admin-lessons"], queryFn: () => api("/api/lessons") });
  const { data: sections } = useQuery<any[]>({ queryKey: ["admin-sections"], queryFn: () => api("/api/sections") });
  const del = useMutation({ mutationFn: (lessonId: number) => api(`/api/lessons/${lessonId}`, { method: "DELETE" }), onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["admin-lessons"] }); } });
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between"><h2 className="text-lg font-semibold">Lessons</h2><LessonDialog sections={sections || []} mode="create" onSaved={() => qc.invalidateQueries({ queryKey: ["admin-lessons"] })} /></div>
      <ApiAlert error={list.error} />
      {list.isLoading ? <Skeleton className="h-40" /> : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>ID</TableHead><TableHead>Title</TableHead><TableHead>Section</TableHead><TableHead>Video</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {list.data?.map((l: any) => (
                <TableRow key={l.lessonId}>
                  <TableCell>{l.lessonId}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <span>{l.title}</span>
                      {l.isFree && (
                        <Badge variant="outline" className="text-[10px] px-1.5 py-0 bg-emerald-50 text-emerald-700 border-emerald-300 shrink-0">
                          Free Trial
                        </Badge>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>{l.sectionName || `Section #${l.sectionId}`}</TableCell>
                  <TableCell className="max-w-xs truncate text-xs text-muted-foreground">
                    {(l.videoUrl || l.videoDir) && <video src={mediaUrl(l.videoUrl || l.videoDir)} width={"100px"} controls/>}
                  </TableCell>
                  <TableCell className="space-x-1 text-right">
                    <LessonDialog sections={sections || []} mode="edit" initial={l} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-lessons"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(l.lessonId); }}><Trash2 className="h-4 w-4" /></Button>
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

function LessonDialog({ mode, sections,initial, onSaved }: { mode: "create" | "edit"; initial?: any; sections:any[]; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial ? {
    ...initial,
    videoUrl: initial.videoUrl ?? initial.videoDir ?? "",
    videoPublicId: initial.videoPublicId ?? "",
    videoDir: initial.videoUrl ?? initial.videoDir ?? "",
    isFree: initial.isFree ?? false,
  } : { title: "", videoUrl: "", videoPublicId: "", videoDir: "", sectionId: "", isFree: false });
  const [error, setError] = useState<unknown>(null);
  const [uploading, setUploading] = useState(false);

  async function uploadVideo(file: File) {
    setUploading(true);
    try {
      const fd = new FormData(); fd.append("file", file);
      const res = await api<any>("/api/uploads/lesson-video", { method: "POST", formData: fd });
      setForm((f: any) => ({ ...f, videoUrl: res.url, videoPublicId: res.publicId, videoDir: res.url }));
      toast.success("Video uploaded");
    } catch (e) { setError(e); } finally { setUploading(false); }
  }

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, sectionId: Number(form.sectionId) };
      if (mode === "create") return api("/api/lessons", { method: "POST", body });
      return api(`/api/lessons/${initial.lessonId}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial ? {
      ...initial,
      videoUrl: initial.videoUrl ?? initial.videoDir ?? "",
      videoPublicId: initial.videoPublicId ?? "",
      videoDir: initial.videoUrl ?? initial.videoDir ?? "",
      orderIndex: initial.orderIndex ?? 0,
      duration: initial.duration ?? "",
      description: initial.description ?? "",
      textContent: initial.textContent ?? "",
      isFree: initial.isFree ?? false,
    } : {
      title: "",
      videoUrl: "",
      videoPublicId: "",
      videoDir: "",
      sectionId: "",
      orderIndex: 0,
      duration: "",
      description: "",
      textContent: "",
      isFree: false,
    }); setError(null); } }}>
      <DialogTrigger asChild>{mode === "create" ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New lesson</Button> : <Button size="icon" variant="ghost"><FileVideo className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader><DialogTitle>{mode === "create" ? "New lesson" : "Edit lesson"}</DialogTitle></DialogHeader>
        <div className="space-y-3 max-h-[70vh] overflow-y-auto px-1">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Title</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
          <div className="space-y-1"><Label>Section</Label>
            <Select value={String(form.sectionId)} onValueChange={(v) => setForm({ ...form, sectionId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{sections.map((s) => <SelectItem key={s.sectionId} value={String(s.sectionId)}>{s.title}</SelectItem>)}</SelectContent>
            </Select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label>Order Index</Label>
              <Input type="number" value={form.orderIndex ?? 0} onChange={(e) => setForm({ ...form, orderIndex: parseInt(e.target.value) || 0 })} />
            </div>
            <div className="space-y-1">
              <Label>Duration</Label>
              <Input placeholder="e.g. 15:30" value={form.duration ?? ""} onChange={(e) => setForm({ ...form, duration: e.target.value })} />
            </div>
          </div>
          <div className="space-y-1"><Label>Video</Label>
            <div className="flex items-center gap-2"><Input type="file" accept="video/*" onChange={(e) => e.target.files?.[0] && uploadVideo(e.target.files[0])} />{uploading && <Upload className="h-4 w-4 animate-pulse" />}</div>
            {(form.videoUrl || form.videoDir) && <p className="text-xs text-muted-foreground truncate">{form.videoUrl || form.videoDir}</p>}
          </div>
          <div className="space-y-1">
            <Label>Description</Label>
            <Textarea rows={2} placeholder="Brief description of this lesson" value={form.description ?? ""} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div className="space-y-1">
            <Label>Reading Text / Notes</Label>
            <Textarea rows={3} placeholder="Supplemental text or instructions" value={form.textContent ?? ""} onChange={(e) => setForm({ ...form, textContent: e.target.value })} />
          </div>
          <div className="flex items-start space-x-2 rounded-md border p-3 bg-muted/20">
            <Checkbox
              id="adminLessonIsFree"
              checked={Boolean(form.isFree)}
              onCheckedChange={(checked) =>
                setForm({ ...form, isFree: checked === true })
              }
            />
            <div className="grid gap-1 leading-none">
              <Label htmlFor="adminLessonIsFree" className="font-medium cursor-pointer">
                Free Trial / Preview Lesson
              </Label>
              <p className="text-xs text-muted-foreground">
                Allow prospective students to watch this video without enrolling in the course.
              </p>
            </div>
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

const PAYMENT_STATUSES = ["PENDING", "PAID", "FAILED", "REFUNDED"];

function AdminPayments() {
  const qc = useQueryClient();
  const list = useQuery<any[]>({ queryKey: ["admin-payments"], queryFn: () => api("/api/payments") });
  const update = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      api(`/api/payments/${id}/status`, { method: "PATCH", body: { status, providerReference: "admin-confirmed" } }),
    onSuccess: (_, vars) => {
      if (vars.status === "PAID") {
        toast.success("Payment confirmed and student enrolled!");
      } else {
        toast.success(`Payment status updated to ${vars.status}`);
      }
      qc.invalidateQueries({ queryKey: ["admin-payments"] });
      qc.invalidateQueries({ queryKey: ["admin-enrollments"] });
    },
    onError: () => toast.error("Failed to update payment status"),
  });
  const del = useMutation({ mutationFn: (id: number) => api(`/api/payments/${id}`, { method: "DELETE" }), onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["admin-payments"] }); } });

  const pendingCount = list.data?.filter((p: any) => p.status === "PENDING").length || 0;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold">Payments</h2>
          <p className="text-xs text-muted-foreground">
            Approve payments made via ABA PayWay. Confirming a payment automatically activates the student's enrollment.
          </p>
        </div>
        {pendingCount > 0 && (
          <Badge variant="outline" className="bg-amber-50 text-amber-800 border-amber-300 font-medium">
            {pendingCount} Pending Confirmation
          </Badge>
        )}
      </div>

      <ApiAlert error={list.error} />
      {list.isLoading ? <Skeleton className="h-40" /> : (
        <div className="rounded-md border border-border bg-card overflow-x-auto">
          <Table>
            <TableHeader><TableRow><TableHead>ID</TableHead><TableHead>Student</TableHead><TableHead>Course</TableHead><TableHead>Amount</TableHead><TableHead>Provider</TableHead><TableHead>Reference</TableHead><TableHead>Status</TableHead><TableHead>Date</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {list.data?.map((p: any) => (
                <TableRow key={p.paymentId || p.id} className={p.status === "PENDING" ? "bg-amber-50/40 dark:bg-amber-950/10" : ""}>
                  <TableCell>{p.paymentId || p.id}</TableCell>
                  <TableCell className="font-medium">{p.username || p.userEmail}</TableCell>
                  <TableCell>{p.courseTitle}</TableCell>
                  <TableCell className="font-semibold">${Number(p.amount ?? 0).toFixed(2)}</TableCell>
                  <TableCell>
                    <Badge variant="outline" className="text-xs">
                      {p.provider || "ABA_PAYWAY"}
                    </Badge>
                  </TableCell>
                  <TableCell className="font-mono text-xs">{p.providerReference}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Select value={p.status} onValueChange={(v) => update.mutate({ id: p.paymentId || p.id, status: v })}>
                        <SelectTrigger className="w-28 h-8 text-xs"><SelectValue /></SelectTrigger>
                        <SelectContent>{PAYMENT_STATUSES.map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}</SelectContent>
                      </Select>
                      {p.status === "PENDING" && (
                        <Button
                          size="sm"
                          className="bg-emerald-600 hover:bg-emerald-700 text-white h-8 text-xs gap-1 font-medium whitespace-nowrap"
                          disabled={update.isPending}
                          onClick={() => update.mutate({ id: p.paymentId || p.id, status: "PAID" })}
                        >
                          <Check className="h-3.5 w-3.5" /> Confirm & Enroll
                        </Button>
                      )}
                    </div>
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground whitespace-nowrap">
                    {p.createdAt ? new Date(p.createdAt).toLocaleDateString() : "—"}
                  </TableCell>
                  <TableCell className="text-right">
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete payment?")) del.mutate(p.paymentId || p.id); }}>
                      <Trash2 className="h-4 w-4 text-muted-foreground hover:text-destructive" />
                    </Button>
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

const ENROLL_STATUSES = ["ACTIVE", "COMPLETED", "CANCELLED"];

function AdminEnrollments() {
  const qc = useQueryClient();
  const list = useQuery<any[]>({ queryKey: ["admin-enrollments"], queryFn: () => api("/api/enrollments") });
  const update = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) => api(`/api/enrollments/${id}/status`, { method: "PATCH", body: { status } }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin-enrollments"] }),
  });
  const del = useMutation({ mutationFn: (id: number) => api(`/api/enrollments/${id}`, { method: "DELETE" }), onSuccess: () => qc.invalidateQueries({ queryKey: ["admin-enrollments"] }) });

  const [creating, setCreating] = useState(false);
  const [newForm, setNewForm] = useState({ userId: "", courseId: "", status: "ACTIVE" });
  const create = useMutation({
    mutationFn: () => api("/api/enrollments/manage", { method: "POST", body: { userId: Number(newForm.userId), courseId: Number(newForm.courseId), status: newForm.status } }),
    onSuccess: () => { toast.success("Enrolled"); setCreating(false); setNewForm({ userId: "", courseId: "", status: "ACTIVE" }); qc.invalidateQueries({ queryKey: ["admin-enrollments"] }); },
    onError: (e: any) => toast.error(e.message),
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Enrollments</h2>
        <Dialog open={creating} onOpenChange={setCreating}>
          <DialogTrigger asChild><Button size="sm"><Plus className="mr-1 h-4 w-4" />Enroll user</Button></DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>Manage enrollment</DialogTitle></DialogHeader>
            <div className="space-y-3">
              <div className="space-y-1"><Label>User ID</Label><Input type="number" value={newForm.userId} onChange={(e) => setNewForm({ ...newForm, userId: e.target.value })} /></div>
              <div className="space-y-1"><Label>Course ID</Label><Input type="number" value={newForm.courseId} onChange={(e) => setNewForm({ ...newForm, courseId: e.target.value })} /></div>
              <div className="space-y-1"><Label>Status</Label>
                <Select value={newForm.status} onValueChange={(v) => setNewForm({ ...newForm, status: v })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>{ENROLL_STATUSES.map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}</SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter><Button onClick={() => create.mutate()} disabled={create.isPending}>Enroll</Button></DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
      <ApiAlert error={list.error} />
      {list.isLoading ? <Skeleton className="h-40" /> : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>ID</TableHead><TableHead>User</TableHead><TableHead>Course</TableHead><TableHead>Status</TableHead><TableHead>Enrolled At</TableHead><TableHead></TableHead></TableRow></TableHeader>
            <TableBody>
              {list.data?.map((e: any) => (
                <TableRow key={e.enrollmentId}>
                  <TableCell>{e.enrollmentId}</TableCell>
                  <TableCell>{e.username || e.userEmail || `#${e.userId}`}</TableCell>
                  <TableCell>{e.courseTitle || `#${e.courseId}`}</TableCell>
                  <TableCell>
                    <Select value={e.status} onValueChange={(v) => update.mutate({ id: e.enrollmentId, status: v })}>
                      <SelectTrigger className="w-32"><SelectValue /></SelectTrigger>
                      <SelectContent>{ENROLL_STATUSES.map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}</SelectContent>
                    </Select>
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground whitespace-nowrap">
                    {e.enrolledAt ? new Date(e.enrolledAt).toLocaleDateString() : "—"}
                  </TableCell>
                  <TableCell><Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(e.enrollmentId); }}><Trash2 className="h-4 w-4" /></Button></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

function SectionPanel() {
  const qc = useQueryClient();
  const list = useQuery<any[]>({ queryKey: ["admin-sections"], queryFn: () => api("/api/sections") });
  const { data: courses } = useQuery<any[]>({ queryKey: ["admin-courses"], queryFn: () => api("/api/courses") });
  const del = useMutation({ mutationFn: (sectionId: number) => api(`/api/sections/${sectionId}`, { method: "DELETE" }), onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["admin-sections"] }); } });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between"><h2 className="text-lg font-semibold">Sections</h2><SectionDialog mode="create" courses={courses || []} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-sections"] })} /></div>
      <ApiAlert error={list.error} />
      {list.isLoading ? <Skeleton className="h-40" /> : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>ID</TableHead><TableHead>Title</TableHead><TableHead>Duration</TableHead><TableHead>Course</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {list.data?.map((s: any) => (
                <TableRow key={s.sectionId || s.id}>
                  <TableCell>{s.sectionId}</TableCell><TableCell>{s.title}</TableCell><TableCell>{s.duration}</TableCell><TableCell>{s.courseName || courses?.find((c) => c.courseId === s.courseId)?.title || s.courseId}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <SectionDialog mode="edit" initial={s} courses={courses || []} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-sections"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(s.sectionId); }}><Trash2 className="h-4 w-4" /></Button>
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

function SectionDialog({ mode, initial, courses, onSaved }: { mode: "create" | "edit"; initial?: any; courses: any[]; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { title: "", duration: "", courseId: "" });
  const [error, setError] = useState<unknown>(null);

  // console.log(courses.map(c => c.courseId))
  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, courseId: Number(form.courseId) };
      if (mode === "create") return api("/api/sections", { method: "POST", body });
      return api(`/api/sections/${initial.sectionId}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { title: "", duration: "", courseId: "" }); setError(null); } }}>
      <DialogTrigger asChild>{mode === "create" ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New section</Button> : <Button size="icon" variant="ghost"><Layers className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{mode === "create" ? "New section" : "Edit section"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Title</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
          <div className="space-y-1"><Label>Duration</Label><Input value={form.duration} onChange={(e) => setForm({ ...form, duration: e.target.value })} placeholder="e.g. 1 hour" /></div>
          <div className="space-y-1"><Label>Course</Label>
            <Select value={String(form.courseId)} onValueChange={(v) => setForm({ ...form, courseId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{courses.map((c) => <SelectItem key={c.courseId} value={String(c.courseId)}>{c.title}</SelectItem>)}</SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function QuizPanel() {
  const qc = useQueryClient();
  const list = useQuery<any[]>({ queryKey: ["admin-quizzes"], queryFn: () => api("/api/quizzes") });
  const { data: lessons } = useQuery<any[]>({ queryKey: ["admin-lessons"], queryFn: () => api("/api/lessons") });
  const del = useMutation({ mutationFn: (quizId: number) => api(`/api/quizzes/${quizId}`, { method: "DELETE" }), onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["admin-quizzes"] }); } });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between"><h2 className="text-lg font-semibold">Quizzes</h2><QuizDialog mode="create" lessons={lessons || []} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-quizzes"] })} /></div>
      <ApiAlert error={list.error} />
      {list.isLoading ? <Skeleton className="h-40" /> : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>ID</TableHead><TableHead>Title</TableHead><TableHead>Points</TableHead><TableHead>Lesson</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {list.data?.map((q: any) => (
                <TableRow key={q.quizId}>
                  <TableCell>{q.quizId}</TableCell><TableCell>{q.title}</TableCell><TableCell>{q.totalPoints}</TableCell><TableCell>{q.lessonName || lessons?.find((l) => l.lessonId === q.lessonId)?.title || q.lessonId}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <QuizDialog mode="edit" initial={q} lessons={lessons || []} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-quizzes"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(q.quizId); }}><Trash2 className="h-4 w-4" /></Button>
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

function QuizDialog({ mode, initial, lessons, onSaved }: { mode: "create" | "edit"; initial?: any; lessons: any[]; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { title: "", totalPoints: 10, lessonId: "" });
  const [error, setError] = useState<unknown>(null);

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, lessonId: Number(form.lessonId) };
      if (mode === "create") return api("/api/quizzes", { method: "POST", body });
      return api(`/api/quizzes/${initial.quizId}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { title: "", totalPoints: 10, lessonId: "" }); setError(null); } }}>
      <DialogTrigger asChild>{mode === "create" ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New quiz</Button> : <Button size="icon" variant="ghost"><ListChecks className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{mode === "create" ? "New quiz" : "Edit quiz"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Title</Label><Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
          <div className="space-y-1"><Label>Total Points</Label><Input type="number" value={form.totalPoints} onChange={(e) => setForm({ ...form, totalPoints: Number(e.target.value) })} /></div>
          <div className="space-y-1"><Label>Lesson</Label>
            <Select value={String(form.lessonId)} onValueChange={(v) => setForm({ ...form, lessonId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{lessons.map((l) => <SelectItem key={l.lessonId} value={String(l.lessonId)}>{l.title}</SelectItem>)}</SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function QuestionPanel() {
  const qc = useQueryClient();
  const list = useQuery<any[]>({ queryKey: ["admin-questions"], queryFn: () => api("/api/questions") });
  const { data: quizzes } = useQuery<any[]>({ queryKey: ["admin-quizzes"], queryFn: () => api("/api/quizzes") });
  const del = useMutation({ mutationFn: (id: number) => api(`/api/questions/${id}`, { method: "DELETE" }), onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["admin-questions"] }); } });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between"><h2 className="text-lg font-semibold">Questions</h2><QuestionDialog mode="create" quizzes={quizzes || []} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-questions"] })} /></div>
      <ApiAlert error={list.error} />
      {list.isLoading ? <Skeleton className="h-40" /> : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>ID</TableHead><TableHead>Question</TableHead><TableHead>Points</TableHead><TableHead>Quiz</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {list.data?.map((q: any) => (
                <TableRow key={q.questionId}>
                  <TableCell>{q.questionId}</TableCell>
                  <TableCell className="max-w-xs truncate">{q.questionText}</TableCell>
                  <TableCell><Badge variant="outline">{q.point != null ? `${q.point} pts` : "Auto"}</Badge></TableCell>
                  <TableCell>{quizzes?.find((z) => z.quizId === q.quizId)?.title || q.quizId}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <QuestionDialog mode="edit" initial={q} quizzes={quizzes || []} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-questions"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(q.questionId); }}><Trash2 className="h-4 w-4" /></Button>
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

function QuestionDialog({ mode, initial, quizzes, onSaved }: { mode: "create" | "edit"; initial?: any; quizzes: any[]; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { questionText: "", point: "", quizId: "" });
  const [error, setError] = useState<unknown>(null);

  const save = useMutation({
    mutationFn: () => {
      const body = {
        ...form,
        quizId: Number(form.quizId),
        point: form.point === "" || form.point == null ? null : Number(form.point),
      };
      if (mode === "create") return api("/api/questions", { method: "POST", body });
      return api(`/api/questions/${initial.questionId}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { questionText: "", point: "", quizId: "" }); setError(null); } }}>
      <DialogTrigger asChild>{mode === "create" ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New question</Button> : <Button size="icon" variant="ghost"><HelpCircle className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{mode === "create" ? "New question" : "Edit question"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Question</Label><Textarea value={form.questionText} onChange={(e) => setForm({ ...form, questionText: e.target.value })} /></div>
          <div className="space-y-1">
            <Label>Point Value</Label>
            <Input
              type="number"
              placeholder="e.g. 1"
              value={form.point ?? ""}
              onChange={(e) => setForm({ ...form, point: e.target.value })}
            />
          </div>
          <div className="space-y-1"><Label>Quiz</Label>
            <Select value={String(form.quizId)} onValueChange={(v) => setForm({ ...form, quizId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{quizzes.map((z) => <SelectItem key={z.quizId} value={String(z.quizId)}>{z.title}</SelectItem>)}</SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function AnswerPanel() {
  const qc = useQueryClient();
  const list = useQuery<any[]>({ queryKey: ["admin-answers"], queryFn: () => api("/api/answers") });
  const { data: questions } = useQuery<any[]>({ queryKey: ["admin-questions"], queryFn: () => api("/api/questions") });
  const del = useMutation({ mutationFn: (id: number) => api(`/api/answers/${id}`, { method: "DELETE" }), onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: ["admin-answers"] }); } });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between"><h2 className="text-lg font-semibold">Answers</h2><AnswerDialog mode="create" questions={questions || []} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-answers"] })} /></div>
      <ApiAlert error={list.error} />
      {list.isLoading ? <Skeleton className="h-40" /> : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow><TableHead>ID</TableHead><TableHead>Answer</TableHead><TableHead>Correct</TableHead><TableHead>Question</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {list.data?.map((a: any) => (
                <TableRow key={a.answerId}>
                  <TableCell>{a.answerId}</TableCell><TableCell className="max-w-xs truncate">{a.answerText}</TableCell><TableCell>{a.isCorrect ? <Badge>Yes</Badge> : <Badge variant="secondary">No</Badge>}</TableCell><TableCell>{questions?.find((q) => q.questionId === a.questionId)?.questionText?.substring(0, 30) || a.questionId}</TableCell>
                  <TableCell className="space-x-1 text-right">
                    <AnswerDialog mode="edit" initial={a} questions={questions || []} onSaved={() => qc.invalidateQueries({ queryKey: ["admin-answers"] })} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete?")) del.mutate(a.answerId); }}><Trash2 className="h-4 w-4" /></Button>
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

function AnswerDialog({ mode, initial, questions, onSaved }: { mode: "create" | "edit"; initial?: any; questions: any[]; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<any>(() => initial || { answerText: "", isCorrect: false, questionId: "" });
  const [error, setError] = useState<unknown>(null);

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, questionId: Number(form.questionId) };
      if (mode === "create") return api("/api/answers", { method: "POST", body });
      return api(`/api/answers/${initial.answerId}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); onSaved(); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { answerText: "", isCorrect: false, questionId: "" }); setError(null); } }}>
      <DialogTrigger asChild>{mode === "create" ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New answer</Button> : <Button size="icon" variant="ghost"><CheckSquare className="h-4 w-4" /></Button>}</DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>{mode === "create" ? "New answer" : "Edit answer"}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          <div className="space-y-1"><Label>Answer</Label><Textarea value={form.answerText} onChange={(e) => setForm({ ...form, answerText: e.target.value })} /></div>
          <div className="space-y-1"><Label>Question</Label>
            <Select value={String(form.questionId)} onValueChange={(v) => setForm({ ...form, questionId: parseInt(v) })}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>{questions.map((q) => <SelectItem key={q.questionId} value={String(q.questionId)}>{q.questionText?.substring(0, 50)}</SelectItem>)}</SelectContent>
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

function CreateAdmin() {
  const [form, setForm] = useState({ email: "", username: "", password: "", role: "ADMIN" });
  const [error, setError] = useState<unknown>(null);
  const m = useMutation({
    mutationFn: () => api("/admin/register", { method: "POST", body: form }),
    onSuccess: () => { toast.success("Admin created"); setForm({ email: "", username: "", password: "", role: "ADMIN" }); },
    onError: (e) => setError(e),
  });
  return (
    <div className="max-w-md space-y-4">
      <h2 className="text-lg font-semibold">Create admin user</h2>
      <p className="text-sm text-muted-foreground">The backend doesn't expose a full users list; this creates a new admin account.</p>
      <ApiAlert error={error} />
      <Card><CardContent className="space-y-3 pt-6">
        <div className="space-y-1"><Label>Email</Label><Input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
        <div className="space-y-1"><Label>Username</Label><Input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} /></div>
        <div className="space-y-1"><Label>Password</Label><Input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div>
        <Button onClick={() => m.mutate()} disabled={m.isPending}>{m.isPending ? "Creating..." : "Create admin"}</Button>
      </CardContent></Card>
    </div>
  );
}
function AdminUsers() {
  const qc = useQueryClient();

  const [search, setSearch] = useState("");
  const [sortBy, setSortBy] = useState("id");

  const [open, setOpen] = useState(false);

  const emptyForm = {
    fullName: "",
    phoneNumber: "",
    email: "",
    username: "",
    password: "",
    role: "STUDENT",
  };

  const [form, setForm] = useState<any>(emptyForm);

  const [editing, setEditing] =
      useState<any>(null);

  const USER_ROLES = [
    "STUDENT",
    "INSTRUCTOR",
    "ADMIN",
    "USER",
  ];

  const list = useQuery<any[]>({
    queryKey: ["admin-users"],
    queryFn: () => api("/api/users"),
  });

  const create = useMutation({
    mutationFn: () =>
        api("/admin/register", {
          method: "POST",
          body: form,
        }),

    onSuccess: () => {
      toast.success("User created");

      setOpen(false);

      setForm(emptyForm);

      qc.invalidateQueries({
        queryKey: ["admin-users"],
      });
    },

    onError: (e: any) => {
      toast.error(e.message);
    },
  });

  const update = useMutation({
    mutationFn: () =>
        api(`/api/users/${editing.id}`, {
          method: "PUT",
          body: form,
        }),

    onSuccess: () => {
      toast.success("User updated");

      setOpen(false);

      setEditing(null);

      setForm(emptyForm);

      qc.invalidateQueries({
        queryKey: ["admin-users"],
      });
    },

    onError: (e: any) => {
      toast.error(e.message);
    },
  });

  const updateRole = useMutation({
    mutationFn: ({
                   id,
                   role,
                 }: {
      id: number;
      role: string;
    }) =>
        api(`/api/users/${id}/role`, {
          method: "PATCH",
          body: { role },
        }),

    onSuccess: () => {
      toast.success("Role updated");

      qc.invalidateQueries({
        queryKey: ["admin-users"],
      });
    },

    onError: (e: any) => {
      toast.error(e.message);
    },
  });

  const del = useMutation({
    mutationFn: (id: number) =>
        api(`/api/users/${id}`, {
          method: "DELETE",
        }),

    onSuccess: () => {
      toast.success("User deleted");

      qc.invalidateQueries({
        queryKey: ["admin-users"],
      });
    },

    onError: (e: any) => {
      toast.error(e.message);
    },
  });

  const filteredUsers = [...(list.data || [])]
      .filter((u) => {
        const text =
            `${u.username} ${u.email}`
                .toLowerCase();

        return text.includes(
            search.toLowerCase()
        );
      })

      .sort((a, b) => {
        if (sortBy === "username") {
          return a.username.localeCompare(
              b.username
          );
        }

        if (sortBy === "email") {
          return a.email.localeCompare(
              b.email
          );
        }

        return a.id - b.id;
      });

  return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">
            User Management
          </h2>

          <Button
              size="sm"
              onClick={() => {
                setEditing(null);
                setForm(emptyForm);
                setOpen(true);
              }}
          >
            <Plus className="mr-1 h-4 w-4" />
            New User
          </Button>
        </div>

        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <Input
              placeholder="Search users..."
              value={search}
              onChange={(e) =>
                  setSearch(e.target.value)
              }
              className="max-w-sm"
          />

          <Select
              value={sortBy}
              onValueChange={setSortBy}
          >
            <SelectTrigger className="w-48">
              <SelectValue />
            </SelectTrigger>

            <SelectContent>
              <SelectItem value="id">
                ID
              </SelectItem>

              <SelectItem value="username">
                Username
              </SelectItem>

              <SelectItem value="email">
                Email
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <ApiAlert error={list.error} />

        {list.isLoading ? (
            <Skeleton className="h-40" />
        ) : (
            <div className="rounded-md border border-border bg-card overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Username</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Role</TableHead>
                    <TableHead className="text-right">
                      Actions
                    </TableHead>
                  </TableRow>
                </TableHeader>

                <TableBody>
                  {filteredUsers.map((u: any) => (

                      <TableRow key={u.id}>
                        <TableCell>
                          <span className="font-mono text-xs">{u.id}</span>
                          {u.userId && <span className="block text-[10px] text-muted-foreground font-mono truncate max-w-[120px]">{u.userId}</span>}
                        </TableCell>

                        <TableCell className="font-medium">
                          {u.username}
                        </TableCell>

                        <TableCell>
                          {u.email}
                        </TableCell>

                        <TableCell>
                          <Badge variant={u.isVerified ? "default" : "outline"} className="text-xs">
                            {u.isVerified ? "Verified" : "Unverified"}
                          </Badge>
                        </TableCell>

                        <TableCell>
                          <Select
                              value={u.role}
                              onValueChange={(v) =>
                                  updateRole.mutate({
                                    id: u.id,
                                    role: v,
                                  })
                              }
                          >
                            <SelectTrigger className="w-40">
                              <SelectValue />
                            </SelectTrigger>

                            <SelectContent>
                              {USER_ROLES.map(
                                  (role) => (
                                      <SelectItem
                                          key={role}
                                          value={role}
                                      >
                                        {role}
                                      </SelectItem>
                                  )
                              )}
                            </SelectContent>
                          </Select>
                        </TableCell>

                        <TableCell className="space-x-1 text-right">
                          <Button
                              size="icon"
                              variant="ghost"
                              onClick={() => {
                                if (
                                    confirm(
                                        "Delete user?"
                                    )
                                ) {
                                  del.mutate(
                                      u.id
                                  );
                                }
                              }}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
        )}

        <Dialog
            open={open}
            onOpenChange={setOpen}
        >
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {editing
                    ? "Edit User"
                    : "Create User"}
              </DialogTitle>
            </DialogHeader>

            <div className="space-y-3">
              <div className="space-y-1">
                <Label>Full Name</Label>
                <Input
                    value={form.fullName || ""}
                    onChange={(e) =>
                        setForm({
                          ...form,
                          fullName: e.target.value,
                        })
                    }
                />
              </div>

              <div className="space-y-1">
                <Label>Phone Number</Label>
                <Input
                    value={form.phoneNumber || ""}
                    onChange={(e) =>
                        setForm({
                          ...form,
                          phoneNumber: e.target.value,
                        })
                    }
                />
              </div>

              <div className="space-y-1">
                <Label>Email</Label>

                <Input
                    value={form.email}
                    onChange={(e) =>
                        setForm({
                          ...form,
                          email:
                          e.target.value,
                        })
                    }
                />
              </div>

              <div className="space-y-1">
                <Label>Username</Label>

                <Input
                    value={form.username}
                    onChange={(e) =>
                        setForm({
                          ...form,
                          username:
                          e.target.value,
                        })
                    }
                />
              </div>

              <div className="space-y-1">
                <Label>
                  Password
                </Label>

                <Input
                    type="password"
                    value={form.password}
                    onChange={(e) =>
                        setForm({
                          ...form,
                          password:
                          e.target.value,
                        })
                    }
                />
              </div>

              <div className="space-y-1">
                <Label>Role</Label>

                <Select
                    value={form.role}
                    onValueChange={(v) =>
                        setForm({
                          ...form,
                          role: v,
                        })
                    }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>

                  <SelectContent>
                    {USER_ROLES.map(
                        (role) => (
                            <SelectItem
                                key={role}
                                value={role}
                            >
                              {role}
                            </SelectItem>
                        )
                    )}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <DialogFooter>
              <Button
                  onClick={() => {
                    if (editing) {
                      update.mutate();
                    } else {
                      create.mutate();
                    }
                  }}
                  disabled={
                      create.isPending ||
                      update.isPending
                  }
              >
                {editing
                    ? "Update"
                    : "Create"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
  );
}

function AdminAssignments() {
  const qc = useQueryClient();
  const [selectedCourseId, setSelectedCourseId] = useState<string>("");
  const [submissionsAssignment, setSubmissionsAssignment] = useState<any>(null);
  const [gradingSubmission, setGradingSubmission] = useState<any>(null);
  const [score, setScore] = useState<string>("");
  const [grade, setGrade] = useState<string>("");
  const [feedback, setFeedback] = useState<string>("");
  const [gradingPending, setGradingPending] = useState(false);

  const coursesQuery = useQuery<any[]>({
    queryKey: ["admin-courses"],
    queryFn: () => api("/api/courses", { auth: false }),
  });

  const activeCourseId = selectedCourseId || (coursesQuery.data && coursesQuery.data.length > 0 ? String(coursesQuery.data[0].courseId) : "");

  const assignmentsQuery = useQuery<any[]>({
    queryKey: ["admin-course-assignments", activeCourseId],
    queryFn: () => api(`/api/courses/${activeCourseId}/assignments`),
    enabled: Boolean(activeCourseId),
  });

  const submissionsQuery = useQuery<any[]>({
    queryKey: ["admin-assignment-submissions", submissionsAssignment?.assignmentId],
    queryFn: () => api(`/api/assignments/${submissionsAssignment.assignmentId}/submissions`),
    enabled: Boolean(submissionsAssignment),
  });

  const deleteAssignment = useMutation({
    mutationFn: (id: number) => api(`/api/assignments/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      toast.success("Assignment deleted successfully");
      qc.invalidateQueries({ queryKey: ["admin-course-assignments", activeCourseId] });
    },
    onError: (e: any) => toast.error(e.message || "Failed to delete assignment"),
  });

  const handleGradeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!gradingSubmission) return;
    setGradingPending(true);
    try {
      await api(`/api/assignments/submissions/${gradingSubmission.submissionId}/grade`, {
        method: "PUT",
        body: {
          score: score !== "" ? Number(score) : null,
          grade: grade.trim() || null,
          feedback: feedback.trim() || null,
        },
      });
      toast.success("Submission graded successfully");
      setGradingSubmission(null);
      qc.invalidateQueries({ queryKey: ["admin-assignment-submissions", submissionsAssignment?.assignmentId] });
    } catch (err: any) {
      toast.error(err.message || "Failed to grade submission");
    } finally {
      setGradingPending(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <h2 className="text-lg font-semibold">Course Assignments Management</h2>
        <div className="flex items-center gap-2">
          <Label className="text-xs whitespace-nowrap">Filter Course:</Label>
          <Select value={activeCourseId} onValueChange={(val) => setSelectedCourseId(val)}>
            <SelectTrigger className="w-[240px]">
              <SelectValue placeholder="Select course..." />
            </SelectTrigger>
            <SelectContent>
              {coursesQuery.data?.map((c) => (
                <SelectItem key={c.courseId} value={String(c.courseId)}>
                  {c.title}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {assignmentsQuery.isLoading ? (
        <Skeleton className="h-40 w-full" />
      ) : !assignmentsQuery.data || assignmentsQuery.data.length === 0 ? (
        <div className="rounded-md border p-8 text-center text-muted-foreground bg-card">
          No assignments found for this course.
        </div>
      ) : (
        <div className="rounded-md border bg-card overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>Title</TableHead>
                <TableHead>Section</TableHead>
                <TableHead>Due Date</TableHead>
                <TableHead>Max Score</TableHead>
                <TableHead>Resubmission</TableHead>
                <TableHead>Attachment</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {assignmentsQuery.data.map((a: any) => (
                <TableRow key={a.assignmentId}>
                  <TableCell>{a.assignmentId}</TableCell>
                  <TableCell className="font-semibold">{a.title}</TableCell>
                  <TableCell className="text-muted-foreground">{a.sectionTitle || "—"}</TableCell>
                  <TableCell className="text-xs">
                    {a.dueDate ? new Date(a.dueDate).toLocaleDateString() : "—"}
                  </TableCell>
                  <TableCell>{a.maxScore} pts</TableCell>
                  <TableCell>
                    <Badge variant={a.allowResubmission ? "secondary" : "outline"} className="text-[10px]">
                      {a.allowResubmission ? "Allowed" : "One-time"}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {a.supportingFileUrl ? (
                      <a
                        href={mediaUrl(a.supportingFileUrl)}
                        target="_blank"
                        rel="noreferrer"
                        download
                        className="text-xs text-primary underline"
                      >
                        Download
                      </a>
                    ) : (
                      "—"
                    )}
                  </TableCell>
                  <TableCell className="text-right space-x-2">
                    <Button
                      size="sm"
                      variant="outline"
                      className="text-xs gap-1"
                      onClick={() => setSubmissionsAssignment(a)}
                    >
                      <Users className="h-3.5 w-3.5" /> Submissions
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      className="text-xs text-destructive hover:bg-destructive/10"
                      onClick={() => {
                        if (window.confirm("Delete this assignment?")) {
                          deleteAssignment.mutate(a.assignmentId);
                        }
                      }}
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Submissions Dialog */}
      {submissionsAssignment && (
        <Dialog open onOpenChange={() => setSubmissionsAssignment(null)}>
          <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Submissions for: {submissionsAssignment.title}</DialogTitle>
              <p className="text-xs text-muted-foreground">
                Course: {submissionsAssignment.courseTitle} • Max Score: {submissionsAssignment.maxScore} pts
              </p>
            </DialogHeader>

            <div className="py-2">
              {submissionsQuery.isLoading ? (
                <Skeleton className="h-40 w-full" />
              ) : !submissionsQuery.data || submissionsQuery.data.length === 0 ? (
                <p className="py-8 text-center text-sm text-muted-foreground border rounded-lg">
                  No students have submitted this assignment yet.
                </p>
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
                      {submissionsQuery.data.map((s: any) => (
                        <TableRow key={s.submissionId}>
                          <TableCell>
                            <p className="font-semibold text-sm">{s.studentName || "Student"}</p>
                            <p className="text-xs text-muted-foreground">{s.studentEmail}</p>
                          </TableCell>
                          <TableCell className="text-xs text-muted-foreground">
                            {s.submittedAt ? new Date(s.submittedAt).toLocaleString() : "—"}
                          </TableCell>
                          <TableCell className="max-w-xs">
                            {s.textSubmission && (
                              <p className="text-xs line-clamp-2 font-mono bg-muted/40 p-1 rounded">
                                {s.textSubmission}
                              </p>
                            )}
                            {s.fileUrl && (
                              <a
                                href={mediaUrl(s.fileUrl)}
                                target="_blank"
                                rel="noreferrer"
                                download
                                className="text-xs text-primary underline block mt-1"
                              >
                                Download Attached File
                              </a>
                            )}
                          </TableCell>
                          <TableCell>
                            <Badge
                              variant={
                                s.status === "GRADED"
                                  ? "default"
                                  : s.status === "LATE"
                                  ? "destructive"
                                  : "secondary"
                              }
                            >
                              {s.status}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            {s.score != null ? (
                              <div>
                                <span className="font-bold text-sm">
                                  {s.score} / {submissionsAssignment.maxScore}
                                </span>
                                {s.grade && (
                                  <Badge className="ml-1.5 text-[10px] bg-emerald-600">{s.grade}</Badge>
                                )}
                                {s.feedback && (
                                  <p className="text-[11px] text-muted-foreground line-clamp-1 mt-0.5">
                                    "{s.feedback}"
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
                              variant="outline"
                              className="text-xs"
                              onClick={() => {
                                setGradingSubmission(s);
                                setScore(s.score != null ? String(s.score) : "");
                                setGrade(s.grade || "");
                                setFeedback(s.feedback || "");
                              }}
                            >
                              {s.status === "GRADED" ? "Update Grade" : "Grade"}
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
              <Button variant="outline" size="sm" onClick={() => setSubmissionsAssignment(null)}>
                Close
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}

      {/* Grade Dialog */}
      {gradingSubmission && (
        <Dialog open onOpenChange={() => setGradingSubmission(null)}>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Grade Student Submission</DialogTitle>
              <p className="text-xs text-muted-foreground">
                Student: {gradingSubmission.studentName} ({gradingSubmission.studentEmail})
              </p>
            </DialogHeader>
            <form onSubmit={handleGradeSubmit} className="space-y-4 py-2">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Score (Max: {submissionsAssignment?.maxScore})</Label>
                  <Input
                    type="number"
                    step="any"
                    min={0}
                    max={submissionsAssignment?.maxScore}
                    required
                    value={score}
                    onChange={(e) => setScore(e.target.value)}
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label>Grade (Optional)</Label>
                  <Input
                    placeholder="A, B, C, D, F"
                    value={grade}
                    onChange={(e) => setGrade(e.target.value.toUpperCase())}
                    className="mt-1"
                  />
                </div>
              </div>
              <div>
                <Label>Feedback</Label>
                <Textarea
                  placeholder="Feedback for the student..."
                  rows={3}
                  value={feedback}
                  onChange={(e) => setFeedback(e.target.value)}
                  className="mt-1"
                />
              </div>
              <DialogFooter className="gap-2 pt-2">
                <Button type="button" variant="outline" onClick={() => setGradingSubmission(null)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={gradingPending}>
                  {gradingPending ? "Saving..." : "Save Grade"}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}

