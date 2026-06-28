import Link from "next/link";
import { Button, Field } from "@/components/ui";

/**
 * Screen 01 — Log in. Static form only: no submit handler, no validation,
 * no auth call. Per the approved design, this routes conceptually to
 * either the user Home or Admin Home depending on role, but since there
 * is no backend yet, the link below simply points at the user Home.
 */
export default function LoginPage() {
  return (
    <div>
      <h1 className="text-ink text-center text-lg font-bold">Welcome back</h1>
      <p className="text-ink-3 mb-6 text-center text-sm">Log in to manage your bookings</p>

      <form className="flex flex-col">
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
            Log in
          </Button>
        </Link>
      </form>

      <p className="text-ink-3 mt-5 text-center text-sm">
        Don&apos;t have an account?{" "}
        <Link href="/register" className="text-user font-medium">
          Register
        </Link>
      </p>
    </div>
  );
}
