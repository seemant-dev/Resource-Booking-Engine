"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { CalendarCheck, LayoutGrid, LogOut, Settings, ListChecks, Building2 } from "lucide-react";
import { cn } from "@/lib/cn";

interface NavItem {
  label: string;
  href: string;
  icon: typeof LayoutGrid;
}

const USER_NAV_ITEMS: NavItem[] = [
  { label: "Home", href: "/", icon: LayoutGrid },
  { label: "Resources", href: "/resources", icon: Building2 },
  { label: "My bookings", href: "/bookings", icon: CalendarCheck },
];

const ADMIN_NAV_ITEMS: NavItem[] = [
  { label: "Home", href: "/admin", icon: LayoutGrid },
  { label: "Resources", href: "/admin/resources", icon: Building2 },
  { label: "All bookings", href: "/admin/bookings", icon: ListChecks },
];

interface SidebarProps {
  variant: "user" | "admin";
}

/**
 * Persistent left navigation, matching `.dsidebar` in the approved design.
 * Two variants share one component since their structure (brand, nav
 * items, spacer, settings, logout) is identical — only the item list and
 * accent color differ between the user and admin shells.
 *
 * "Active" state is derived from the current route rather than hardcoded,
 * so navigation reflects the actual page the person is on.
 */
export function Sidebar({ variant }: SidebarProps) {
  const pathname = usePathname();
  const items = variant === "user" ? USER_NAV_ITEMS : ADMIN_NAV_ITEMS;
  const accentClass = variant === "user" ? "bg-user" : "bg-admin";
  const settingsHref = variant === "user" ? "/settings" : "/admin/settings";

  const isActive = (href: string) => {
    if (href === "/" || href === "/admin") return pathname === href;
    return pathname === href || pathname.startsWith(`${href}/`);
  };

  return (
    <aside className="bg-sidebar-bg flex h-full w-56 flex-shrink-0 flex-col px-3 py-5">
      <div className="mb-6 px-2 text-sm font-bold text-white">
        BookEngine{variant === "admin" && <span className="text-sidebar-text"> admin</span>}
      </div>

      <nav className="flex flex-col gap-0.5">
        {items.map((item) => {
          const active = isActive(item.href);
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm transition-colors",
                active
                  ? "bg-white/10 text-white"
                  : "text-sidebar-text hover:bg-white/5 hover:text-white",
              )}
            >
              <Icon
                className={cn(
                  "h-4 w-4 flex-shrink-0",
                  active && accentClass.replace("bg-", "text-"),
                )}
                strokeWidth={2}
              />
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="flex-1" />

      <div className="border-sidebar-line flex flex-col gap-0.5 border-t pt-3">
        <Link
          href={settingsHref}
          className={cn(
            "flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm transition-colors",
            isActive(settingsHref)
              ? "bg-white/10 text-white"
              : "text-sidebar-text hover:bg-white/5 hover:text-white",
          )}
        >
          <Settings className="h-4 w-4 flex-shrink-0" strokeWidth={2} />
          Settings
        </Link>
        <Link
          href="/login"
          className="flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm text-[#e09b8f] transition-colors hover:bg-white/5"
        >
          <LogOut className="h-4 w-4 flex-shrink-0" strokeWidth={2} />
          Log out
        </Link>
      </div>
    </aside>
  );
}
