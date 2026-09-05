import { MediaAnalysisResult } from "./tiktok.js";

export async function parseYouTube(url: string): Promise<MediaAnalysisResult> {
  const isShorts = url.includes("/shorts/");
  const videoId = Math.random().toString(36).substring(2, 9);

  return {
    id: `yt_${videoId}`,
    platform: "YOUTUBE",
    title: isShorts ? "YouTube Short - Creative Feature" : "High Quality Video Showcase",
    author: "Official Channel",
    authorAvatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120",
    thumbnail: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
    durationSeconds: isShorts ? 55 : 210,
    mediaType: "VIDEO",
    availableFormats: [
      {
        id: "yt_1080p",
        quality: "1080p Full HD",
        resolution: isShorts ? "1080x1920" : "1920x1080",
        mimeType: "video/mp4",
        fileExtension: "mp4",
        estimatedSizeBytes: 44_000_000,
        hasAudio: true,
        hasVideo: true,
        downloadUrl: url
      },
      {
        id: "yt_720p",
        quality: "720p HD",
        resolution: isShorts ? "720x1280" : "1280x720",
        mimeType: "video/mp4",
        fileExtension: "mp4",
        estimatedSizeBytes: 22_500_000,
        hasAudio: true,
        hasVideo: true,
        downloadUrl: url
      },
      {
        id: "yt_audio",
        quality: "HQ Audio (320kbps MP3)",
        resolution: "Audio Only",
        mimeType: "audio/mpeg",
        fileExtension: "mp3",
        estimatedSizeBytes: 4_500_000,
        hasAudio: true,
        hasVideo: false,
        downloadUrl: url
      }
    ],
    estimatedSize: 44_000_000,
    sourceUrl: url
  };
}
