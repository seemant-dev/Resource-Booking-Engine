"use client";

import type { ReactNode } from "react";
import { useEffect } from "react";
import { createPortal } from "react-dom";

interface ModalProps {
  open: boolean;
  onClose: () => void;
  children: ReactNode;
}

/**
 * Overlay + centered panel, matching `.d-modal-overlay` / `.d-modal` in the
 * approved design. Used for the booking confirmation step, the admin
 * force-cancel confirmation, and the slot-conflict message — all of which
 * the approved design treats as modal states rather than separate routes.
 *
 * This is a presentation-only modal: it has no submission logic, it simply
 * shows and hides content. Closing on Escape and disabling background
 * scroll are kept since they're expected baseline behavior, not visual
 * additions to the approved design.
 */
export function Modal({ open, onClose, children }: ModalProps) {
  useEffect(() => {
    if (!open) return;

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };

    document.addEventListener("keydown", handleKeyDown);
    document.body.style.overflow = "hidden";

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "";
    };
  }, [open, onClose]);

  if (!open || typeof document === "undefined") return null;

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-[#0f1018]/45 p-4"
      onClick={onClose}
      role="presentation"
    >
      <div
        className="border-screen-line w-full max-w-sm rounded-xl border bg-white p-5 shadow-xl"
        onClick={(event) => event.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        {children}
      </div>
    </div>,
    document.body,
  );
}
