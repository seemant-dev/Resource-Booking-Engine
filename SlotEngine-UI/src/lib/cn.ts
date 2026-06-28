import clsx, { type ClassValue } from "clsx";

/**
 * Thin wrapper around clsx for composing conditional Tailwind classes.
 * Kept as its own helper (rather than calling clsx directly everywhere)
 * so a class-merging library like tailwind-merge can be dropped in later
 * without touching every call site.
 */
export function cn(...inputs: ClassValue[]): string {
  return clsx(inputs);
}
