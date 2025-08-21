package groomton_univ.tasting_note.s3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
// import com.amazonaws.services.s3.AmazonS3Client; // 실제 구현 시 필요

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("s3") // ⭐️ "s3" 프로필이 활성화되었을 때만 이 Bean을 사용
public class S3FileUploadService implements FileUploadService {

    // private final AmazonS3Client amazonS3Client;

    // @Value("${cloud.aws.s3.bucket}")
    // private String bucket;

    @Override
    public String upload(MultipartFile file, String dirName) {
        log.info("--- S3FileUploadService: S3에 실제 파일 업로드를 시작합니다 ---");
        // TODO: 여기에 실제 AWS S3에 파일을 업로드하고 URL을 반환하는 로직을 구현합니다.
        // String uploadedFileUrl = ...;
        // return uploadedFileUrl;

        // 지금은 임시 URL을 반환합니다.
        return "https://s3.example.com/" + dirName + "/" + file.getOriginalFilename();
    }
}