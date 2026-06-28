import { SetBreadcrumb } from "@/components/layout";
import { ResourceCard } from "@/components/resources";
import { mockResources } from "@/data";

/**
 * Screen 04 — Resources. Resolves the resource-discovery question from
 * the approved design review: every resource is a card here, one click
 * opens that resource's own slot calendar. Backed conceptually by
 * `GET /resources`.
 */
export default function ResourcesPage() {
  return (
    <div className="max-w-4xl">
      <SetBreadcrumb>
        <b className="text-ink">Resources</b>
      </SetBreadcrumb>

      <h1 className="text-ink text-xl font-bold">Resources</h1>
      <p className="text-ink-3 mb-5 text-sm">Pick a resource to see its availability</p>

      <div className="grid grid-cols-3 gap-4">
        {mockResources.map((resource) => (
          <ResourceCard key={resource.id} resource={resource} />
        ))}
      </div>
    </div>
  );
}
