import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Bell, Check, CheckCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { api } from "@/lib/api";
import { useState } from "react";
import { toast } from "sonner";

export type NotificationItem = {
  notificationId: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
};

export function NotificationPopover() {
  const [open, setOpen] = useState(false);
  const qc = useQueryClient();

  const unreadQuery = useQuery<{ unreadCount: number }>({
    queryKey: ["notifications-unread-count"],
    queryFn: () => api("/api/notifications/unread-count", { auth: true }).catch(() => ({ unreadCount: 0 })),
    refetchInterval: 15000,
  });

  const notificationsQuery = useQuery<NotificationItem[]>({
    queryKey: ["notifications"],
    queryFn: () => api("/api/notifications", { auth: true }).catch(() => []),
    enabled: open,
  });

  const markReadMutation = useMutation({
    mutationFn: (id: number) => api(`/api/notifications/${id}/read`, { method: "PATCH", auth: true }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["notifications-unread-count"] });
      qc.invalidateQueries({ queryKey: ["notifications"] });
    },
  });

  const markAllReadMutation = useMutation({
    mutationFn: () => api("/api/notifications/read-all", { method: "PATCH", auth: true }),
    onSuccess: () => {
      toast.success("All notifications marked as read");
      qc.invalidateQueries({ queryKey: ["notifications-unread-count"] });
      qc.invalidateQueries({ queryKey: ["notifications"] });
    },
  });

  const unreadCount = unreadQuery.data?.unreadCount ?? 0;
  const items = notificationsQuery.data || [];

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="icon" className="relative" aria-label="Notifications">
          <Bell className="h-4 w-4" />
          {unreadCount > 0 && (
            <span className="absolute -top-0.5 -right-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-rose-600 px-1 text-[10px] font-bold text-white shadow-sm animate-in zoom-in-50">
              {unreadCount > 99 ? "99+" : unreadCount}
            </span>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-80 p-0 sm:w-96" align="end">
        <div className="flex items-center justify-between border-b px-4 py-3">
          <div className="flex items-center gap-2">
            <span className="font-semibold text-sm">Notifications</span>
            {unreadCount > 0 && (
              <Badge variant="secondary" className="text-xs px-1.5 py-0">
                {unreadCount} new
              </Badge>
            )}
          </div>
          {unreadCount > 0 && (
            <Button
              variant="ghost"
              size="sm"
              className="h-7 text-xs gap-1 text-muted-foreground hover:text-foreground"
              disabled={markAllReadMutation.isPending}
              onClick={() => markAllReadMutation.mutate()}
            >
              <CheckCheck className="h-3.5 w-3.5" />
              Mark all read
            </Button>
          )}
        </div>
        <ScrollArea className="max-h-80">
          {notificationsQuery.isLoading ? (
            <div className="p-4 text-center text-xs text-muted-foreground">Loading notifications...</div>
          ) : items.length === 0 ? (
            <div className="p-6 text-center text-xs text-muted-foreground">No notifications yet</div>
          ) : (
            <div className="divide-y divide-border">
              {items.map((n) => (
                <div
                  key={n.notificationId}
                  className={`p-3 text-left transition-colors flex items-start justify-between gap-2 hover:bg-muted/50 ${
                    !n.isRead ? "bg-primary/5" : ""
                  }`}
                >
                  <div className="flex-1 space-y-1">
                    <div className="flex items-center gap-1.5">
                      {!n.isRead && <span className="h-2 w-2 rounded-full bg-primary shrink-0" />}
                      <p className={`text-xs ${!n.isRead ? "font-semibold text-foreground" : "font-medium text-foreground/80"}`}>
                        {n.title}
                      </p>
                    </div>
                    <p className="text-xs text-muted-foreground leading-relaxed pl-3.5">{n.message}</p>
                    <p className="text-[10px] text-muted-foreground/70 pl-3.5 pt-0.5">
                      {n.createdAt ? new Date(n.createdAt).toLocaleString() : ""}
                    </p>
                  </div>
                  {!n.isRead && (
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-6 w-6 shrink-0 text-muted-foreground hover:text-foreground"
                      title="Mark as read"
                      onClick={() => markReadMutation.mutate(n.notificationId)}
                    >
                      <Check className="h-3 w-3" />
                    </Button>
                  )}
                </div>
              ))}
            </div>
          )}
        </ScrollArea>
      </PopoverContent>
    </Popover>
  );
}
