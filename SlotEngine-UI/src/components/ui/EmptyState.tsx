import type { ReactNode } from "react";
import { cn } from "@/lib/cn";

interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description: string;
  action?: ReactNode;
  tone?: "neutral" | "danger";
}

/**
 * Centered icon + message + optional action, matching `.empty-state` in
 * the approved design. Reused for "no slots today", "no resources yet",
 * and the 403 "not authorized" page — the approved design treats these as
 * one shared pattern rather than bespoke screens.
 */
export function EmptyState({
  icon,
  title,
  description,
  action,
  tone = "neutral",
}: EmptyStateProps) {
  return (
    <div className="flex h-full flex-col items-center justify-center px-6 py-12 text-center">
      <div
        className={cn(
          "mb-3 flex h-12 w-12 items-center justify-center rounded-full",
          tone === "danger" ? "bg-danger-bg text-danger" : "bg-screen-line text-ink-3",
        )}
      >
        {icon}
      </div>
      <div className="text-ink mb-1.5 text-sm font-semibold">{title}</div>
      <p className="text-ink-3 mb-4 max-w-xs text-sm leading-relaxed">{description}</p>
      {action}
    </div>
  );
}
