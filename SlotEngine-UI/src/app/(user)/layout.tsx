import type { ReactNode } from "react";
import { AppShell } from "@/components/layout";
import { mockCurrentUser } from "@/data";

/**
 * Shared shell for every user-facing route: Home (/), Resources,
 * My bookings, Settings. Each page sets its own breadcrumb via
 * <SetBreadcrumb>; this layout just provides the persistent chrome.
 */
export default function UserLayout({ children }: { children: ReactNode }) {
  return (
    <AppShell variant="user" userLabel={mockCurrentUser.email}>
      {children}
    </AppShell>
  );
}
