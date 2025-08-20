package groomton_univ.tasting_note.s3.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@Profile("!s3") // ⭐️ "s3" 프로필이 활성화되지 않았을 때 이 Bean을 사용
public class MockFileUploadService implements FileUploadService {

    @Override
    public String upload(MultipartFile file, String dirName) {
        log.info("--- MockFileUploadService: 실제 파일 업로드가 비활성화되었습니다 ---");
        log.info("파일명: {}, 디렉토리: {}", file.getOriginalFilename(), dirName);
        // 실제 업로드 로직 없이 null을 반환합니다.
        return null;
    }
}