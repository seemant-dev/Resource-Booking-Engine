"use client";

import { useBreadcrumbContext } from "./BreadcrumbContext";

interface TopbarProps {
  userLabel?: string;
}

/**
 * Top bar showing the current breadcrumb and the signed-in person's
 * email/avatar, matching `.dtopbar` in the approved design. Reads the
 * breadcrumb from BreadcrumbContext so individual pages can set it via
 * <SetBreadcrumb> without the shell needing route-specific knowledge.
 */
export function Topbar({ userLabel }: TopbarProps) {
  const { breadcrumb } = useBreadcrumbContext();

  return (
    <header className="border-screen-line flex h-12 flex-shrink-0 items-center justify-between border-b bg-white px-5">
      <div className="text-ink-3 text-sm">{breadcrumb}</div>
      <div className="flex items-center gap-2.5">
        {userLabel && <span className="text-ink-2 text-sm">{userLabel}</span>}
        <div className="bg-line-2 h-7 w-7 flex-shrink-0 rounded-full" />
      </div>
    </header>
  );
}
