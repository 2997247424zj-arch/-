package com.example.musicshareplatformback.media;

import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.util.Locale;

public final class MediaTypeFactoryHelper {

    private MediaTypeFactoryHelper() {
    }

    public static MediaType resolve(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".mp3")) {
            return MediaType.valueOf("audio/mpeg");
        }
        if (fileName.endsWith(".wav")) {
            return MediaType.valueOf("audio/wav");
        }
        if (fileName.endsWith(".ogg")) {
            return MediaType.valueOf("audio/ogg");
        }
        if (fileName.endsWith(".m4a")) {
            return MediaType.valueOf("audio/mp4");
        }
        if (fileName.endsWith(".flac")) {
            return MediaType.valueOf("audio/flac");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
