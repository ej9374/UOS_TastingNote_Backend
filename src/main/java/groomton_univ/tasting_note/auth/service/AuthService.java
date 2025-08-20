package groomton_univ.tasting_note.auth.service;

import groomton_univ.tasting_note.auth.JwtProvider;
import groomton_univ.tasting_note.auth.dto.RegisterRequestDTO;
import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.entity.UserPreferenceEntity;
import groomton_univ.tasting_note.entity.UserTagEntity;
import groomton_univ.tasting_note.s3.service.FileUploadService;
import groomton_univ.tasting_note.user.repository.UserPreferenceRepository;
import groomton_univ.tasting_note.user.repository.UserRepository;
import groomton_univ.tasting_note.user.repository.UserTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final JwtProvider jwtProvider;
    private final FileUploadService fileUploadService;

    @Transactional
    public String register(RegisterRequestDTO registerRequestDTO, MultipartFile profileImage) {

        String finalProfileImageUrl;

        if (profileImage != null && !profileImage.isEmpty()) {
            // Case 1: 사용자가 새 이미지를 업로드한 경우
            log.info("새로운 프로필 이미지 업로드 감지. S3 업로드를 시도합니다.");
            finalProfileImageUrl = fileUploadService.upload(profileImage, "profile-images");
        } else {
            // Case 2: 사용자가 이미지를 업로드하지 않은 경우, DTO에 담겨온 카카오 기본 URL 사용
            log.info("새로운 프로필 이미지가 없습니다. 카카오 기본 프로필 URL을 사용합니다.");
            finalProfileImageUrl = registerRequestDTO.getProfileImageUrl();
        }

        // 혹시 모를 중복 가입 방지
        if (userRepository.findByKakaoId(registerRequestDTO.getKakaoId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 유저입니다.");
        }

        // 1. UserEntity 생성 및 저장
        UserEntity newUser = new UserEntity();
        newUser.setKakaoId(registerRequestDTO.getKakaoId());
        newUser.setNickname(registerRequestDTO.getNickname());
        newUser.setProfileImageUrl(finalProfileImageUrl);
        newUser.setKakaoNickname(registerRequestDTO.getKakaoNickname());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(newUser);

        // 2. 사용자가 선택한 취향 태그 저장
        if (registerRequestDTO.getTagIds() != null) {
            for (Long tagId : registerRequestDTO.getTagIds()) {
                UserTagEntity tag = userTagRepository.findById(tagId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다."));

                UserPreferenceEntity preference = new UserPreferenceEntity();
                preference.setUser(newUser);
                preference.setUserTag(tag);
                preference.setAiRecommended(false);
                preference.setCreatedAt(LocalDateTime.now());

                userPreferenceRepository.save(preference);
            }
        }

        log.info("New user registration complete. Kakao ID: {}", newUser.getKakaoId());

        // 3. 최종 JWT 발급 및 반환
        return jwtProvider.createToken(newUser.getKakaoId());
    }
}