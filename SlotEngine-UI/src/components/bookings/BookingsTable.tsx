"use client";

import type { Booking } from "@/types";
import { Badge, Table, TableCell, TableHeaderCell, type BadgeTone } from "@/components/ui";

const STATUS_LABEL: Record<Booking["status"], string> = {
  CONFIRMED: "Confirmed",
  CANCELLED: "Cancelled",
  PAST: "Past",
  LOCKED: "Locked",
};

const STATUS_TONE: Record<Booking["status"], BadgeTone> = {
  CONFIRMED: "green",
  CANCELLED: "red",
  PAST: "gray",
  LOCKED: "amber",
};

interface BookingsTableProps {
  bookings: Booking[];
  /**
   * "self" — My bookings (user): cancel respects the cutoff lock.
   * "admin" — All bookings (admin): cancel overrides the cutoff and
   * shows the user column, since an admin acts on anyone's booking.
   */
  mode: "self" | "admin";
  onCancelClick?: (booking: Booking) => void;
}

/**
 * Shared bookings table, matching `.d-table` in the approved design.
 * One component backs both Screen 06 (My bookings) and Screen 12
 * (All bookings) since their column layout and row styling are
 * identical apart from the user column and cancel semantics.
 */
export function BookingsTable({ bookings, mode, onCancelClick }: BookingsTableProps) {
  return (
    <Table>
      <thead>
        <tr>
          {mode === "admin" && <TableHeaderCell>User</TableHeaderCell>}
          <TableHeaderCell>Resource</TableHeaderCell>
          <TableHeaderCell>Date</TableHeaderCell>
          <TableHeaderCell>Time</TableHeaderCell>
          <TableHeaderCell>Status</TableHeaderCell>
          <TableHeaderCell />
        </tr>
      </thead>
      <tbody>
        {bookings.map((booking) => {
          const canCancel =
            booking.status === "CONFIRMED" && (mode === "admin" || !booking.cancellationLocked);

          return (
            <tr key={booking.id}>
              {mode === "admin" && (
                <TableCell className="text-ink font-medium">{booking.userEmail}</TableCell>
              )}
              <TableCell className="text-ink font-medium">{booking.resourceName}</TableCell>
              <TableCell>{formatDate(booking.date)}</TableCell>
              <TableCell>
                {booking.startTime}–{booking.endTime}
              </TableCell>
              <TableCell>
                <Badge tone={STATUS_TONE[booking.status]}>{STATUS_LABEL[booking.status]}</Badge>
              </TableCell>
              <TableCell>
                {canCancel ? (
                  <button
                    type="button"
                    onClick={() => onCancelClick?.(booking)}
                    className="text-danger text-xs font-semibold hover:underline"
                  >
                    Cancel
                  </button>
                ) : booking.status === "LOCKED" ? (
                  <Badge tone="gray">&lt; 1hr left</Badge>
                ) : null}
              </TableCell>
            </tr>
          );
        })}
      </tbody>
    </Table>
  );
}

function formatDate(isoDate: string): string {
  const date = new Date(`${isoDate}T00:00:00`);
  const weekday = date.toLocaleDateString("en-GB", { weekday: "short" });
  const day = date.getDate();
  const month = date.toLocaleDateString("en-GB", { month: "short" });
  return `${weekday}, ${day} ${month}`;
}
