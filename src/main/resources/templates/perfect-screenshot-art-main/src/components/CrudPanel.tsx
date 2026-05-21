import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { ApiAlert } from "@/components/ApiAlert";
import { toast } from "sonner";
import { Plus, Pencil, Trash2 } from "lucide-react";

export type FieldDef = {
  name: string;
  label: string;
  type?: "text" | "number" | "textarea" | "checkbox";
  placeholder?: string;
};

export type CrudConfig = {
  resource: string;       // e.g. "categories" -> /api/categories
  title: string;
  queryKey: string;
  fields: FieldDef[];     // editable form fields
  columns: { key: string; label: string; render?: (row: any) => React.ReactNode }[];
  emptyForm: Record<string, any>;
  toBody?: (form: Record<string, any>) => any;
};

export function CrudPanel({ cfg }: { cfg: CrudConfig }) {
  const qc = useQueryClient();
  const list = useQuery<any[]>({ queryKey: [cfg.queryKey], queryFn: () => api(`/api/${cfg.resource}`) });

  const del = useMutation({
    mutationFn: (id: number) => api(`/api/${cfg.resource}/${id}`, { method: "DELETE" }),
    onSuccess: () => { toast.success("Deleted"); qc.invalidateQueries({ queryKey: [cfg.queryKey] }); },
    onError: (e: any) => toast.error(e.message || "Failed"),
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-foreground">{cfg.title}</h2>
        <EditDialog cfg={cfg} mode="create" />
      </div>
      <ApiAlert error={list.error} />
      {list.isLoading ? <Skeleton className="h-40 w-full" /> : !list.data || list.data.length === 0 ? (
        <p className="text-muted-foreground">No records yet.</p>
      ) : (
        <div className="rounded-md border border-border bg-card">
          <Table>
            <TableHeader><TableRow>{cfg.columns.map((c) => <TableHead key={c.key}>{c.label}</TableHead>)}<TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
            <TableBody>
              {list.data.map((row: any) => (
                <TableRow key={row.id}>
                  {cfg.columns.map((c) => <TableCell key={c.key}>{c.render ? c.render(row) : String(row[c.key] ?? "")}</TableCell>)}
                  <TableCell className="space-x-1 text-right">
                    <EditDialog cfg={cfg} mode="edit" initial={row} />
                    <Button size="icon" variant="ghost" onClick={() => { if (confirm("Delete this record?")) del.mutate(row.id); }}><Trash2 className="h-4 w-4" /></Button>
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

function EditDialog({ cfg, mode, initial }: { cfg: CrudConfig; mode: "create" | "edit"; initial?: any }) {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<Record<string, any>>(initial || { ...cfg.emptyForm });
  const [error, setError] = useState<unknown>(null);

  const save = useMutation({
    mutationFn: () => {
      const body = cfg.toBody ? cfg.toBody(form) : form;
      if (mode === "create") return api(`/api/${cfg.resource}`, { method: "POST", body });
      return api(`/api/${cfg.resource}/${initial.id}`, { method: "PUT", body });
    },
    onSuccess: () => { toast.success("Saved"); setOpen(false); qc.invalidateQueries({ queryKey: [cfg.queryKey] }); },
    onError: (e) => setError(e),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (o) { setForm(initial || { ...cfg.emptyForm }); setError(null); } }}>
      <DialogTrigger asChild>
        {mode === "create"
          ? <Button size="sm"><Plus className="mr-1 h-4 w-4" />New</Button>
          : <Button size="icon" variant="ghost"><Pencil className="h-4 w-4" /></Button>}
      </DialogTrigger>
      <DialogContent className="max-w-lg">
        <DialogHeader><DialogTitle>{mode === "create" ? `New ${cfg.title}` : `Edit ${cfg.title}`}</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <ApiAlert error={error} />
          {cfg.fields.map((f) => (
            <div key={f.name} className="space-y-1">
              <Label>{f.label}</Label>
              {f.type === "textarea" ? (
                <Textarea value={form[f.name] ?? ""} onChange={(e) => setForm({ ...form, [f.name]: e.target.value })} />
              ) : f.type === "checkbox" ? (
                <div className="flex items-center gap-2"><Checkbox checked={!!form[f.name]} onCheckedChange={(v) => setForm({ ...form, [f.name]: !!v })} /><span className="text-sm text-muted-foreground">{f.placeholder}</span></div>
              ) : f.type === "number" ? (
                <Input type="number" step="any" value={form[f.name] ?? ""} onChange={(e) => setForm({ ...form, [f.name]: e.target.value === "" ? "" : Number(e.target.value) })} placeholder={f.placeholder} />
              ) : (
                <Input value={form[f.name] ?? ""} onChange={(e) => setForm({ ...form, [f.name]: e.target.value })} placeholder={f.placeholder} />
              )}
            </div>
          ))}
        </div>
        <DialogFooter><Button onClick={() => save.mutate()} disabled={save.isPending}>{save.isPending ? "Saving..." : "Save"}</Button></DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
