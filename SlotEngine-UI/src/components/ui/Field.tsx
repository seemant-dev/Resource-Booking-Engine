import type { ReactNode } from "react";
import { cn } from "@/lib/cn";

interface FieldLabelProps {
  children: ReactNode;
}

/** Small uppercase-weight label above a field, matching `.d-field-label`. */
export function FieldLabel({ children }: FieldLabelProps) {
  return <div className="text-ink-2 mb-1 text-xs font-semibold">{children}</div>;
}

interface FieldProps {
  children: ReactNode;
  className?: string;
}

/**
 * Static field-shaped container, used for both real inputs (login/register
 * forms) and read-only display values (settings, resource detail) so both
 * render with identical proportions, matching `.d-field` in the approved
 * design. This build has no form submission logic — fields are presentational.
 */
export function Field({ children, className }: FieldProps) {
  return (
    <div
      className={cn(
        "border-screen-line text-ink-3 mb-2.5 flex h-10 items-center rounded-md border bg-white px-3 text-sm",
        className,
      )}
    >
      {children}
    </div>
  );
}
