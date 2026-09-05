# Google Play Store Release Checklist for MediaFetch

Before publishing MediaFetch to Google Play:

## 1. Permissions Justification
- `FOREGROUND_SERVICE_DATA_SYNC`:
  - Required for persistent, user-initiated file downloads when the user switches apps.
  - Video justification link must show user initiating a download and the progress notification appearing.
- `POST_NOTIFICATIONS`:
  - Explicit runtime permission prompt shown on first download on Android 13+.
- `READ_MEDIA_VIDEO`, `READ_MEDIA_IMAGES`, `READ_MEDIA_AUDIO`:
  - Used strictly to read and manage downloaded media in the in-app Media Library.

## 2. Store Assets
- **App Icon**: 512x512 PNG (32-bit color, no transparency).
- **Feature Graphic**: 1024x500 JPEG/PNG.
- **Phone Screenshots**: Minimum 4 high-resolution screenshots:
  1. Home Screen (with Smart Clipboard banner)
  2. Analysis Bottom Sheet (with format/quality picker)
  3. Active Downloads (live speed, progress, ETA)
  4. Media Library (grid view of videos and photos)

## 3. Privacy Policy URL
Host the privacy declaration text publicly (e.g. on GitHub Pages or custom domain) and configure the URL in Google Play Console.
