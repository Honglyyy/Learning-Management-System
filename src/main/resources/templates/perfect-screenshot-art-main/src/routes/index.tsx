import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { SiteHeader } from "@/components/SiteHeader";
import { api, mediaUrl } from "@/lib/api";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { ApiAlert } from "@/components/ApiAlert";
import { Search, Sparkles, Star, Clock, User } from "lucide-react";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Browse courses — Lumen LMS" },
      { name: "description", content: "Discover and enroll in expert-led courses." },
    ],
  }),
  component: Index,
});

type Course = {
  courseId: number;
  title: string;
  description?: string;
  price?: number;
  overallDuration?: string;
  coverDir?: string;
  instructorName?: string;
  instructor?: any;
  categories?: Array<{ id: number; category: string }> | string[];
  rating?: number;
};

function Index() {
  const nav = useNavigate();
  const [q, setQ] = useState("");
  const { data, isLoading, error } = useQuery<Course[]>({
    queryKey: ["courses"],
    queryFn: () => api<Course[]>("/api/courses", { auth: false }),
  });

  const filtered = useMemo(() => {
    if (!data) return [];
    const needle = q.trim().toLowerCase();
    if (!needle) return data;
    return data.filter((c) => {
      const inst = typeof c.instructor === "object" ? c.instructor?.username || c.instructor?.email : c.instructorName;
      const cats = (c.categories || []).map((x: any) => (typeof x === "string" ? x : x?.category)).join(" ");
      return [c.title, c.description, inst, cats].filter(Boolean).join(" ").toLowerCase().includes(needle);
    });
  }, [data, q]);

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <section className="border-b border-border bg-gradient-to-b from-accent/40 to-background">
        <div className="container mx-auto px-4 py-16 md:py-24">
          <Badge variant="secondary" className="mb-4 gap-1"><Sparkles className="h-3 w-3" /> New courses every week</Badge>
          <h1 className="text-4xl font-semibold tracking-tight text-foreground md:text-5xl">
            Learn from the best.<br />
            <span className="text-primary">Build what's next.</span>
          </h1>
          <p className="mt-4 max-w-xl text-muted-foreground">
            Hands-on courses in programming, design, and business — taught by practitioners.
          </p>
          <div className="mt-6 flex max-w-md items-center gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search by title, instructor, or category"
                className="pl-9"
                value={q}
                onChange={(e) => setQ(e.target.value)}
              />
            </div>
          </div>
        </div>
      </section>

      <section className="container mx-auto px-4 py-12">
        <h2 className="mb-6 text-2xl font-semibold text-foreground">All courses</h2>
        <ApiAlert error={error} />
        {isLoading ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-72 rounded-lg" />)}
          </div>
        ) : filtered.length === 0 ? (
          <p className="text-muted-foreground">No courses found.</p>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">

            {/*{filtered.map((c) => <CourseCard key={c.id} c={c} onOpen={() => nav({ to: "/courses/$id", params: { id: String(c.id) } })} />)}*/}
            {filtered.map((c) => <CourseCard key={c.courseId} c={c} onOpen={() => nav({ to: "/courses/$id", params: { id: String(c.courseId) } })} />)}
          </div>
        )}
      </section>
    </div>
  );
}

function CourseCard({ c, onOpen }: { c: Course; onOpen: () => void }) {
  const cover = mediaUrl(c.coverDir);
  const inst = typeof c.instructor === "object" ? c.instructor?.username || c.instructor?.email : c.instructorName;
  const cats = (c.categories || []).map((x: any) => (typeof x === "string" ? x : x?.category)).filter(Boolean);
  const id = c.courseId;
  return (
    <Card className="flex flex-col overflow-hidden transition hover:shadow-lg">
      <div className="aspect-video w-full overflow-hidden bg-muted">
        {cover ? (
          <img src={cover} alt={c.title} className="h-full w-full object-cover" loading="lazy" />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-muted-foreground">No cover</div>
        )}
      </div>
      <CardHeader>
        <CardTitle className="line-clamp-2 text-base">{c.title}</CardTitle>
        <p className="line-clamp-2 text-sm text-muted-foreground">{c.description}</p>
      </CardHeader>
      <CardContent className="flex-1 space-y-2">
        <div className="flex flex-wrap gap-1">
          {cats.slice(0, 3).map((cat: string) => <Badge key={cat} variant="secondary">{cat}</Badge>)}
        </div>
        <div className="flex items-center gap-3 text-xs text-muted-foreground">
          {inst && <span className="flex items-center gap-1"><User className="h-3 w-3" />{inst}</span>}
          {c.overallDuration && <span className="flex items-center gap-1"><Clock className="h-3 w-3" />{c.overallDuration}</span>}
          {typeof c.rating === "number" && <span className="flex items-center gap-1"><Star className="h-3 w-3 fill-current" />{c.rating.toFixed(1)}</span>}
        </div>
      </CardContent>
      <CardFooter className="flex items-center justify-between">
        <span className="text-lg font-semibold text-foreground">${Number(c.price ?? 0).toFixed(2)}</span>
        <Button size="sm" onClick={onOpen}>View</Button>
      </CardFooter>
    </Card>
  );
}
