import { Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { useAuth, roleHome } from "@/lib/auth";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getApiBase, setApiBase } from "@/lib/api";
import { GraduationCap, Settings } from "lucide-react";

export function SiteHeader() {
  const { isAuthenticated, role, email, logout } = useAuth();
  const nav = useNavigate();
  const [apiBase, setBase] = useState(() => (typeof window === "undefined" ? "" : getApiBase()));
  const [open, setOpen] = useState(false);

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border bg-background/80 backdrop-blur">
      <div className="container mx-auto flex h-14 items-center justify-between px-4">
        <Link to="/" className="flex items-center gap-2 font-semibold text-foreground">
          <GraduationCap className="h-5 w-5 text-primary" />
          <span>Lumen LMS</span>
        </Link>
        <nav className="hidden gap-6 text-sm md:flex">
          <Link to="/" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground font-medium" }}>Courses</Link>
          <Link to="/instructors" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground font-medium" }}>Instructors</Link>
          {isAuthenticated && (
            <>
              <Link to="/my/enrollments" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground font-medium" }}>My learning</Link>
              <Link to="/my/payments" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground font-medium" }}>Payments</Link>
            </>
          )}
          {role === "INSTRUCTOR" && (
            <Link to="/instructor" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground font-medium" }}>Instructor Studio</Link>
          )}
          {role === "ADMIN" && (
            <Link to="/admin" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground font-medium" }}>Admin</Link>
          )}
        </nav>

        <div className="flex items-center gap-2">
          {isAuthenticated ? (
            <>
              <span className="hidden text-xs text-muted-foreground lg:inline">{email}</span>
              <Button variant="ghost" size="sm" onClick={() => nav({ to: "/profile" })}>
                Profile
              </Button>
              {role && role !== "USER" && role !== "STUDENT" && (
                <Button variant="outline" size="sm" className="hidden sm:inline-flex" onClick={() => nav({ to: roleHome(role) })}>
                  Dashboard
                </Button>
              )}
              <Button variant="outline" size="sm" onClick={() => { logout(); nav({ to: "/login" }); }}>
                Sign out
              </Button>
            </>
          ) : (
            <>
              <Button variant="ghost" size="sm" onClick={() => nav({ to: "/login" })}>
                Sign in
              </Button>
              <Button size="sm" onClick={() => nav({ to: "/register" })}>
                Get started
              </Button>
            </>
          )}

          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button variant="ghost" size="icon" aria-label="API settings">
                <Settings className="h-4 w-4" />
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Backend API Connection</DialogTitle>
              </DialogHeader>
              <div className="space-y-3 py-2">
                <Label htmlFor="apiBaseUrl">Backend Server URL</Label>
                <Input
                  id="apiBaseUrl"
                  value={apiBase}
                  onChange={(e) => setBase(e.target.value)}
                  placeholder="http://localhost:8080"
                />
                <p className="text-xs text-muted-foreground">
                  Default: <code>http://localhost:8080</code>
                </p>
              </div>
              <DialogFooter>
                <Button
                  onClick={() => {
                    setApiBase(apiBase);
                    setOpen(false);
                  }}
                >
                  Save URL
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>
      </div>
    </header>
  );
}
