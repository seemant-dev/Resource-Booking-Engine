"use client";

import Link from "next/link";
import { notFound, useParams } from "next/navigation";
import { SetBreadcrumb } from "@/components/layout";
import { Button, Field, FieldLabel } from "@/components/ui";
import { getResourceById } from "@/data";

/**
 * Screen 11 — Generate slots. A distinct bulk operation, not folded into
 * resource CRUD, matching the approved design's reasoning: it's a
 * separate workflow (date range + interval → generate), not a quick edit.
 * Past, unbooked slots are removed automatically by a scheduled job on
 * the backend — there is no admin action for that part of the flow.
 */
export default function GenerateSlotsPage() {
  const params = useParams<{ resourceId: string }>();
  const resource = getResourceById(params.resourceId);

  if (!resource) notFound();

  return (
    <div className="max-w-sm">
      <SetBreadcrumb>
        <Link href="/admin/resources" className="hover:text-ink">
          Resources
        </Link>{" "}
        <Link href={`/admin/resources/${resource.id}`} className="hover:text-ink">
          / {resource.name}
        </Link>{" "}
        <b className="text-ink">/ Generate slots</b>
      </SetBreadcrumb>

      <h1 className="text-ink text-xl font-bold">Generate slots</h1>
      <p className="text-ink-3 mb-5 text-sm">Bulk-create bookable time slots for {resource.name}</p>

      <FieldLabel>Date range</FieldLabel>
      <Field>1 Jul 2026 → 14 Jul 2026</Field>

      <FieldLabel>Slot length</FieldLabel>
      <Field>30 minutes</Field>

      <Button variant="primaryAdmin" className="mt-1">
        Generate slots
      </Button>
    </div>
  );
}
