# MediaFetch Legal & Platform Compliance Guidelines

MediaFetch is engineered to adhere to modern fair use doctrines, user authorization standards, and platform terms of service.

## Core Compliance Principles

1. **Public Content Only**:
   - The application only extracts media that is publicly accessible without authentication or paywalls.
   - Private Instagram accounts, locked TikTok profiles, members-only YouTube streams, and closed Facebook groups are strictly not supported.

2. **No DRM Circumvention**:
   - The application does not contain decryption keys, Widevine DRM bypasses, or copy-protection circumvention algorithms.
   - If an encountered stream is DRM-protected, the application returns `DataError.Media.DRM_PROTECTED`.

3. **No Credential Harvesting**:
   - MediaFetch does not ask users for social media account passwords or hijack session cookies.

4. **Rate Limiting & Server Respect**:
   - The backend API gateway enforces rate limits (100 requests per minute per IP) to avoid creating undue load on host platforms.

5. **User Responsibility**:
   - End-users are responsible for adhering to local copyright statutes and the license under which the original creator published the media.
