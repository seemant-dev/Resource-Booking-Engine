import type { ReactNode } from "react";
import { AppShell } from "@/components/layout";
import { mockCurrentAdmin } from "@/data";

/**
 * Shared shell for every admin-facing route: Admin home, Resources,
 * Resource detail, Generate slots, All bookings, Settings.
 */
export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <AppShell variant="admin" userLabel={mockCurrentAdmin.email}>
      {children}
    </AppShell>
  );
}
