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

export type BookingPageResponse = {
  content: BookingResponse[];
  page: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
};

export function getMyBookings(page = 0): Promise<BookingPageResponse> {
  return apiRequest<BookingPageResponse>(`/api/bookings/me?page=${page}`, {
    method: "GET",
  });
}

export function cancelBooking(id: number): Promise<BookingResponse> {
  return apiRequest<BookingResponse>(`/api/bookings/${id}`, {
    method: "DELETE",
  });
}
