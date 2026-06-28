import Link from "next/link";
import { FileQuestion } from "lucide-react";
import { Button, EmptyState } from "@/components/ui";

/**
 * App-wide 404, shown when notFound() is called (e.g. an invalid
 * resourceId) or a URL doesn't match any route. Reuses the same
 * EmptyState pattern as the other empty/error states in the approved
 * design rather than introducing a new visual treatment.
 */
export default function NotFound() {
  return (
    <div className="bg-bg flex min-h-screen items-center justify-center px-4">
      <div className="border-line w-full max-w-sm rounded-xl border bg-white p-8">
        <EmptyState
          icon={<FileQuestion className="h-5 w-5" />}
          title="Page not found"
          description="The page you're looking for doesn't exist or may have been moved."
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
