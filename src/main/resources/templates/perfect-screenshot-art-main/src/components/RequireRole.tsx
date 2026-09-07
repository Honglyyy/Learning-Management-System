import { Navigate } from "@tanstack/react-router";
import { useAuth, type Role } from "@/lib/auth";

export function RequireRole({ roles, children }: { roles: Role[]; children: React.ReactNode }) {
  const { isAuthenticated, role, isLoading } = useAuth();
  if (isLoading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    );
  }
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  const hasRole = role && (
    roles.includes(role) ||
    (role === "STUDENT" && roles.includes("USER")) ||
    (role === "USER" && roles.includes("STUDENT"))
  );
  if (!hasRole) return <Navigate to="/" replace />;
  return <>{children}</>;
}

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    );
  }
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}
