import type { ButtonHTMLAttributes, ReactNode } from "react";
import { cn } from "@/lib/cn";

export type ButtonVariant = "primary" | "primaryAdmin" | "ghost" | "danger" | "dangerSolid";

const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary: "bg-user text-white hover:bg-user/90",
  primaryAdmin: "bg-admin text-white hover:bg-admin/90",
  ghost: "bg-white border border-screen-line text-ink-2 hover:bg-bg",
  danger: "bg-white border border-danger-bg text-danger hover:bg-danger-bg",
  dangerSolid: "bg-danger text-white hover:bg-danger/90",
};

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  children: ReactNode;
}

/**
 * Button primitive used throughout the app. Mirrors `.d-btn` and its
 * `.primary` / `.primary.adm` / `.ghost` / `.danger` / `.dangersolid`
 * variants from the approved design board.
 */
export function Button({ variant = "primary", className, children, ...rest }: ButtonProps) {
  return (
    <button
      className={cn(
        "inline-flex items-center justify-center gap-1.5 rounded-md px-3.5 py-2 text-sm font-semibold transition-colors",
        "disabled:cursor-not-allowed disabled:opacity-50",
        VARIANT_CLASSES[variant],
        className,
      )}
      {...rest}
    >
      {children}
    </button>
  );
}
