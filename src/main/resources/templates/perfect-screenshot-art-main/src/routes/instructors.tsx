import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { api, mediaUrl } from "@/lib/api";
import { SiteHeader } from "@/components/SiteHeader";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { ApiAlert } from "@/components/ApiAlert";
import { User, BookOpen, Star, Search, Mail } from "lucide-react";

export const Route = createFileRoute("/instructors")({
  head: () => ({ meta: [{ title: "Our Instructors — Lumen LMS" }] }),
  component: InstructorsDirectoryPage,
});

type Instructor = {
  instructorId: number;
  userId: number;
  username: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  profilePhotoUrl?: string;
  biography?: string;
  expertise?: string;
  averageRating?: number;
  totalCourses?: number;
};

function InstructorsDirectoryPage() {
  const [search, setSearch] = useState("");

  const { data: instructors, isLoading, error } = useQuery<Instructor[]>({
    queryKey: ["public-instructors"],
    queryFn: () => api<Instructor[]>("/api/instructors"),
  });

  const filtered = (instructors || []).filter((inst) => {
    const q = search.toLowerCase();
    return (
      (inst.fullName && inst.fullName.toLowerCase().includes(q)) ||
      (inst.username && inst.username.toLowerCase().includes(q)) ||
      (inst.expertise && inst.expertise.toLowerCase().includes(q)) ||
      (inst.biography && inst.biography.toLowerCase().includes(q))
    );
  });

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <main className="container mx-auto px-4 py-8 space-y-8">
        <div className="text-center max-w-2xl mx-auto space-y-2">
          <h1 className="text-3xl font-bold tracking-tight">Meet Our Expert Instructors</h1>
          <p className="text-muted-foreground">
            Learn directly from passionate industry practitioners, educators, and subject matter experts.
          </p>
          <div className="pt-4 max-w-md mx-auto relative">
            <Search className="absolute left-3 top-7 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search by name, expertise, or topic..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-9 mt-4"
            />
          </div>
        </div>

        <ApiAlert error={error} />

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((n) => (
              <Card key={n} className="overflow-hidden">
                <CardHeader className="flex flex-row items-center gap-4">
                  <Skeleton className="h-16 w-16 rounded-full" />
                  <div className="space-y-2 flex-1">
                    <Skeleton className="h-4 w-3/4" />
                    <Skeleton className="h-3 w-1/2" />
                  </div>
                </CardHeader>
                <CardContent className="space-y-2">
                  <Skeleton className="h-3 w-full" />
                  <Skeleton className="h-3 w-4/5" />
                </CardContent>
              </Card>
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-16 text-muted-foreground">
            No instructors found matching &quot;{search}&quot;.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filtered.map((inst) => (
              <Card key={inst.instructorId} className="flex flex-col justify-between hover:shadow-md transition-shadow">
                <CardHeader className="flex flex-row items-center gap-4 pb-2">
                  <div className="h-16 w-16 rounded-full overflow-hidden bg-muted border border-border flex items-center justify-center shrink-0">
                    {inst.profilePhotoUrl ? (
                      <img
                        src={mediaUrl(inst.profilePhotoUrl)}
                        alt={inst.fullName || inst.username}
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <User className="h-8 w-8 text-muted-foreground" />
                    )}
                  </div>
                  <div className="min-w-0 flex-1">
                    <CardTitle className="text-lg truncate">
                      {inst.fullName || inst.username}
                    </CardTitle>
                    {inst.expertise && (
                      <Badge variant="secondary" className="mt-1 font-normal text-xs">
                        {inst.expertise}
                      </Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4 flex-1 flex flex-col justify-between">
                  <p className="text-sm text-muted-foreground line-clamp-3">
                    {inst.biography || "No biography provided yet."}
                  </p>

                  <div className="pt-2 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
                    <div className="flex items-center gap-1">
                      <BookOpen className="h-3.5 w-3.5 text-primary" />
                      <span>{inst.totalCourses || 0} Courses</span>
                    </div>
                    {inst.averageRating != null && inst.averageRating > 0 && (
                      <div className="flex items-center gap-1 text-amber-500 font-medium">
                        <Star className="h-3.5 w-3.5 fill-amber-500" />
                        <span>{inst.averageRating.toFixed(1)}</span>
                      </div>
                    )}
                    <a
                      href={`mailto:${inst.email}`}
                      className="hover:text-foreground flex items-center gap-1"
                      title={inst.email}
                    >
                      <Mail className="h-3.5 w-3.5" />
                      <span>Contact</span>
                    </a>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
