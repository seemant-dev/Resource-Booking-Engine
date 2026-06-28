import Link from "next/link";
import { Button, Field } from "@/components/ui";

/**
 * Screen 02 — Register. Static form only, same constraints as Login.
 * All self-registrations default to USER per the approved design; ADMIN
 * accounts are seeded, not self-service, so there is no role picker here.
 */
export default function RegisterPage() {
  return (
    <div>
      <h1 className="text-ink text-lg font-bold">Create your account</h1>
      <p className="text-ink-3 mb-6 text-sm">Book resources in seconds</p>

      <form className="flex flex-col">
        <Field>
          <input
            type="text"
            placeholder="Full name"
            className="text-ink placeholder:text-ink-3 w-full bg-transparent text-sm outline-none"
            disabled
          />
        </Field>
        <Field>
          <input
            type="email"
            placeholder="name@company.com"
            className="text-ink placeholder:text-ink-3 w-full bg-transparent text-sm outline-none"
            disabled
          />
        </Field>
        <Field>
          <input
            type="password"
            placeholder="Password"
            className="text-ink placeholder:text-ink-3 w-full bg-transparent text-sm outline-none"
            disabled
          />
        </Field>

        <Link href="/" className="mt-1">
          <Button type="button" className="w-full justify-center">
            Create account
          </Button>
        </Link>
      </form>

      <p className="text-ink-3 mt-5 text-center text-sm">
        Already have an account?{" "}
        <Link href="/login" className="text-user font-medium">
          Log in
        </Link>
      </p>
    </div>
  );
}
