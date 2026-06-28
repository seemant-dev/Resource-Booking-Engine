import type { ReactNode, TdHTMLAttributes, ThHTMLAttributes } from "react";
import { cn } from "@/lib/cn";

interface TableProps {
  children: ReactNode;
}

/** Wraps a native `<table>` with the approved design's row/border styling. */
export function Table({ children }: TableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-sm">{children}</table>
    </div>
  );
}

export function TableHeaderCell({
  className,
  children,
  ...rest
}: ThHTMLAttributes<HTMLTableCellElement>) {
  return (
    <th
      className={cn(
        "border-screen-line text-ink-3 border-b px-3 py-2 text-left text-xs font-semibold tracking-wide uppercase",
        className,
      )}
      {...rest}
    >
      {children}
    </th>
  );
}

export function TableCell({
  className,
  children,
  ...rest
}: TdHTMLAttributes<HTMLTableCellElement>) {
  return (
    <td className={cn("border-screen-line text-ink-2 border-b px-3 py-2.5", className)} {...rest}>
      {children}
    </td>
  );
}
