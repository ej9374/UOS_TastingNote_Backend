package groomton_univ.tasting_note.s3.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 기능을 위한 인터페이스입니다.
 * 이 인터페이스를 구현하는 클래스는 실제 파일 업로드 로직을 담당합니다.
 * (예: 로컬 저장, AWS S3 업로드 등)
 */
public interface FileUploadService {

    /**
     * 파일을 지정된 디렉토리에 업로드하고, 접근 가능한 URL을 반환합니다.
     *
     * @param file    업로드할 MultipartFile 객체
     * @param dirName 파일을 저장할 디렉토리 이름 (예: "profile-images")
     * @return 업로드된 파일에 접근할 수 있는 고유 URL
     */
    String upload(MultipartFile file, String dirName);
}