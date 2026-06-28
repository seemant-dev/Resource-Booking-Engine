import Link from "next/link";
import { SetBreadcrumb } from "@/components/layout";
import { Badge, Button, Card, MetricTile } from "@/components/ui";
import { getBookingsForUser, mockCurrentUser, mockUserOverview } from "@/data";

/**
 * Screen 03 — Home. The first page a user lands on after logging in:
 * a quick orientation (upcoming booking, three count tiles) and a clear
 * way into the booking flow. No charts, by design — matches Section 6
 * of the project spec, which rules out elaborate/analytics UI.
 */
export default function UserHomePage() {
  const bookings = getBookingsForUser(mockCurrentUser.id);
  const nextBooking = bookings.find(
    (booking) => booking.status === "CONFIRMED" || booking.status === "LOCKED",
  );

  return (
    <div className="max-w-3xl">
      <SetBreadcrumb>
        <b className="text-ink">Home</b>
      </SetBreadcrumb>

      <h1 className="text-ink text-xl font-bold">
        Welcome back, {mockCurrentUser.fullName.split(" ")[0]}
      </h1>
      <p className="text-ink-3 mb-5 text-sm">Here&apos;s what&apos;s coming up</p>

      <div className="mb-5 flex gap-3">
        <MetricTile label="Upcoming" value={mockUserOverview.upcomingCount} />
        <MetricTile label="This week" value={mockUserOverview.thisWeekCount} />
        <MetricTile label="Resources" value={mockUserOverview.resourceCount} />
      </div>

      {nextBooking && (
        <Card className="mb-5 flex items-center justify-between px-4 py-3">
          <div>
            <div className="text-ink text-sm font-semibold">
              {nextBooking.resourceName} · {nextBooking.startTime}–{nextBooking.endTime}
            </div>
            <div className="text-ink-3 mt-0.5 text-xs">{formatDate(nextBooking.date)}</div>
          </div>
          <Badge tone={nextBooking.status === "LOCKED" ? "amber" : "green"}>
            {nextBooking.status === "LOCKED" ? "Confirmed · cancel locked" : "Confirmed"}
          </Badge>
        </Card>
      )}

      <Link href="/resources">
        <Button>Browse resources →</Button>
      </Link>
    </div>
  );
}

function formatDate(isoDate: string): string {
  const date = new Date(`${isoDate}T00:00:00`);
  const weekday = date.toLocaleDateString("en-GB", { weekday: "short" });
  const day = date.getDate();
  const month = date.toLocaleDateString("en-GB", { month: "short" });
  const year = date.getFullYear();
  return `${weekday}, ${day} ${month} ${year}`;
}
