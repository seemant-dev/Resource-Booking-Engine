import type { HTMLAttributes, ReactNode } from "react";
import { cn } from "@/lib/cn";

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode;
}

/**
 * Generic bordered surface used as the base for resource cards, list rows,
 * and metric tiles. Mirrors the recurring `background: #fff; border: 1px
 * solid var(--screen-line); border-radius` pattern in the approved design.
 */
export function Card({ children, className, ...rest }: CardProps) {
  return (
    <div className={cn("border-screen-line rounded-lg border bg-white", className)} {...rest}>
      {children}
    </div>
  );
}
