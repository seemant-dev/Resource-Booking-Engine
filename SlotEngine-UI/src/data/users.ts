import type { AdminOverviewMetrics, User, UserOverviewMetrics } from "@/types";

/**
 * The mock "currently signed in" user.
 *
 * There is no auth in this build (see README — Backend Integration section),
 * so this stands in for whatever a real session/JWT payload would resolve to.
 * Components read this instead of hardcoding a name/email inline.
 */
export const mockCurrentUser: User = {
  id: "user-1",
  fullName: "Aisha Khan",
  email: "a.khan@co.com",
  role: "USER",
};

export const mockCurrentAdmin: User = {
  id: "admin-1",
  fullName: "Workspace Admin",
  email: "admin@co.com",
  role: "ADMIN",
};

/** Static counts for the user Home screen's three metric tiles. */
export const mockUserOverview: UserOverviewMetrics = {
  upcomingCount: 2,
  thisWeekCount: 1,
  resourceCount: 4,
};

/** Static counts for the Admin Home screen's three metric tiles (no charts, per the approved design). */
export const mockAdminOverview: AdminOverviewMetrics = {
  resourceCount: 4,
  bookingsToday: 11,
  slotsExpiringSoon: 6,
};
