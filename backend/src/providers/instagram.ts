import { MediaAnalysisResult } from "./tiktok.js";

export async function parseInstagram(url: string): Promise<MediaAnalysisResult> {
  const isReel = url.includes("/reel/");
  const shortId = Math.random().toString(36).substring(2, 8);

  return {
    id: `ig_${shortId}`,
    platform: "INSTAGRAM",
    title: isReel ? "Instagram Reel by Creator" : "Instagram Post Snapshot",
    author: "creative_photographer",
    authorAvatar: "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=120",
    thumbnail: "https://images.unsplash.com/photo-1611262588024-d12430b98920?w=800",
    durationSeconds: isReel ? 28 : 0,
    mediaType: isReel ? "VIDEO" : "IMAGE",
    availableFormats: isReel
      ? [
          {
            id: "ig_hd",
            quality: "1080p High Definition",
            resolution: "1080x1920",
            mimeType: "video/mp4",
            fileExtension: "mp4",
            estimatedSizeBytes: 14_800_000,
            hasAudio: true,
            hasVideo: true,
            downloadUrl: url
          },
          {
            id: "ig_audio",
            quality: "Reel Audio (M4A)",
            resolution: "Audio Only",
            mimeType: "audio/mp4",
            fileExtension: "m4a",
            estimatedSizeBytes: 1_900_000,
            hasAudio: true,
            hasVideo: false,
            downloadUrl: url
          }
        ]
      : [
          {
            id: "ig_full_res",
            quality: "Original Image (High Res)",
            resolution: "1440x1440",
            mimeType: "image/jpeg",
            fileExtension: "jpg",
            estimatedSizeBytes: 2_600_000,
            hasAudio: false,
            hasVideo: false,
            downloadUrl: url
          }
        ],
    estimatedSize: isReel ? 14_800_000 : 2_600_000,
    sourceUrl: url
  };
}
