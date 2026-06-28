"use client";

import { createContext, useContext, useState, type ReactNode } from "react";

interface BreadcrumbContextValue {
  breadcrumb: ReactNode;
  setBreadcrumb: (node: ReactNode) => void;
}

const BreadcrumbContext = createContext<BreadcrumbContextValue | null>(null);

/**
 * Lightweight context so each page can set its own topbar breadcrumb
 * (e.g. "Resources / Room A") without the shell layout needing to know
 * about every route's title in advance. This mirrors how the approved
 * design varies the breadcrumb per screen while keeping one shared
 * Topbar component.
 */
export function BreadcrumbProvider({ children }: { children: ReactNode }) {
  const [breadcrumb, setBreadcrumb] = useState<ReactNode>(null);
  return (
    <BreadcrumbContext.Provider value={{ breadcrumb, setBreadcrumb }}>
      {children}
    </BreadcrumbContext.Provider>
  );
}

export function useBreadcrumbContext() {
  const ctx = useContext(BreadcrumbContext);
  if (!ctx) throw new Error("useBreadcrumbContext must be used within BreadcrumbProvider");
  return ctx;
}
