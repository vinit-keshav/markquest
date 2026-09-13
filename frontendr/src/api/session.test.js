import { test } from "node:test";
import assert from "node:assert/strict";
import axios from "axios";
import { attachSession } from "./session.js";

test("public authentication ignores stale tokens while protected requests keep them", async () => {
  const originalStorage = Object.getOwnPropertyDescriptor(globalThis, "localStorage");
  Object.defineProperty(globalThis, "localStorage", {
    configurable: true,
    value: { getItem: () => "expired-token" },
  });
  try {
    const client = attachSession(axios.create({
      adapter: async (config) => ({ config, data: null, status: 200, statusText: "OK", headers: {} }),
    }));
    for (const endpoint of ["login", "signup", "verify-otp", "resend-otp"]) {
      const response = await client.post(`/auth/${endpoint}`, {}, {
        skipSession: true,
        headers: { Authorization: "Bearer old-default" },
      });
      assert.equal(response.config.headers.get("Authorization"), undefined);
    }
    const response = await client.post("/auth/reset-password", {});
    assert.equal(response.config.headers.get("Authorization"), "Bearer expired-token");
  } finally {
    if (originalStorage) Object.defineProperty(globalThis, "localStorage", originalStorage);
    else delete globalThis.localStorage;
  }
});
