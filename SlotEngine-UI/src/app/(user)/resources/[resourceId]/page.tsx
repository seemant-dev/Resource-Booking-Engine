"use client";

import Link from "next/link";
import { notFound, useParams } from "next/navigation";
import { useState } from "react";
import { CalendarX } from "lucide-react";
import { SetBreadcrumb } from "@/components/layout";
import { BookingConfirmModal, BookingConflictModal } from "@/components/bookings";
import { SlotGrid } from "@/components/resources";
import { Button, EmptyState, Field, Toast } from "@/components/ui";
import { getResourceById, getSlotsForResource } from "@/data";
import type { Slot } from "@/types";

/**
 * Screen 05 — Resource detail / slot calendar, the core screen of the
 * whole application. Also folds in three states the approved design
 * drew separately since they all live on this one route:
 *  - 05A: booking confirm modal
 *  - 05B: booking success toast
 *  - State A: no-slots-today empty state (shown for resources with no
 *    mock slots, e.g. Lab 1)
 *  - State B: booking conflict (reachable via a "Simulate conflict"
 *    demo action, since there is no real concurrent request to trigger it)
 *
 * All booking/cancellation here is local component state — selecting and
 * "confirming" a slot does not call any API.
 */
export default function ResourceDetailPage() {
  const params = useParams<{ resourceId: string }>();
  const resource = getResourceById(params.resourceId);
  const slots = getSlotsForResource(params.resourceId);

  const [selectedSlot, setSelectedSlot] = useState<Slot | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [conflictOpen, setConflictOpen] = useState(false);
  const [bookedSlot, setBookedSlot] = useState<Slot | null>(null);

  if (!resource) notFound();

  const handleConfirm = () => {
    setConfirmOpen(false);
    setBookedSlot(selectedSlot);
    setSelectedSlot(null);
  };

  return (
    <div className="max-w-3xl">
      <SetBreadcrumb>
        <Link href="/resources" className="hover:text-ink">
          Resources
        </Link>{" "}
        <b className="text-ink">/ {resource.name}</b>
      </SetBreadcrumb>

      <div className="mb-4 flex items-end justify-between">
        <div>
          <h1 className="text-ink text-xl font-bold">{resource.name}</h1>
          <p className="text-ink-3 text-sm">
            {resource.type} · {resource.capacity} seats
          </p>
        </div>
        <Field className="mb-0 w-40">Thu, 2 Jul 2026 ▾</Field>
      </div>

      {slots.length === 0 ? (
        <EmptyState
          icon={<CalendarX className="h-5 w-5" />}
          title="No slots available Thu 2 Jul"
          description={`${resource.name} is fully booked for this date. Try another day or resource.`}
          action={<Button variant="ghost">Pick another date</Button>}
        />
      ) : (
        <>
          <div className="mb-2 flex items-center justify-between">
            <span className="text-ink text-sm font-semibold">Available slots</span>
            <span className="text-ink-3 text-xs">30 min each</span>
          </div>

          <SlotGrid slots={slots} onSelect={setSelectedSlot} />

          <div className="mt-4 flex items-center justify-between">
            <span className="text-ink-2 text-sm">
              {selectedSlot ? (
                <>
                  Selected:{" "}
                  <b className="text-ink">
                    {selectedSlot.startTime} – {selectedSlot.endTime}
                  </b>
                </>
              ) : (
                "Select a slot to continue"
              )}
            </span>
            <div className="flex gap-2">
              <Button variant="ghost" onClick={() => setConflictOpen(true)}>
                Simulate conflict
              </Button>
              <Button disabled={!selectedSlot} onClick={() => setConfirmOpen(true)}>
                Book this slot
              </Button>
            </div>
          </div>

          {bookedSlot && (
            <div className="mt-5">
              <Toast>
                Booking confirmed — {resource.name}, {bookedSlot.startTime}–{bookedSlot.endTime}
              </Toast>
            </div>
          )}
        </>
      )}

      <BookingConfirmModal
        open={confirmOpen}
        slot={selectedSlot}
        resourceName={resource.name}
        onClose={() => setConfirmOpen(false)}
        onConfirm={handleConfirm}
      />

      <BookingConflictModal
        open={conflictOpen}
        resourceName={resource.name}
        slotLabel={
          selectedSlot ? `${selectedSlot.startTime}–${selectedSlot.endTime}` : "10:30–11:00"
        }
        onClose={() => setConflictOpen(false)}
      />
    </div>
  );
}
