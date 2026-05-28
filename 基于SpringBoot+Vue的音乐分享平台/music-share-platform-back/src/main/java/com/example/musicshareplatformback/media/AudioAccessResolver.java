package com.example.musicshareplatformback.media;

import org.springframework.stereotype.Service;

@Service
public class AudioAccessResolver {

    private final MediaPathService mediaPathService;

    public AudioAccessResolver(MediaPathService mediaPathService) {
        this.mediaPathService = mediaPathService;
    }

    public AudioAccessInfo resolve(String audioUrl) {
        if (audioUrl == null || audioUrl.isBlank()) {
            return new AudioAccessInfo(null, null, null, false, "NONE");
        }

        String normalizedUrl = audioUrl.trim();
        if (isRemoteUrl(normalizedUrl)) {
            return new AudioAccessInfo(normalizedUrl, normalizedUrl, normalizedUrl, true, "REMOTE");
        }

        String fileName = extractFileName(normalizedUrl);
        boolean available = mediaPathService.audioFileExists(fileName);
        return new AudioAccessInfo(
                normalizedUrl,
                mediaPathService.toStreamUrl(fileName),
                mediaPathService.toDownloadUrl(fileName),
                available,
                "LOCAL"
        );
    }

    private boolean isRemoteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String extractFileName(String audioUrl) {
        int lastSlashIndex = audioUrl.lastIndexOf('/');
        return lastSlashIndex >= 0 ? audioUrl.substring(lastSlashIndex + 1) : audioUrl;
    }
}
