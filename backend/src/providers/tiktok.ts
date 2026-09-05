export interface MediaFormatResponse {
  id: string;
  quality: string;
  resolution: string;
  mimeType: string;
  fileExtension: string;
  estimatedSizeBytes: number;
  hasAudio: boolean;
  hasVideo: boolean;
  downloadUrl: string;
}

export interface MediaAnalysisResult {
  id: string;
  platform: "TIKTOK" | "INSTAGRAM" | "YOUTUBE" | "FACEBOOK" | "UNKNOWN";
  title: string;
  author: string;
  authorAvatar?: string;
  thumbnail?: string;
  durationSeconds: number;
  mediaType: "VIDEO" | "IMAGE" | "CAROUSEL" | "AUDIO";
  availableFormats: MediaFormatResponse[];
  estimatedSize: number;
  sourceUrl: string;
}

export async function parseTikTok(url: string): Promise<MediaAnalysisResult> {
  const shortId = Math.random().toString(36).substring(2, 9);
  return {
    id: `tiktok_${shortId}`,
    platform: "TIKTOK",
    title: "Trending Creator Video",
    author: "@creator_official",
    authorAvatar: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
    thumbnail: "https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=800",
    durationSeconds: 38,
    mediaType: "VIDEO",
    availableFormats: [
      {
        id: "tt_1080p",
        quality: "1080p HD (Watermark-Free)",
        resolution: "1080x1920",
        mimeType: "video/mp4",
        fileExtension: "mp4",
        estimatedSizeBytes: 19_500_000,
        hasAudio: true,
        hasVideo: true,
        downloadUrl: url
      },
      {
        id: "tt_720p",
        quality: "720p Standard",
        resolution: "720x1280",
        mimeType: "video/mp4",
        fileExtension: "mp4",
        estimatedSizeBytes: 9_200_000,
        hasAudio: true,
        hasVideo: true,
        downloadUrl: url
      },
      {
        id: "tt_audio",
        quality: "Original Sound (MP3)",
        resolution: "Audio Only",
        mimeType: "audio/mpeg",
        fileExtension: "mp3",
        estimatedSizeBytes: 2_300_000,
        hasAudio: true,
        hasVideo: false,
        downloadUrl: url
      }
    ],
    estimatedSize: 19_500_000,
    sourceUrl: url
  };
}
