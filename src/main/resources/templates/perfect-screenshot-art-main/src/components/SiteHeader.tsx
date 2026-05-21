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
          <Link to="/" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground" }}>Courses</Link>
          {isAuthenticated && (
            <>
              <Link to="/my/enrollments" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground" }}>My learning</Link>
              <Link to="/my/payments" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground" }}>Payments</Link>
            </>
          )}
          {role === "INSTRUCTOR" && (
            <Link to="/instructor" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground" }}>Instructor</Link>
          )}
          {role === "ADMIN" && (
            <Link to="/admin" className="text-muted-foreground hover:text-foreground" activeProps={{ className: "text-foreground" }}>Admin</Link>
          )}
        </nav>
        <div className="flex items-center gap-2">
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button variant="ghost" size="icon" aria-label="API settings"><Settings className="h-4 w-4" /></Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>Backend API URL</DialogTitle></DialogHeader>
              <div className="space-y-2">
                <Label htmlFor="api">Base URL</Label>
                <Input id="api" value={apiBase} onChange={(e) => setBase(e.target.value)} placeholder="http://localhost:8080" />
                <p className="text-xs text-muted-foreground">Stored in this browser only.</p>
              </div>
              <DialogFooter>
                <Button onClick={() => { setApiBase(apiBase); setOpen(false); location.reload(); }}>Save & reload</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
          {isAuthenticated ? (
            <>
              <span className="hidden text-xs text-muted-foreground sm:inline">{email}</span>
              <Button variant="outline" size="sm" onClick={() => { logout(); nav({ to: "/login" }); }}>Sign out</Button>
            </>
          ) : (
            <>
              <Button variant="ghost" size="sm" onClick={() => nav({ to: "/login" })}>Sign in</Button>
              <Button size="sm" onClick={() => nav({ to: "/register" })}>Get started</Button>
            </>
          )}
          {isAuthenticated && role && (
            <Button variant="ghost" size="sm" className="hidden md:inline-flex" onClick={() => nav({ to: roleHome(role) })}>Dashboard</Button>
          )}
        </div>
      </div>
    </header>
  );
}
