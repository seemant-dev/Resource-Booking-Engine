import { SetBreadcrumb } from "@/components/layout";
import { Button, Field, FieldLabel } from "@/components/ui";
import { mockCurrentUser } from "@/data";

/**
 * Screen 07 — Account settings. Deliberately minimal: name, email, and a
 * change-password action only — the data already collected at
 * registration, nothing invented beyond it per the approved design.
 */
export default function SettingsPage() {
  return (
    <div className="max-w-sm">
      <SetBreadcrumb>
        <b className="text-ink">Settings</b>
      </SetBreadcrumb>

      <h1 className="text-ink text-xl font-bold">Account</h1>
      <p className="text-ink-3 mb-5 text-sm">Your details</p>

      <FieldLabel>Full name</FieldLabel>
      <Field>{mockCurrentUser.fullName}</Field>

      <FieldLabel>Email</FieldLabel>
      <Field>{mockCurrentUser.email}</Field>

      <Button variant="ghost" className="mt-1">
        Change password
      </Button>
    </div>
  );
}
