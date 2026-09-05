import { describe, it, expect, beforeAll, afterAll } from "vitest";
import { buildServer } from "../src/server.js";

describe("MediaFetch API Endpoints", () => {
  const app = buildServer();

  beforeAll(async () => {
    await app.ready();
  });

  afterAll(async () => {
    await app.close();
  });

  it("GET /api/v1/health should report healthy status", async () => {
    const response = await app.inject({
      method: "GET",
      url: "/api/v1/health"
    });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.status).toBe("healthy");
    expect(body.platforms.tiktok).toBe(true);
    expect(body.platforms.instagram).toBe(true);
    expect(body.platforms.youtube).toBe(true);
    expect(body.platforms.facebook).toBe(true);
  });

  it("POST /api/v1/analyze should analyze a public TikTok link", async () => {
    const response = await app.inject({
      method: "POST",
      url: "/api/v1/analyze",
      payload: {
        url: "https://www.tiktok.com/@creator/video/1234567890"
      }
    });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.success).toBe(true);
    expect(body.data.platform).toBe("TIKTOK");
    expect(body.data.availableFormats.length).toBeGreaterThan(0);
  });

  it("POST /api/v1/analyze should reject invalid or unsupported URLs", async () => {
    const response = await app.inject({
      method: "POST",
      url: "/api/v1/analyze",
      payload: {
        url: "https://unsupported-site.com/post/999"
      }
    });

    expect(response.statusCode).toBe(400);
    const body = JSON.parse(response.body);
    expect(body.success).toBe(false);
    expect(body.errorCode).toBe("UNSUPPORTED_URL");
  });
});
