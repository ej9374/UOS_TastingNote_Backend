package groomton_univ.tasting_note.user.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserTagRepository userTagRepository;
    private final FileUploadService fileUploadService;

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
            user.setNickname(requestDto.getNickname());
            log.info("사용자 닉네임 변경: KakaoId = {}, New Nickname = {}", user.getKakaoId(), requestDto.getNickname());
        }

        // 프로필 이미지 업데이트 (파일이 있는 경우)
        if (profileImage != null && !profileImage.isEmpty()) {
            String newImageUrl = fileUploadService.upload(profileImage, "profile-images");
            user.setProfileImageUrl(newImageUrl);
            log.info("사용자 프로필 이미지 변경: KakaoId = {}, New Image URL = {}", user.getKakaoId(), newImageUrl);
        }

        user.setUpdatedAt(LocalDateTime.now());

        // 3. 변경된 내용을 명시적으로 저장
        userRepository.save(user);

        log.info("사용자 정보 수정을 완료했습니다: KakaoId = {}", user.getKakaoId());
        return getMyInfo(user);
    }
}