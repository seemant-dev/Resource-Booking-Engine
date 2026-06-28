import type { Resource } from "@/types";

/**
 * Static mock resources.
 *
 * Mirrors what `GET /api/resources` is expected to return once the backend
 * exists. The four resources here match the examples used throughout the
 * approved design (Room A, Room B, Conference Hall, Lab 1).
 */
export const mockResources: Resource[] = [
  {
    id: "room-a",
    name: "Room A",
    type: "Meeting room",
    description: "6-seat meeting room, 3rd floor",
    capacity: 6,
    activeSlotCount: 24,
    availability: "open",
  },
  {
    id: "room-b",
    name: "Room B",
    type: "Meeting room",
    description: "4-seat meeting room, 3rd floor",
    capacity: 4,
    activeSlotCount: 18,
    availability: "open",
  },
  {
    id: "conference-hall",
    name: "Conference Hall",
    type: "Hall",
    description: "Full-floor conference hall with AV setup",
    capacity: 40,
    activeSlotCount: 9,
    availability: "limited",
  },
  {
    id: "lab-1",
    name: "Lab 1",
    type: "Equipment bay",
    description: "Shared equipment and testing bay",
    capacity: 8,
    activeSlotCount: 0,
    availability: "full",
  },
];

export function getResourceById(resourceId: string): Resource | undefined {
  return mockResources.find((resource) => resource.id === resourceId);
}
