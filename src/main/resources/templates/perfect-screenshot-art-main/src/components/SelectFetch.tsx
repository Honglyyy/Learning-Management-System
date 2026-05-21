import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";

export function SelectFetch({
  resource,
  value,
  onChange,
  placeholder = "Select...",
  labelKey = "name",
}: {
  resource: string;
  value: number | string | "";
  onChange: (val: number | string) => void;
  placeholder?: string;
  labelKey?: string;
}) {
  const { data, isLoading } = useQuery<any[]>({
    queryKey: [`select-${resource}`],
    queryFn: () => api(`/api/${resource}`),
  });

  if (isLoading) return <Skeleton className="h-10 w-full" />;

  return (
    <Select value={String(value)} onValueChange={(v) => onChange(Number(v) || v)}>
      <SelectTrigger>
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent>
        {data?.map((item) => (
          <SelectItem key={item.id} value={String(item.id)}>
            {item[labelKey] || item.title || String(item.id)}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
