import http from "k6/http";
import { check, fail } from "k6";
import { Counter, Trend } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const PERF_EMAIL = __ENV.PERF_EMAIL || "user1@mailinator.com";
const PERF_PASSWORD = __ENV.PERF_PASSWORD || "SlotEnginePerf123!";
const SLOT_ID = 50005;
const EXPECTED_BOOKING_RESPONSES = http.expectedStatuses(201, 409);

const created = new Counter("booking_created");
const conflicts = new Counter("booking_conflicts");
const unexpected = new Counter("booking_unexpected");
const createdDuration = new Trend("booking_created_duration");
const conflictDuration = new Trend("booking_conflict_duration");

export const options = {
  scenarios: {
    same_slot: {
      executor: "per-vu-iterations",
      vus: 20,
      iterations: 1,
      maxDuration: "1m",
    },
  },
  thresholds: {
    checks: ["rate==1"],
    iterations: ["count==20"],
    booking_created: ["count==1"],
    booking_conflicts: ["count==19"],
    booking_unexpected: ["count==0"],
    "http_req_failed{name:booking_attempt}": ["rate==0"],
  },
  summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
  const loginResponse = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: PERF_EMAIL, password: PERF_PASSWORD }),
    {
      headers: { "Content-Type": "application/json" },
      tags: { name: "setup_login" },
    },
  );

  const accessToken = loginResponse.cookies.access_token?.[0]?.value;
  const loginSucceeded = check(loginResponse, {
    "login status is 200": (response) => response.status === 200,
    "login sets access token": () => Boolean(accessToken),
  });

  if (!loginSucceeded) {
    fail("Setup login failed; booking attempts were not started.");
  }

  return { accessToken };
}

export default function ({ accessToken }) {
  const response = http.post(`${BASE_URL}/api/slots/${SLOT_ID}/book`, null, {
    headers: {
      Accept: "application/json",
      Cookie: `access_token=${accessToken}`,
    },
    tags: { name: "booking_attempt" },
    responseCallback: EXPECTED_BOOKING_RESPONSES,
  });

  created.add(response.status === 201 ? 1 : 0);
  conflicts.add(response.status === 409 ? 1 : 0);
  unexpected.add(response.status !== 201 && response.status !== 409 ? 1 : 0);

  if (response.status === 201) {
    createdDuration.add(response.timings.duration);
  } else if (response.status === 409) {
    conflictDuration.add(response.timings.duration);
  }

  check(response, {
    "booking response is 201 or 409": (result) =>
      result.status === 201 || result.status === 409,
  });
}
