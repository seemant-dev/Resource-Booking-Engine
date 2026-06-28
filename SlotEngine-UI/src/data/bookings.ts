import type { Booking } from "@/types";

/**
 * Static mock bookings.
 *
 * Serves two screens from the approved design:
 *  - "My bookings" (user) — filtered to the current mock user, see `mockCurrentUser`.
 *  - "All bookings" (admin) — the full unfiltered list.
 *
 * Mirrors `GET /api/bookings/me` and `GET /api/admin/bookings`, both paginated
 * in the real API; pagination UI is present but inert since there is no
 * backend to page against yet.
 */
export const mockBookings: Booking[] = [
  {
    id: "booking-1",
    resourceId: "room-a",
    resourceName: "Room A",
    userId: "user-1",
    userEmail: "a.khan@co.com",
    date: "2026-07-02",
    startTime: "10:30",
    endTime: "11:00",
    status: "CONFIRMED",
    cancellationLocked: false,
  },
  {
    id: "booking-2",
    resourceId: "room-b",
    resourceName: "Room B",
    userId: "user-1",
    userEmail: "a.khan@co.com",
    date: "2026-07-03",
    startTime: "14:00",
    endTime: "14:30",
    status: "LOCKED",
    cancellationLocked: true,
  },
  {
    id: "booking-3",
    resourceId: "lab-1",
    resourceName: "Lab 1",
    userId: "user-1",
    userEmail: "a.khan@co.com",
    date: "2026-06-29",
    startTime: "09:00",
    endTime: "10:00",
    status: "PAST",
    cancellationLocked: true,
  },
  {
    id: "booking-4",
    resourceId: "room-b",
    resourceName: "Room B",
    userId: "user-2",
    userEmail: "s.rao@co.com",
    date: "2026-07-03",
    startTime: "14:00",
    endTime: "14:30",
    status: "CONFIRMED",
    cancellationLocked: false,
  },
  {
    id: "booking-5",
    resourceId: "conference-hall",
    resourceName: "Conference Hall",
    userId: "user-3",
    userEmail: "m.iyer@co.com",
    date: "2026-06-29",
    startTime: "09:00",
    endTime: "10:00",
    status: "PAST",
    cancellationLocked: true,
  },
];

export function getBookingsForUser(userId: string): Booking[] {
  return mockBookings.filter((booking) => booking.userId === userId);
}
