import { SetBreadcrumb } from "@/components/layout";
import { Button, Field, FieldLabel } from "@/components/ui";
import { mockCurrentAdmin } from "@/data";

/**
 * Admin Settings. The approved design's admin sidebar includes a
 * Settings link using the same pattern as the user shell, without
 * drawing a distinct frame for it — this page reuses that same minimal
 * name/email/password layout, scoped to the admin account.
 */
export default function AdminSettingsPage() {
  return (
    <div className="max-w-sm">
      <SetBreadcrumb>
        <b className="text-ink">Settings</b>
      </SetBreadcrumb>

      <h1 className="text-ink text-xl font-bold">Account</h1>
      <p className="text-ink-3 mb-5 text-sm">Your details</p>

      <FieldLabel>Full name</FieldLabel>
      <Field>{mockCurrentAdmin.fullName}</Field>

      <FieldLabel>Email</FieldLabel>
      <Field>{mockCurrentAdmin.email}</Field>

      <Button variant="ghost" className="mt-1">
        Change password
      </Button>
    </div>
  );
}
