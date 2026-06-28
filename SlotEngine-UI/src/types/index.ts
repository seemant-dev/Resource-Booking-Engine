/**
 * Domain types for the Resource Booking Engine frontend.
 *
 * These mirror the shape the real backend API is expected to return
 * (see Section 11/12 of the approved design — Database Design & API Design).
 * Keeping them in one place means swapping mock data for real fetch calls
 * later does not require touching any component prop types.
 */

export type UserRole = "USER" | "ADMIN";

export interface User {
  id: string;
  fullName: string;
  email: string;
  role: UserRole;
}

export type ResourceAvailability = "open" | "limited" | "full";

export interface Resource {
  id: string;
  name: string;
  type: string;
  description: string;
  capacity: number;
  activeSlotCount: number;
  availability: ResourceAvailability;
}

export type SlotStatus = "open" | "taken" | "selected";

export interface Slot {
  id: string;
  resourceId: string;
  date: string; // ISO date, e.g. "2026-07-02"
  startTime: string; // "10:30"
  endTime: string; // "11:00"
  status: SlotStatus;
}

export type BookingStatus = "CONFIRMED" | "CANCELLED" | "PAST" | "LOCKED";

export interface Booking {
  id: string;
  resourceId: string;
  resourceName: string;
  userId: string;
  userEmail: string;
  date: string;
  startTime: string;
  endTime: string;
  status: BookingStatus;
  /** True when within the 1-hour cancellation cutoff window. */
  cancellationLocked: boolean;
}

export interface AdminOverviewMetrics {
  resourceCount: number;
  bookingsToday: number;
  slotsExpiringSoon: number;
}

export interface UserOverviewMetrics {
  upcomingCount: number;
  thisWeekCount: number;
  resourceCount: number;
}
