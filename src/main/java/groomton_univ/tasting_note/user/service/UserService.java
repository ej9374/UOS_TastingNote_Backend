package groomton_univ.tasting_note.user.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.entity.UserPreferenceEntity;
import groomton_univ.tasting_note.entity.UserTagEntity;
import groomton_univ.tasting_note.s3.service.FileUploadService;
import groomton_univ.tasting_note.user.dto.PreferenceUpdateRequestDto;
import groomton_univ.tasting_note.user.dto.UserResponseDto;
import groomton_univ.tasting_note.user.dto.UserTagResponseDto;
import groomton_univ.tasting_note.user.dto.UserUpdateRequestDto;
import groomton_univ.tasting_note.user.repository.UserPreferenceRepository;
import groomton_univ.tasting_note.user.repository.UserRepository;
import groomton_univ.tasting_note.user.repository.UserTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserTagRepository userTagRepository;
    private final Storage storage;

    @Value("${gcp.storage.bucket.name}")
    private String bucketName;

    public UserResponseDto getMyInfo(UserEntity user) {
        List<UserPreferenceEntity> preferences = userPreferenceRepository.findByUser(user);
        List<UserResponseDto.PreferenceDto> preferenceDtos = UserResponseDto.preferences(preferences);

        log.info("현재 로그인된 사용자 정보 조회를 요청했습니다: KakaoId = {}", user.getKakaoId());

        return UserResponseDto.builder()
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .kakaoNickname(user.getKakaoNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .preferences(preferenceDtos)
                .build();
    }

    @Transactional
    public UserResponseDto updateUserPreferences(UserEntity user, PreferenceUpdateRequestDto requestDto) {
        // 기존의 사용자 취향 정보를 모두 삭제
        List<UserPreferenceEntity> existingPreferences = userPreferenceRepository.findByUser(user);
        userPreferenceRepository.deleteAll(existingPreferences);

        // 요청으로 받은 tagId 목록으로 새로운 취향 정보를 저장
        if (requestDto.getTagIds() != null && !requestDto.getTagIds().isEmpty()) {
            for (Long tagId : requestDto.getTagIds()) {
                UserTagEntity tag = userTagRepository.findById(tagId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그 ID입니다: " + tagId));

                UserPreferenceEntity newPreference = new UserPreferenceEntity();
                newPreference.setUser(user);
                newPreference.setUserTag(tag);
                newPreference.setAiRecommended(false); // 기본값 설정
                newPreference.setCreatedAt(LocalDateTime.now());

                userPreferenceRepository.save(newPreference);
            }
        }

        log.info("사용자 취향 정보 수정을 요청했습니다: KakaoId = {}", user.getKakaoId());

        // 변경된 정보를 포함한 전체 사용자 정보를 다시 조회하여 반환
        return getMyInfo(user);
    }

    @Transactional(readOnly = true)
    public List<UserTagResponseDto> getAllUserTags() {
        List<UserTagEntity> tags = userTagRepository.findAllByOrderByIdAsc();

        log.info("전체 태그 목록 조회를 요청했습니다.");

        return tags.stream()
                .map(UserTagResponseDto::of)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 닉네임, 프로필 이미지 수정 메소드 (새로 추가)
     */
    @Transactional
    public UserResponseDto updateMyInfo(UserEntity user, UserUpdateRequestDto requestDto, MultipartFile profileImage) {
        // 닉네임 업데이트 (요청이 있는 경우)
        if (requestDto != null && requestDto.getNickname() != null && !requestDto.getNickname().isEmpty()) {
            String newNickname = requestDto.getNickname();

            // 닉네임 중복 검사 로직 (조건부)
            // 변경하려는 닉네임이 자신의 카카오 닉네임과 다른 경우에만 중복 검사 수행
            if (!newNickname.equals(user.getKakaoNickname())) {
                Optional<UserEntity> userWithSameNickname = userRepository.findByNickname(newNickname);

                // 닉네임이 이미 존재하고, 그 닉네임의 주인이 현재 사용자가 아닐 경우
                if (userWithSameNickname.isPresent() && !userWithSameNickname.get().getKakaoId().equals(user.getKakaoId())) {
                    throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
                }
            }

            user.setNickname(requestDto.getNickname());
            log.info("사용자 닉네임 변경: KakaoId = {}, New Nickname = {}", user.getKakaoId(), requestDto.getNickname());
        }

        // 프로필 이미지 업데이트 (파일이 있는 경우)
        // GCS를 이용한 프로필 이미지 업로드 로직 시작
        if (profileImage != null && !profileImage.isEmpty()) {
            log.info("새로운 프로필 이미지 감지. GCS 업로드를 시작합니다.");

            // 기존 이미지 GCS에서 삭제
            String oldImageUrl = user.getProfileImageUrl();
            if (oldImageUrl != null && oldImageUrl.startsWith("https://storage.googleapis.com/")) {
                try {
                    String prefix = String.format("https://storage.googleapis.com/%s/", bucketName);
                    String encodedObjectName = oldImageUrl.substring(prefix.length());

                    // URL 인코딩된 파일명을 실제 경로로 디코딩
                    String objectName = URLDecoder.decode(encodedObjectName, StandardCharsets.UTF_8);

                    // storage.delete()의 반환값(boolean)으로 실제 삭제 성공 여부 확인
                    boolean deleted = storage.delete(BlobId.of(bucketName, objectName));

                    if (deleted) {
                        log.info("기존 프로필 이미지 GCS에서 삭제 성공: {}", objectName);
                    } else {
                        log.warn("기존 프로필 이미지 GCS에서 삭제 실패 (파일을 찾을 수 없음): {}", objectName);
                    }
                } catch (Exception e) {
                    log.warn("기존 프로필 이미지 GCS 삭제 중 예외 발생. URL: {}", oldImageUrl, e);
                }
            }

            // 새 이미지 GCS에 업로드
            String newFilename = "profile-images/" + UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, newFilename)
                    .setContentType(profileImage.getContentType())
                    .build();
            try {
                storage.create(blobInfo, profileImage.getBytes());
            } catch (IOException | StorageException e) {
                log.error("GCS 프로필 이미지 업로드 실패", e);
                throw new IllegalArgumentException("GCS 업로드 중 오류가 발생했습니다.");
            }

            // 업로드된 파일의 공개 URL 생성 및 UserEntity에 설정
            String publicUrl = String.format("https://storage.googleapis.com/%s/%s",
                    bucketName, URLEncoder.encode(newFilename, StandardCharsets.UTF_8));
            user.setProfileImageUrl(publicUrl);
            log.info("새로운 프로필 이미지 URL 설정: {}", publicUrl);
        }


        user.setUpdatedAt(LocalDateTime.now());

        // 변경된 내용을 명시적으로 저장
        userRepository.save(user);

        log.info("사용자 정보 수정을 완료했습니다: KakaoId = {}", user.getKakaoId());
        return getMyInfo(user);
    }
}