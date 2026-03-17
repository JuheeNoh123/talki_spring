package springkong.talki_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "영상 업로드 URL 발급",
            description =  """
filename은 반드시 unique하게 생성해서 보내주세요.
같은 filename이 들어오면 기존 파일이 덮어써질 수 있습니다.

uuid 또는 timestamp를 붙여서 생성하는 것을 권장합니다.

예시
- profile_12345_1712345678.png
- uuid-3f9c1d2e.png
""")
    public ResponseEntity<?> getUploadUrl(@Parameter(

    )@RequestBody AnalyzeResultDTO.UploadUrlDTO dto) {

        return ResponseEntity.ok(
                s3Service.generateUploadUrl(dto.getPresentationId(), dto.getFilename(), dto.getUserId(), dto.getPresentationType(), dto.getTopic())
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
