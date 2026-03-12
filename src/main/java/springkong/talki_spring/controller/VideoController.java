package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springkong.talki_spring.dto.request.AnalyzeResultDTO;
import springkong.talki_spring.repository.UserRepository;
import springkong.talki_spring.service.S3Service;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/videos")
@Tag(name = "Video", description = "영상 업로드 및 다운로드 API")
public class VideoController {
    private final S3Service s3Service;
    private final UserRepository userRepository;
    // 업로드 URL 발급
    @PostMapping("/upload-url")
    @Operation(summary = "영상 업로드 URL 발급")
    public ResponseEntity<?> getUploadUrl(@RequestBody AnalyzeResultDTO.UploadUrlDTO dto) {

        return ResponseEntity.ok(
                s3Service.generateUploadUrl(dto.getPresentationId(), dto.getFilename(), dto.getUserId(), dto.getPresentationType())
        );
    }

    // 다운로드 URL 발급
    @Operation(summary = "영상 다운로드 URL 발급")
    @GetMapping("/download-url")
    public ResponseEntity<?> getDownloadUrl(@RequestParam String key) {
        return ResponseEntity.ok(
                Map.of("downloadUrl", s3Service.generateDownloadUrl(key))
        );
    }

}
