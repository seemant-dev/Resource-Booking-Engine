import type { UserRole } from "@/types";

export function getRoleHome(role: UserRole): string {
  return role === "ADMIN" ? "/admin" : "/";
}

export function canAccessRole(requiredRole: UserRole, actualRole: UserRole): boolean {
  if (requiredRole === "USER") {
    return actualRole === "USER" || actualRole === "ADMIN";
  }

  return actualRole === "ADMIN";
}