import Link from "next/link";
import { ShieldAlert } from "lucide-react";
import { EmptyState, Button } from "@/components/ui";

/**
 * State C — Not authorized (403). A USER hitting an admin URL directly
 * should get a real, honest error state rather than a silent redirect —
 * the @PreAuthorize boundary on the backend is the actual security; this
 * page is just truthful UI about it. Kept as its own route (rather than
 * only a modal/inline state) since a person can land here directly from
 * a bookmarked or typed URL.
 */
export default function ForbiddenPage() {
  return (
    <div className="bg-bg flex min-h-screen items-center justify-center px-4">
      <div className="border-line w-full max-w-sm rounded-xl border bg-white p-8">
        <EmptyState
          tone="danger"
          icon={<ShieldAlert className="h-5 w-5" />}
          title="You don't have access to this page"
          description="This area is for admins only. If you think that's wrong, contact your workspace admin."
          action={
            <Link href="/">
              <Button variant="ghost">Back to home</Button>
            </Link>
          }
        />
      </div>
    </div>
  );
}
