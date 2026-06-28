import type { ReactNode } from "react";

/**
 * Layout for /login and /register. Deliberately has no sidebar or topbar —
 * the approved design shows these as plain centered screens since role is
 * unknown until the (mock) login response would resolve it.
 */
export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="bg-bg flex min-h-screen items-center justify-center px-4">
      <div className="border-line w-full max-w-sm rounded-xl border bg-white p-8 shadow-sm">
        {children}
      </div>
    </div>
  );
}
