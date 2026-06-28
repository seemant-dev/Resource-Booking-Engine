import Link from "next/link";
import type { Resource } from "@/types";
import { Badge, type BadgeTone } from "@/components/ui";

const AVAILABILITY_LABEL: Record<Resource["availability"], string> = {
  open: "Slots open",
  limited: "Limited",
  full: "Fully booked today",
};

const AVAILABILITY_TONE: Record<Resource["availability"], BadgeTone> = {
  open: "green",
  limited: "amber",
  full: "gray",
};

interface ResourceCardProps {
  resource: Resource;
}

/**
 * A single resource card in the discovery grid, matching `.res-card` in
 * the approved design. The whole card links to the resource detail
 * screen — this is the "browse then book" entry point the approved
 * design settled on.
 */
export function ResourceCard({ resource }: ResourceCardProps) {
  return (
    <Link
      href={`/resources/${resource.id}`}
      className="border-screen-line block rounded-lg border bg-white p-4 transition-shadow hover:shadow-md"
    >
      <div className="bg-user-bg mb-3 h-20 rounded-md" />
      <div className="text-ink text-sm font-semibold">{resource.name}</div>
      <div className="text-ink-3 mt-0.5 text-xs">
        {resource.type} · {resource.capacity} seats
      </div>
      <Badge tone={AVAILABILITY_TONE[resource.availability]} className="mt-2">
        {AVAILABILITY_LABEL[resource.availability]}
      </Badge>
    </Link>
  );
}
