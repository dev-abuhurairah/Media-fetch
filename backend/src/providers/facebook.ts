import { MediaAnalysisResult } from "./tiktok.js";

export async function parseFacebook(url: string): Promise<MediaAnalysisResult> {
  const shortId = Math.random().toString(36).substring(2, 9);
  return {
    id: `fb_${shortId}`,
    platform: "FACEBOOK",
    title: "Facebook Public Video Post",
    author: "Verified Page",
    authorAvatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120",
    thumbnail: "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800",
    durationSeconds: 110,
    mediaType: "VIDEO",
    availableFormats: [
      {
        id: "fb_hd",
        quality: "720p High Definition",
        resolution: "1280x720",
        mimeType: "video/mp4",
        fileExtension: "mp4",
        estimatedSizeBytes: 31_000_000,
        hasAudio: true,
        hasVideo: true,
        downloadUrl: url
      },
      {
        id: "fb_sd",
        quality: "360p Standard Quality",
        resolution: "640x360",
        mimeType: "video/mp4",
        fileExtension: "mp4",
        estimatedSizeBytes: 11_200_000,
        hasAudio: true,
        hasVideo: true,
        downloadUrl: url
      }
    ],
    estimatedSize: 31_000_000,
    sourceUrl: url
  };
}
