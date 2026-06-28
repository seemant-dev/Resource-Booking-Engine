"use client";

import type { Slot } from "@/types";
import { Button, Modal } from "@/components/ui";

interface BookingConfirmModalProps {
  open: boolean;
  slot: Slot | null;
  resourceName: string;
  onClose: () => void;
  onConfirm: () => void;
}

/**
 * Screen 05A — Confirm booking. Mirrors the approved design's modal copy
 * exactly: resource name, date, and time range, with Cancel / Confirm
 * actions. Confirming here triggers the local "success" toast state on
 * the resource detail page — there is no POST /slots/{id}/book call.
 */
export function BookingConfirmModal({
  open,
  slot,
  resourceName,
  onClose,
  onConfirm,
}: BookingConfirmModalProps) {
  if (!slot) return null;

  return (
    <Modal open={open} onClose={onClose}>
      <div className="text-ink mb-1 text-sm font-bold">Confirm your booking</div>
      <p className="text-ink-3 mb-4 text-sm">
        {resourceName} · {slot.startTime}–{slot.endTime}
      </p>
      <div className="flex gap-2">
        <Button variant="ghost" className="flex-1 justify-center" onClick={onClose}>
          Cancel
        </Button>
        <Button className="flex-1 justify-center" onClick={onConfirm}>
          Confirm
        </Button>
      </div>
    </Modal>
  );
}
