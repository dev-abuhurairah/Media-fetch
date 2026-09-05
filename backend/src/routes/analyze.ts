import { FastifyInstance } from "fastify";
import { z } from "zod";
import { parseTikTok } from "../providers/tiktok.js";
import { parseInstagram } from "../providers/instagram.js";
import { parseYouTube } from "../providers/youtube.js";
import { parseFacebook } from "../providers/facebook.js";

const AnalyzeSchema = z.object({
  url: z.string().url(),
  clientVersion: z.string().optional()
});

export async function analyzeRoutes(fastify: FastifyInstance) {
  fastify.post("/api/v1/analyze", async (request, reply) => {
    const parseResult = AnalyzeSchema.safeParse(request.body);
    if (!parseResult.success) {
      return reply.status(400).send({
        success: false,
        error: "Invalid request payload. Please supply a valid URL.",
        errorCode: "INVALID_URL"
      });
    }

    const { url } = parseResult.data;
    const lower = url.toLowerCase();

    try {
      let result;
      if (lower.includes("tiktok.com")) {
        result = await parseTikTok(url);
      } else if (lower.includes("instagram.com") || lower.includes("instagr.am")) {
        result = await parseInstagram(url);
      } else if (lower.includes("youtube.com") || lower.includes("youtu.be")) {
        result = await parseYouTube(url);
      } else if (lower.includes("facebook.com") || lower.includes("fb.watch") || lower.includes("fb.com")) {
        result = await parseFacebook(url);
      } else {
        return reply.status(400).send({
          success: false,
          error: "This link isn't supported.",
          errorCode: "UNSUPPORTED_URL"
        });
      }

      return reply.send({
        success: true,
        data: result
      });
    } catch (err: any) {
      request.log.error(err);
      return reply.status(500).send({
        success: false,
        error: "The service is temporarily unavailable. Please try again later.",
        errorCode: "EXTRACTION_FAILED"
      });
    }
  });
}
