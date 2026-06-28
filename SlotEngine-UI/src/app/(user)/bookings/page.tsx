"use client";

import { useState } from "react";
import { SetBreadcrumb } from "@/components/layout";
import { BookingsTable } from "@/components/bookings";
import { getBookingsForUser, mockCurrentUser } from "@/data";
import type { Booking } from "@/types";

/**
 * Screen 06 — My bookings. Cancel is disabled within the 1-hour cutoff
 * window — shown as a locked state with the reason visible (per the
 * approved design), not a silently disabled button. Cancelling here only
 * updates local component state; nothing is persisted.
 */
export default function MyBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>(() => getBookingsForUser(mockCurrentUser.id));

  const handleCancel = (booking: Booking) => {
    setBookings((current) =>
      current.map((item) => (item.id === booking.id ? { ...item, status: "CANCELLED" } : item)),
    );
  };

  return (
    <div className="max-w-4xl">
      <SetBreadcrumb>
        <b className="text-ink">My bookings</b>
      </SetBreadcrumb>

      <h1 className="text-ink mb-4 text-xl font-bold">My bookings</h1>

      <BookingsTable bookings={bookings} mode="self" onCancelClick={handleCancel} />
    </div>
  );
}
