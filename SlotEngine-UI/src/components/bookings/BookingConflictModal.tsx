"use client";

import { Button, Modal } from "@/components/ui";

interface BookingConflictModalProps {
  open: boolean;
  resourceName: string;
  slotLabel: string;
  onClose: () => void;
}

/**
 * State B — Booking conflict. Demonstrates the UI response to a 409
 * Conflict (the core concurrency case the whole project exists to prove
 * out), without an actual concurrent request behind it. Surfaced from
 * the resource detail page via a "Simulate conflict" affordance so the
 * state is reachable and visible, not just described in a comment.
 */
export function BookingConflictModal({
  open,
  resourceName,
  slotLabel,
  onClose,
}: BookingConflictModalProps) {
  return (
    <Modal open={open} onClose={onClose}>
      <div className="text-ink mb-1 text-sm font-bold">That slot&apos;s just been taken</div>
      <p className="text-ink-3 mb-4 text-sm">
        Someone booked {resourceName} · {slotLabel} a moment before you. Pick another slot below.
      </p>
      <Button variant="ghost" className="w-full justify-center" onClick={onClose}>
        Choose another slot
      </Button>
    </Modal>
  );
}
