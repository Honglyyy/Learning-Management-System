import { useState } from "react";
import { X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Input } from "@/components/ui/input";

export type MultiSelectOption = { id: number | string; label: string };

export function MultiSelect({
  options,
  selected,
  onChange,
  placeholder = "Select items...",
}: {
  options: MultiSelectOption[];
  selected: (number | string)[];
  onChange: (ids: (number | string)[]) => void;
  placeholder?: string;
}) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");

  const filtered = options.filter((o) =>
    o.label.toLowerCase().includes(search.toLowerCase())
  );

  const selectedLabels = options
    .filter((o) => selected.includes(o.id))
    .map((o) => o.label);

  const toggle = (id: number | string) => {
    if (selected.includes(id)) {
      onChange(selected.filter((s) => s !== id));
    } else {
      onChange([...selected, id]);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" className="justify-start text-left font-normal">
          {selectedLabels.length > 0 ? (
            <div className="flex flex-wrap gap-1">
              {selectedLabels.slice(0, 2).map((label) => (
                <Badge key={label} variant="secondary" className="text-xs">
                  {label}
                </Badge>
              ))}
              {selectedLabels.length > 2 && (
                <Badge variant="secondary" className="text-xs">
                  +{selectedLabels.length - 2}
                </Badge>
              )}
            </div>
          ) : (
            <span className="text-muted-foreground">{placeholder}</span>
          )}
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Select items</DialogTitle>
        </DialogHeader>
        <div className="space-y-3">
          <Input
            placeholder="Search..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <ScrollArea className="h-64 border rounded">
            <div className="p-3 space-y-2">
              {filtered.map((o) => (
                <button
                  key={o.id}
                  onClick={() => toggle(o.id)}
                  className={`w-full text-left px-3 py-2 rounded text-sm transition ${
                    selected.includes(o.id)
                      ? "bg-primary text-primary-foreground"
                      : "hover:bg-accent"
                  }`}
                >
                  {o.label}
                </button>
              ))}
            </div>
          </ScrollArea>
          <Button onClick={() => setOpen(false)} className="w-full">
            Done
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
