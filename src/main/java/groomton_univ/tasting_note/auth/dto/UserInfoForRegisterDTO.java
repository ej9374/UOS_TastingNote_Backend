package groomton_univ.tasting_note.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoForRegisterDTO {
    private Long kakaoId;
    private String kakaoNickname;
    private String profileImageUrl;
}