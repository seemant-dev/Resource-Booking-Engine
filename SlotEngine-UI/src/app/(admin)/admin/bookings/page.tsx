"use client";

import { useState } from "react";
import { SetBreadcrumb } from "@/components/layout";
import { AdminCancelModal, BookingsTable } from "@/components/bookings";
import { Field } from "@/components/ui";
import { mockBookings } from "@/data";
import type { Booking } from "@/types";

/**
 * Screen 12 — All bookings (admin), with Screen 12A's force-cancel
 * confirmation. Every user's bookings are visible here, unlike My
 * bookings which is scoped to the signed-in user. Admin cancel
 * overrides the cutoff rule that applies to a user's own cancel action.
 */
export default function AdminBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>(mockBookings);
  const [cancelTarget, setCancelTarget] = useState<Booking | null>(null);

  const handleConfirmCancel = () => {
    if (!cancelTarget) return;
    setBookings((current) =>
      current.map((item) =>
        item.id === cancelTarget.id ? { ...item, status: "CANCELLED" } : item,
      ),
    );
    setCancelTarget(null);
  };

  return (
    <div className="max-w-5xl">
      <SetBreadcrumb>
        <b className="text-ink">All bookings</b>
      </SetBreadcrumb>

      <h1 className="text-ink mb-4 text-xl font-bold">All bookings</h1>

      <Field className="w-64">Search by user or resource</Field>

      <BookingsTable bookings={bookings} mode="admin" onCancelClick={setCancelTarget} />

      <AdminCancelModal
        open={cancelTarget !== null}
        booking={cancelTarget}
        onClose={() => setCancelTarget(null)}
        onConfirm={handleConfirmCancel}
      />
    </div>
  );
}
