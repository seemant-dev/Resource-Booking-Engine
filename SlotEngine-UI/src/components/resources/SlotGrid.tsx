"use client";

import { useState } from "react";
import type { Slot } from "@/types";
import { cn } from "@/lib/cn";

interface SlotGridProps {
  slots: Slot[];
  onSelect?: (slot: Slot | null) => void;
}

/**
 * The slot availability grid, matching `.cal-grid` / `.cal-cell` in the
 * approved design. Open slots are clickable and toggle a "selected"
 * visual state; taken slots are inert. This is local UI state only —
 * selecting a slot does not call any API, it just drives which slot the
 * "Book this slot" button on the parent page refers to.
 */
export function SlotGrid({ slots, onSelect }: SlotGridProps) {
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const handleSelect = (slot: Slot) => {
    if (slot.status === "taken") return;
    const next = selectedId === slot.id ? null : slot.id;
    setSelectedId(next);
    onSelect?.(next ? slot : null);
  };

  return (
    <div className="grid grid-cols-6 gap-2">
      {slots.map((slot) => {
        const isSelected = selectedId === slot.id;
        const isTaken = slot.status === "taken";

        return (
          <button
            key={slot.id}
            type="button"
            disabled={isTaken}
            onClick={() => handleSelect(slot)}
            className={cn(
              "rounded-md border px-2 py-2 text-xs font-medium transition-colors",
              isTaken &&
                "border-screen-line text-ink-3 cursor-not-allowed bg-[#f3f1ec] line-through",
              !isTaken && !isSelected && "border-user-bg text-user hover:bg-user-bg",
              isSelected && "border-user bg-user text-white",
            )}
          >
            {slot.startTime}
          </button>
        );
      })}
    </div>
  );
}
