"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import type { FormEvent } from "react";
import { useState } from "react";
import { register } from "@/api/auth";
import { ApiError } from "@/api/client";
import { Button, Field } from "@/components/ui";

type RegisterForm = {
  name: string;
  email: string;
  password: string;
};

const initialForm: RegisterForm = {
  name: "",
  email: "",
  password: "",
};

export default function RegisterPage() {
  const router = useRouter();

  const [form, setForm] = useState<RegisterForm>(initialForm);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setError(null);
    setSubmitting(true);

    try {
      await register({
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password,
      });

      router.replace("/login");
    } catch (caughtError) {
      setError(getErrorMessage(caughtError, "Unable to create account. Please try again."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="text-ink text-lg font-bold">Create your account</h1>
      <p className="text-ink-3 mb-6 text-sm">Book resources in seconds</p>

      <form className="flex flex-col" onSubmit={handleSubmit}>
        <Field>
          <input
            type="text"
            value={form.name}
            onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
            placeholder="Full name"
            autoComplete="name"
            className="text-ink placeholder:text-ink-3 w-full bg-transparent text-sm outline-none"
            required
          />
        </Field>

        <Field>
          <input
            type="email"
            value={form.email}
            onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
            placeholder="name@company.com"
            autoComplete="email"
            className="text-ink placeholder:text-ink-3 w-full bg-transparent text-sm outline-none"
            required
          />
        </Field>

        <Field>
          <input
            type="password"
            value={form.password}
            onChange={(event) =>
              setForm((current) => ({ ...current, password: event.target.value }))
            }
            placeholder="Password"
            autoComplete="new-password"
            minLength={8}
            className="text-ink placeholder:text-ink-3 w-full bg-transparent text-sm outline-none"
            required
          />
        </Field>

        {error && (
          <div className="bg-danger-bg text-danger mb-3 rounded-md px-3 py-2 text-sm" role="alert">
            {error}
          </div>
        )}

        <Button type="submit" className="mt-1 w-full justify-center" disabled={submitting}>
          {submitting ? "Creating account..." : "Create account"}
        </Button>
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

function getErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError) {
    return error.message;
  }

  return fallback;
}