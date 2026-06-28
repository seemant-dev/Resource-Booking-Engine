import type { ReactNode } from "react";

interface ToastProps {
  children: ReactNode;
}

/**
 * Inline confirmation banner, matching `.toast` in the approved design.
 * Used on the resource detail screen right after a booking succeeds, so
 * the flow closes visibly instead of leaving the user to assume success.
 */
export function Toast({ children }: ToastProps) {
  return (
    <div className="bg-ink flex items-center gap-2 rounded-md px-3.5 py-2.5 text-sm text-white">
      <span className="bg-accent-bg h-1.5 w-1.5 flex-shrink-0 rounded-full" />
      {children}
    </div>
  );
}
