"use client";

import { useEffect, type ReactNode } from "react";
import { useBreadcrumbContext } from "./BreadcrumbContext";

/**
 * Pages render this once near the top of their content to set the
 * topbar's breadcrumb for the duration that page is mounted. Resets to
 * null on unmount so the next page's breadcrumb isn't left stale.
 */
export function SetBreadcrumb({ children }: { children: ReactNode }) {
  const { setBreadcrumb } = useBreadcrumbContext();

  useEffect(() => {
    setBreadcrumb(children);
    return () => setBreadcrumb(null);
  }, [children, setBreadcrumb]);

  return null;
}
