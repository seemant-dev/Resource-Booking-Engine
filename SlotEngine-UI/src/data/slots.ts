import type { Slot } from "@/types";

/**
 * Static mock slots, keyed loosely by resource.
 *
 * Mirrors `GET /api/resources/{id}/slots?date=` — the hot-path query in the
 * approved design, backed by the composite index on (resource_id, slot_start).
 * Only Room A has a populated, mixed open/taken grid since it's the resource
 * used in the approved design's hero "Resource detail" screen; the others
 * have enough data to demonstrate their distinct availability states.
 */
export const mockSlotsByResource: Record<string, Slot[]> = {
  "room-a": [
    {
      id: "slot-1",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "09:00",
      endTime: "09:30",
      status: "open",
    },
    {
      id: "slot-2",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "09:30",
      endTime: "10:00",
      status: "taken",
    },
    {
      id: "slot-3",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "10:00",
      endTime: "10:30",
      status: "open",
    },
    {
      id: "slot-4",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "10:30",
      endTime: "11:00",
      status: "open",
    },
    {
      id: "slot-5",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "11:00",
      endTime: "11:30",
      status: "open",
    },
    {
      id: "slot-6",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "11:30",
      endTime: "12:00",
      status: "taken",
    },
    {
      id: "slot-7",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "13:00",
      endTime: "13:30",
      status: "open",
    },
    {
      id: "slot-8",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "13:30",
      endTime: "14:00",
      status: "open",
    },
    {
      id: "slot-9",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "14:00",
      endTime: "14:30",
      status: "taken",
    },
    {
      id: "slot-10",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "14:30",
      endTime: "15:00",
      status: "open",
    },
    {
      id: "slot-11",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "15:00",
      endTime: "15:30",
      status: "open",
    },
    {
      id: "slot-12",
      resourceId: "room-a",
      date: "2026-07-02",
      startTime: "15:30",
      endTime: "16:00",
      status: "taken",
    },
  ],
  "room-b": [
    {
      id: "slot-13",
      resourceId: "room-b",
      date: "2026-07-02",
      startTime: "09:00",
      endTime: "09:30",
      status: "open",
    },
    {
      id: "slot-14",
      resourceId: "room-b",
      date: "2026-07-02",
      startTime: "09:30",
      endTime: "10:00",
      status: "open",
    },
    {
      id: "slot-15",
      resourceId: "room-b",
      date: "2026-07-02",
      startTime: "10:00",
      endTime: "10:30",
      status: "taken",
    },
    {
      id: "slot-16",
      resourceId: "room-b",
      date: "2026-07-02",
      startTime: "10:30",
      endTime: "11:00",
      status: "open",
    },
  ],
  "conference-hall": [
    {
      id: "slot-17",
      resourceId: "conference-hall",
      date: "2026-07-02",
      startTime: "09:00",
      endTime: "10:00",
      status: "taken",
    },
    {
      id: "slot-18",
      resourceId: "conference-hall",
      date: "2026-07-02",
      startTime: "10:00",
      endTime: "11:00",
      status: "open",
    },
  ],
  "lab-1": [],
};

export function getSlotsForResource(resourceId: string): Slot[] {
  return mockSlotsByResource[resourceId] ?? [];
}
