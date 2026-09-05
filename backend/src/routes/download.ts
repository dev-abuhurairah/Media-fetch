import { FastifyInstance } from "fastify";
import { z } from "zod";

const DownloadRequestSchema = z.object({
  url: z.string().url(),
  formatId: z.string().min(1)
});

export async function downloadRoutes(fastify: FastifyInstance) {
  fastify.post("/api/v1/download", async (request, reply) => {
    const parseResult = DownloadRequestSchema.safeParse(request.body);
    if (!parseResult.success) {
      return reply.status(400).send({
        success: false,
        error: "Missing required download parameters."
      });
    }

    const { url, formatId } = parseResult.data;

    return reply.send({
      success: true,
      downloadUrl: url,
      formatId,
      headers: {
        "User-Agent": "MediaFetch-Streaming-Client/1.0",
        "Accept": "*/*"
      }
    });
  });

  fastify.get("/api/v1/download/:id", async (request, reply) => {
    const { id } = request.params as { id: string };
    return reply.send({
      id,
      status: "READY",
      timestamp: new Date().toISOString()
    });
  });

  fastify.delete("/api/v1/download/:id", async (request, reply) => {
    const { id } = request.params as { id: string };
    return reply.send({
      id,
      deleted: true,
      message: "Download session cleared."
    });
  });
}
