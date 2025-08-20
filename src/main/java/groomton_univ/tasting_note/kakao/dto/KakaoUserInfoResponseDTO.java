package groomton_univ.tasting_note.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD에 맞춰 필요한 정보만 받도록 수정한 DTO
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 응답에 모르는 필드가 있어도 무시하고 파싱
public class KakaoUserInfoResponseDTO {

    // 회원번호 (UserEntity의 kakaoId에 해당)
    @JsonProperty("id")
    private Long id;

    // 닉네임과 프로필 이미지 URL을 담고 있는 객체
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {

        @JsonProperty("profile")
        private Profile profile;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {

            // 카카오 닉네임 (UserEntity의 kakaoNickname에 해당)
            @JsonProperty("nickname")
            private String nickname;

            // 프로필 사진 URL (UserEntity의 profileImageUrl에 해당)
            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }
}