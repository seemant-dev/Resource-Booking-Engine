import http from "k6/http";
import { check, fail, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const PERF_EMAIL = __ENV.PERF_EMAIL || "user1@mailinator.com";
const PERF_PASSWORD = __ENV.PERF_PASSWORD || "SlotEnginePerf123!";
const RESOURCE_NAME = __ENV.RESOURCE_NAME || "resource1";
const SLOT_DATE = __ENV.SLOT_DATE || "2030-01-01";

const PAGE_SIZE = 50;
const EXPECTED_BOOKING_COUNT = 10000;

export const options = {
  stages: [
    { duration: "30s", target: 10 },
    { duration: "120s", target: 10 },
    { duration: "15s", target: 0 },
  ],
  thresholds: {
    checks: ["rate==1"],
    "http_req_failed{name:resources}": ["rate<0.01"],
    "http_req_failed{name:resource_slots}": ["rate<0.01"],
    "http_req_failed{name:my_bookings}": ["rate<0.01"],
    "http_req_duration{name:resources}": ["p(95)<500"],
    "http_req_duration{name:resource_slots}": ["p(95)<500"],
    "http_req_duration{name:my_bookings}": ["p(95)<500"],
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
    fail("Setup login failed; read journey test not started.");
  }

  return { accessToken };
}

export default function ({ accessToken }) {
  const resourcesResponse = http.get(`${BASE_URL}/api/resources`, {
    headers: { Accept: "application/json" },
    tags: { name: "resources" },
  });

  const resourcesSucceeded = check(resourcesResponse, {
    "resources status is 200": (response) => response.status === 200,
  });

  if (!resourcesSucceeded) {
    sleep(1);
    return;
  }

  const resources = resourcesResponse.json();
  const resource = resources.find((item) => item.name === RESOURCE_NAME);

  const resourceFound = check(resource, {
    "target resource exists": (item) => Boolean(item?.id),
  });

  if (!resourceFound) {
    sleep(1);
    return;
  }

  const slotsResponse = http.get(
    `${BASE_URL}/api/resources/${resource.id}/slots?date=${SLOT_DATE}`,
    {
      headers: { Accept: "application/json" },
      tags: { name: "resource_slots" },
    },
  );

  check(slotsResponse, {
    "slots status is 200": (response) => response.status === 200,
  });

  const bookingsResponse = http.get(`${BASE_URL}/api/bookings/me?page=0`, {
    headers: {
      Accept: "application/json",
      Cookie: `access_token=${accessToken}`,
    },
    tags: { name: "my_bookings" },
  });

  check(bookingsResponse, {
    "bookings status is 200": (response) => response.status === 200,
    "bookings page contains 50 records": (response) =>  // pagination check
      response.status === 200 && response.json("content").length === PAGE_SIZE,
    "bookings metadata matches seeded data": (response) =>
      response.status === 200 &&
      response.json("page") === 0 &&
      response.json("pageSize") === PAGE_SIZE &&
      response.json("totalElements") === EXPECTED_BOOKING_COUNT,
  });

  sleep(1);
}
