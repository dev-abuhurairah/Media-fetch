import Fastify from "fastify";
import cors from "@fastify/cors";
import helmet from "@fastify/helmet";
import rateLimit from "@fastify/rate-limit";
import swagger from "@fastify/swagger";
import swaggerUi from "@fastify/swagger-ui";
import { healthRoutes } from "./routes/health.js";
import { analyzeRoutes } from "./routes/analyze.js";
import { downloadRoutes } from "./routes/download.js";

export function buildServer() {
  const server = Fastify({
    logger: {
      level: process.env.LOG_LEVEL || "info"
    }
  });

  // Security Headers
  server.register(helmet, {
    contentSecurityPolicy: false // Allows Swagger UI
  });

  // Cross-Origin Resource Sharing
  server.register(cors, {
    origin: true
  });

  // Anti-DDoS / Rate limiting (100 req per minute per IP)
  server.register(rateLimit, {
    max: 100,
    timeWindow: "1 minute"
  });

  // OpenAPI Documentation
  server.register(swagger, {
    openapi: {
      info: {
        title: "MediaFetch Universal Media API",
        description: "Production API gateway for extracting public and user-authorized social media streams",
        version: "1.0.0"
      },
      servers: [
        { url: "http://localhost:8080", description: "Local Development Server" }
      ]
    }
  });

  server.register(swaggerUi, {
    routePrefix: "/docs"
  });

  // Register feature routes
  server.register(healthRoutes);
  server.register(analyzeRoutes);
  server.register(downloadRoutes);

  return server;
}

const server = buildServer();

const PORT = parseInt(process.env.PORT || "8080", 10);
const HOST = process.env.HOST || "0.0.0.0";

if (process.env.NODE_ENV !== "test") {
  server.listen({ port: PORT, host: HOST }, (err, address) => {
    if (err) {
      server.log.error(err);
      process.exit(1);
    }
    server.log.info(`MediaFetch Gateway running at ${address}`);
    server.log.info(`OpenAPI docs available at ${address}/docs`);
  });
}
