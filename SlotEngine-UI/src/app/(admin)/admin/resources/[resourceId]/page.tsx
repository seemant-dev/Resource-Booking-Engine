"use client";

import Link from "next/link";
import { notFound, useParams } from "next/navigation";
import { SetBreadcrumb } from "@/components/layout";
import { Button, Field, FieldLabel } from "@/components/ui";
import { getResourceById } from "@/data";

/**
 * Screen 10 — Resource detail (admin). Resource CRUD and the entry point
 * into slot generation, kept on one screen since editing a resource and
 * adding its slots are part of the same admin task, per the approved
 * design's reasoning.
 */
export default function AdminResourceDetailPage() {
  const params = useParams<{ resourceId: string }>();
  const resource = getResourceById(params.resourceId);

  if (!resource) notFound();

  return (
    <div className="max-w-md">
      <SetBreadcrumb>
        <Link href="/admin/resources" className="hover:text-ink">
          Resources
        </Link>{" "}
        <b className="text-ink">/ {resource.name}</b>
      </SetBreadcrumb>

      <FieldLabel>Name</FieldLabel>
      <Field>{resource.name}</Field>

      <FieldLabel>Description</FieldLabel>
      <Field>{resource.description}</Field>

      <div className="mt-2 flex items-center justify-between">
        <Button variant="ghost">Save changes</Button>
        <Link href={`/admin/resources/${resource.id}/slots/new`}>
          <Button variant="primaryAdmin">Generate slots →</Button>
        </Link>
      </div>
    </div>
  );
}
