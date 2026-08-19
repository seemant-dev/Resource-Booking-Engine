"use client";

import { useState } from "react";
import { cn } from "@/lib/cn";
import type { Slot } from "@/types";

interface SlotGridProps {
  slots: Slot[];
  onSelect?: (slot: Slot | null) => void;
}

/**
 * Slot calendar grid. Owns only selection state; booking remains the page's
 * responsibility because the page controls confirmation and conflict UI.
 */
export function SlotGrid({ slots, onSelect }: SlotGridProps) {
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const handleSelect = (slot: Slot) => {
    if (slot.status === "taken") return;

    const nextSelectedId = selectedId === slot.id ? null : slot.id;
    setSelectedId(nextSelectedId);
    onSelect?.(nextSelectedId ? slot : null);
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
