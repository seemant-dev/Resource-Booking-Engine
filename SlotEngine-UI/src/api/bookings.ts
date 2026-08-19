import { apiRequest } from "./client";

export type BookingStatus = "CONFIRMED" | "CANCELLED";

export type BookingResponse = {
  id: number;
  slotId: number;
  slotStart: string;
  slotEnd: string;
  resourceId: number;
  resourceName: string;
  status: BookingStatus;
  bookedAt: string;
};

export function getMyBookings(): Promise<BookingResponse[]> {
  return apiRequest<BookingResponse[]>("/api/bookings/me", {
    method: "GET",
  });
}

export function cancelBooking(id: number): Promise<BookingResponse> {
  return apiRequest<BookingResponse>(`/api/bookings/${id}`, {
    method: "DELETE",
  });
}
