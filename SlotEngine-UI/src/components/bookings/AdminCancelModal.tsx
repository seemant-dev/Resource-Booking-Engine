"use client";

import type { Booking } from "@/types";
import { Button, Modal } from "@/components/ui";

interface AdminCancelModalProps {
  open: boolean;
  booking: Booking | null;
  onClose: () => void;
  onConfirm: () => void;
}

/**
 * Screen 12A — Cancel booking (admin). Deliberately distinct copy from
 * the user's own cancel action: it names the override explicitly so an
 * admin cancellation is never mistaken for a routine one, matching the
 * approved design's reasoning for giving this its own confirmation state.
 */
export function AdminCancelModal({ open, booking, onClose, onConfirm }: AdminCancelModalProps) {
  if (!booking) return null;

  return (
    <Modal open={open} onClose={onClose}>
      <div className="text-ink mb-1 text-sm font-bold">Cancel this booking?</div>
      <p className="text-ink-3 mb-4 text-sm">
        {booking.userEmail} · {booking.resourceName} · {booking.startTime}. This overrides the
        user&apos;s cancellation cutoff and frees the slot immediately.
      </p>
      <div className="flex gap-2">
        <Button variant="ghost" className="flex-1 justify-center" onClick={onClose}>
          Keep booking
        </Button>
        <Button variant="dangerSolid" className="flex-1 justify-center" onClick={onConfirm}>
          Cancel booking
        </Button>
      </div>
    </Modal>
  );
}
