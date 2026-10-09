import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const RESOURCE_ID = __ENV.RESOURCE_ID || "66";
const SLOT_DATE = __ENV.SLOT_DATE || "2026-10-08";

export const options = {
  stages: [
    { duration: "15s", target: 5 },
    { duration: "60s", target: 5 },
    { duration: "10s", target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<500"],
  },
};

export default function () {
  const resourcesResponse = http.get(`${BASE_URL}/api/resources`);

  check(resourcesResponse, {
    "resources status is 200": (response) => response.status === 200,
  });

  const slotsResponse = http.get(
    `${BASE_URL}/api/resources/${RESOURCE_ID}/slots?date=${SLOT_DATE}`,
  );

  check(slotsResponse, {
    "slots status is 200": (response) => response.status === 200,
  });

  sleep(1);
}