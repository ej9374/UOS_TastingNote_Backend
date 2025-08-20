package groomton_univ.tasting_note.kakao.service;

import groomton_univ.tasting_note.auth.JwtProvider;
import groomton_univ.tasting_note.auth.dto.AuthResponseDTO;
import groomton_univ.tasting_note.auth.dto.UserInfoForRegisterDTO;
import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.kakao.dto.KakaoTokenResponseDTO;
import groomton_univ.tasting_note.kakao.dto.KakaoUserInfoResponseDTO;
import groomton_univ.tasting_note.user.repository.UserRepository;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Value("${kakao.client_id}")
    private String clientId;

    private final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    @Transactional(readOnly = true)
    public AuthResponseDTO handleKakaoLogin(String code) {
        String kakaoAccessToken = getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDTO userInfo = getUserInfo(kakaoAccessToken);

        Optional<UserEntity> existingUser = userRepository.findByKakaoId(userInfo.getId());

        if (existingUser.isPresent()) {
            // Case 1: 기존 회원일 경우
            UserEntity user = existingUser.get();
            String serviceAccessToken = jwtProvider.createToken(user.getKakaoId());
            log.info("[Kakao Service] Existing user login. Kakao ID: {}", user.getKakaoId());

            return AuthResponseDTO.builder()
                    .isNewUser(false)
                    .accessToken(serviceAccessToken)
                    .build();
        } else {
            // Case 2: 신규 회원일 경우
            log.info("[Kakao Service] New user detected. Needs registration. Kakao ID: {}", userInfo.getId());
            KakaoUserInfoResponseDTO.KakaoAccount.Profile profile = userInfo.getKakaoAccount().getProfile();

            UserInfoForRegisterDTO userInfoDto = UserInfoForRegisterDTO.builder()
                    .kakaoId(userInfo.getId())
                    .nickname(profile.getNickname())
                    .profileImageUrl(profile.getProfileImageUrl())
                    .build();

            return AuthResponseDTO.builder()
                    .isNewUser(true)
                    .userInfo(userInfoDto)
                    .build();
        }
    }


    // 인가 코드로 액세스 토큰 발급
    public String getAccessTokenFromKakao(String code) {
        KakaoTokenResponseDTO kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .bodyToMono(KakaoTokenResponseDTO.class)
                .block();

        log.info("[Kakao Service] Access Token Issued: {}", kakaoTokenResponseDto.getAccessToken());
        return kakaoTokenResponseDto.getAccessToken();
    }

    // 액세스 토큰으로 사용자 정보 조회 및 로그 출력
    public KakaoUserInfoResponseDTO getUserInfo(String accessToken) {
        KakaoUserInfoResponseDTO userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDTO.class)
                .block();

        log.info("======================================================");
        log.info("[Kakao API] User Info Response Received");

        if (userInfo != null) {
            log.info("회원번호 (id): {}", userInfo.getId());

            if (userInfo.getKakaoAccount() != null && userInfo.getKakaoAccount().getProfile() != null) {
                KakaoUserInfoResponseDTO.KakaoAccount.Profile profile = userInfo.getKakaoAccount().getProfile();
                log.info("--- Profile Info ---");
                log.info("닉네임 (nickname): {}", profile.getNickname());
                log.info("프로필 이미지 URL (profile_image_url): {}", profile.getProfileImageUrl());
            } else {
                log.warn("Kakao Account or Profile information is missing.");
            }
        } else {
            log.error("Failed to retrieve user information from Kakao.");
        }

        log.info("======================================================");

        return userInfo;
    }
}