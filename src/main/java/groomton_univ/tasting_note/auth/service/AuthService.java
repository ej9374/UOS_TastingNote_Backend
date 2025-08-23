package groomton_univ.tasting_note.auth.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import groomton_univ.tasting_note.auth.JwtProvider;
import groomton_univ.tasting_note.auth.dto.RegisterRequestDTO;
import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.entity.UserPreferenceEntity;
import groomton_univ.tasting_note.entity.UserTagEntity;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final JwtProvider jwtProvider;
    private final Storage storage; // 1. GCS Storage 객체 주입

    @Value("${gcp.storage.bucket.name}")
    private String bucketName; // 2. GCS 버킷 이름 주입

    @Transactional
    public String register(RegisterRequestDTO registerRequestDTO, MultipartFile profileImage) {
        String nickname = registerRequestDTO.getNickname();
        String kakaoNickname = registerRequestDTO.getKakaoNickname();

        // 닉네임 중복 검사 로직 (조건부)
        // 설정하려는 닉네임과 카카오 닉네임이 다를 경우에만 중복 검사 수행
        if (!nickname.equals(kakaoNickname)) {
            if (userRepository.findByNickname(nickname).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }

        // 혹시 모를 이중 가입 방지
        if (userRepository.findByKakaoId(registerRequestDTO.getKakaoId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 유저입니다.");
        }

        String finalProfileImageUrl;

        // GCS 이미지 업로드
        if (profileImage != null && !profileImage.isEmpty()) {
            log.info("새로운 프로필 이미지 업로드 감지. GCS 업로드를 시도합니다.");
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
            finalProfileImageUrl = String.format("https://storage.googleapis.com/%s/%s",
                    bucketName, URLEncoder.encode(newFilename, StandardCharsets.UTF_8));
        } else {
            log.info("새로운 프로필 이미지가 없습니다. 카카오 기본 프로필 URL을 사용합니다.");
            finalProfileImageUrl = registerRequestDTO.getProfileImageUrl();
        }


        // 1. UserEntity 생성 및 저장
        UserEntity newUser = new UserEntity();
        newUser.setKakaoId(registerRequestDTO.getKakaoId());
        newUser.setNickname(nickname);
        newUser.setProfileImageUrl(finalProfileImageUrl);
        newUser.setKakaoNickname(kakaoNickname);
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