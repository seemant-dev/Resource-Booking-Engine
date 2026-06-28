import Link from "next/link";
import { SetBreadcrumb } from "@/components/layout";
import { Button, MetricTile } from "@/components/ui";
import { mockAdminOverview } from "@/data";

/**
 * Screen 08 — Admin home. Counts only — no graphs. Section 6 of the
 * project's own spec explicitly cuts admin analytics dashboards; this
 * stays a thin summary, not a BI view.
 */
export default function AdminHomePage() {
  return (
    <div className="max-w-3xl">
      <SetBreadcrumb>
        <b className="text-ink">Admin home</b>
      </SetBreadcrumb>

      <h1 className="text-ink text-xl font-bold">Overview</h1>
      <p className="text-ink-3 mb-5 text-sm">A quick read, not a dashboard with charts</p>

      <div className="mb-5 flex gap-3">
        <MetricTile label="Resources" value={mockAdminOverview.resourceCount} />
        <MetricTile label="Bookings today" value={mockAdminOverview.bookingsToday} />
        <MetricTile label="Slots expiring" value={mockAdminOverview.slotsExpiringSoon} />
      </div>

      <Link href="/admin/resources">
        <Button variant="primaryAdmin">Manage resources →</Button>
      </Link>
    </div>
  );
}
