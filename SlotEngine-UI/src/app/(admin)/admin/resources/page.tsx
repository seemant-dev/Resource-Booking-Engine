import { SetBreadcrumb } from "@/components/layout";
import { AdminResourcesTable } from "@/components/admin";
import { Button } from "@/components/ui";
import { mockResources } from "@/data";

/**
 * Screen 09 — admin Resources list. Backed conceptually by `GET /resources`.
 * "+ New resource" is a static action here (no create-resource modal was
 * drawn separately in the approved design — creating and editing share
 * the same form, reached via the resource detail screen).
 */
export default function AdminResourcesPage() {
  return (
    <div className="max-w-4xl">
      <SetBreadcrumb>
        <b className="text-ink">Resources</b>
      </SetBreadcrumb>

      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-ink text-xl font-bold">Resources</h1>
        <Button variant="primaryAdmin">+ New resource</Button>
      </div>

      <AdminResourcesTable resources={mockResources} />
    </div>
  );
}
