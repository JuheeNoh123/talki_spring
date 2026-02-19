package springkong.talki_spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springkong.talki_spring.service.S3Service;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
public class VideoController {
    private final S3Service s3Service;

    // 업로드 URL 발급
    @PostMapping("/upload-url")
    public ResponseEntity<?> getUploadUrl(@RequestParam String filename) {
        return ResponseEntity.ok(
                s3Service.generateUploadUrl(filename)
        );
    }

    // 다운로드 URL 발급
    @GetMapping("/download-url")
    public ResponseEntity<?> getDownloadUrl(@RequestParam String key) {
        return ResponseEntity.ok(
                Map.of("downloadUrl", s3Service.generateDownloadUrl(key))
        );
    }

}
