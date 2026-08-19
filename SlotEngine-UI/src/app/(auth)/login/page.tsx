"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import type { FormEvent } from "react";
import { useState } from "react";
import { login } from "@/api/auth";
import { ApiError } from "@/api/client";
import { Button, Field } from "@/components/ui";
import type { UserRole } from "@/types";

type LoginForm = {
  email: string;
  password: string;
};

const initialForm: LoginForm = {
  email: "",
  password: "",
};

export default function LoginPage() {
  const router = useRouter();

  const [form, setForm] = useState<LoginForm>(initialForm);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setError(null);
    setSubmitting(true);

    try {
      const response = await login({
        email: form.email.trim(),
        password: form.password,
      });

      router.replace(getRoleHome(response.role));
    } catch (caughtError) {
      setError(getErrorMessage(caughtError, "Unable to log in. Please try again."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="text-ink text-center text-lg font-bold">Welcome back</h1>
      <p className="text-ink-3 mb-6 text-center text-sm">Log in to manage your bookings</p>

      <form className="flex flex-col" onSubmit={handleSubmit}>
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
            autoComplete="current-password"
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
          {submitting ? "Logging in..." : "Log in"}
        </Button>
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

function getRoleHome(role: UserRole): string {
  return role === "ADMIN" ? "/admin" : "/";
}

function getErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError) {
    return error.message;
  }

  return fallback;
}