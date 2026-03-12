package springkong.talki_spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import springkong.talki_spring.domain.Presentation;
import springkong.talki_spring.domain.User;
import springkong.talki_spring.repository.PresentationRepository;
import springkong.talki_spring.repository.UserRepository;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Presigner presigner;
    private final PresentationRepository presentationRepository;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket:}")
    private String bucket;

    // 업로드용 URL
    public Map<String, String> generateUploadUrl(String presentationId, String filename, Long userId, String presentationType) {

        String key = "recordings/" + UUID.randomUUID() + "-" + filename;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("video/mp4")
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(objectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignRequest);

        User user = null;

        if (userId!=null) {
            user = userRepository.findById(userId).orElse(null);
        }


        Presentation presentation = Presentation.builder()
                .id(presentationId)
                .s3Key(key)
                .s3Url("https://" + bucket + ".s3.amazonaws.com/" + key)
                .presentationType(presentationType)
                .status("UPLOADED")
                .user(user)
                .build();

        presentationRepository.save(presentation);

        return Map.of(
                "uploadUrl", presignedRequest.url().toString(),
                "key", key
        );
    }

    // 다운로드용 URL
    public String generateDownloadUrl(String key) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }

    public Map<String, String> generateProfileUploadUrl(String filename) {

        String key = "profiles/" + UUID.randomUUID() + "-" + filename;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(objectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignRequest);

        return Map.of(
                "uploadUrl", presignedRequest.url().toString(),
                "key", key
        );
    }


}
