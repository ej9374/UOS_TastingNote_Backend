package groomton_univ.tasting_note.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null이 아닌 필드만 JSON에 포함
public class AuthResponseDTO {

    private Boolean isNewUser;         // 신규 유저 여부
    private UserInfoForRegisterDTO userInfo; // 신규 유저일 경우, 회원가입에 필요한 정보
    private String accessToken;        // 기존 유저일 경우, 발급된 JWT
}