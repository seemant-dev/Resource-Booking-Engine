import type { UserRole } from "@/types";
import { apiRequest } from "./client";

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
};

export type LoginResponse = {
  role: UserRole;
  name: string;
};

export type RegisterResponse = {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  createdAt: string;
};

export function login(request: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: request,
  });
}

export function register(request: RegisterRequest): Promise<RegisterResponse> {
  return apiRequest<RegisterResponse>("/api/auth/register", {
    method: "POST",
    body: request,
  });
}

export function logout(): Promise<void> {
  return apiRequest<void>("/api/auth/logout", {
    method: "POST",
  });
}
