import type { ReactNode } from "react";
import { BreadcrumbProvider } from "./BreadcrumbContext";
import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";

interface AppShellProps {
  variant: "user" | "admin";
  userLabel?: string;
  children: ReactNode;
}

/**
 * Full desktop shell: persistent sidebar + topbar + scrollable content
 * area, matching `.dframe` (sidebar + dmain) in the approved design.
 * Shared between the user and admin route groups so the chrome itself
 * is defined once; only the sidebar's nav items and accent differ.
 *
 * Wrapped in BreadcrumbProvider so any page rendered as `children` can
 * set the topbar's breadcrumb via <SetBreadcrumb>.
 */
export function AppShell({ variant, userLabel, children }: AppShellProps) {
  return (
    <BreadcrumbProvider>
      <div className="bg-screen-bg flex h-screen w-full overflow-hidden">
        <Sidebar variant={variant} />
        <div className="flex min-w-0 flex-1 flex-col">
          <Topbar userLabel={userLabel} />
          <main className="flex-1 overflow-y-auto px-8 py-6">{children}</main>
        </div>
      </div>
    </BreadcrumbProvider>
  );
}
