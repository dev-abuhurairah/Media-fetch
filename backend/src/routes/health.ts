import { FastifyInstance } from "fastify";

export async function healthRoutes(fastify: FastifyInstance) {
  fastify.get("/api/v1/health", async (request, reply) => {
    return {
      status: "healthy",
      version: "1.0.0",
      timestamp: new Date().toISOString(),
      platforms: {
        tiktok: true,
        instagram: true,
        youtube: true,
        facebook: true
      }
    };
  });
}
