package com.example.musicshareplatformback.media;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class MediaPathService {

    private final Path audioRoot;

    public MediaPathService(MediaProperties mediaProperties) {
        this.audioRoot = Paths.get(mediaProperties.getAudioRoot()).toAbsolutePath().normalize();
    }

    public Path resolveAudioFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("音频文件名不能为空。");
        }

        Path resolvedPath = audioRoot.resolve(fileName).normalize();
        if (!resolvedPath.startsWith(audioRoot)) {
            throw new IllegalArgumentException("非法的音频文件路径。");
        }

        return resolvedPath;
    }

    public boolean audioFileExists(String fileName) {
        return resolveAudioFile(fileName).toFile().isFile();
    }

    public String toStreamUrl(String fileName) {
        return "/api/media/audio/" + fileName;
    }

    public String toDownloadUrl(String fileName) {
        return "/api/media/audio/" + fileName + "/download";
    }

    public String toStoredAudioUrl(String fileName) {
        return "/media/audio/" + fileName;
    }

    public Path storeAudioFile(String originalFileName, InputStream inputStream) throws IOException {
        Files.createDirectories(audioRoot);
        String sanitizedName = sanitizeFileName(originalFileName);
        Path targetPath = audioRoot.resolve(sanitizedName).normalize();
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }

    private String sanitizeFileName(String originalFileName) {
        String fileName = originalFileName == null ? "audio-file" : originalFileName.trim();
        fileName = fileName.replace("\\", "_").replace("/", "_").replace("..", "_");
        if (fileName.isBlank()) {
            fileName = "audio-file";
        }
        return System.currentTimeMillis() + "-" + fileName;
    }
}
