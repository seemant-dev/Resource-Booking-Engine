import type { ReactNode } from "react";
import { cn } from "@/lib/cn";

export type BadgeTone = "green" | "amber" | "red" | "gray" | "blue";

const TONE_CLASSES: Record<BadgeTone, string> = {
  green: "bg-accent-bg text-accent",
  amber: "bg-warn-bg text-warn-ink",
  red: "bg-danger-bg text-danger",
  gray: "bg-shared-bg text-ink-3",
  blue: "bg-user-bg text-user",
};

interface BadgeProps {
  tone: BadgeTone;
  children: ReactNode;
  className?: string;
}

/**
 * Small status pill used across resource cards, booking tables, and
 * metric tiles. Mirrors `.pill.green/.amber/.red/.gray/.blue` in the
 * approved design board.
 */
export function Badge({ tone, children, className }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold",
        TONE_CLASSES[tone],
        className,
      )}
    >
      {children}
    </span>
  );
}
