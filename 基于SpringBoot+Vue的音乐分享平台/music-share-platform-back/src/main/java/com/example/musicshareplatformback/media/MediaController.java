package com.example.musicshareplatformback.media;

import com.example.musicshareplatformback.media.dto.UploadAudioResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/media/audio")
public class MediaController {

    private final MediaPathService mediaPathService;

    public MediaController(MediaPathService mediaPathService) {
        this.mediaPathService = mediaPathService;
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<?> streamAudio(
            @PathVariable String fileName,
            @RequestHeader HttpHeaders headers
    ) throws IOException {
        Path audioPath = mediaPathService.resolveAudioFile(fileName);
        if (!Files.exists(audioPath) || !Files.isReadable(audioPath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(audioPath);
        MediaType mediaType = MediaTypeFactoryHelper.resolve(audioPath);
        long contentLength = resource.contentLength();

        List<HttpRange> ranges = headers.getRange();
        if (ranges == null || ranges.isEmpty()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentType(mediaType)
                    .contentLength(contentLength)
                    .body(resource);
        }

        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);
        long rangeLength = Math.min(1024 * 1024, end - start + 1);
        ResourceRegion region = new ResourceRegion(resource, start, rangeLength);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentType(mediaType)
                .contentLength(region.getCount())
                .body(region);
    }

    @GetMapping("/{fileName:.+}/download")
    public ResponseEntity<Resource> downloadAudio(@PathVariable String fileName) throws IOException {
        Path audioPath = mediaPathService.resolveAudioFile(fileName);
        if (!Files.exists(audioPath) || !Files.isReadable(audioPath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(audioPath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaTypeFactoryHelper.resolve(audioPath))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadAudioResponse> uploadAudio(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Path storedPath = mediaPathService.storeAudioFile(file.getOriginalFilename(), file.getInputStream());
        String fileName = storedPath.getFileName().toString();
        return ResponseEntity.ok(new UploadAudioResponse(
                mediaPathService.toStoredAudioUrl(fileName),
                mediaPathService.toStreamUrl(fileName),
                mediaPathService.toDownloadUrl(fileName),
                fileName,
                "LOCAL"
        ));
    }
}
