package groomton_univ.tasting_note.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class RegisterRequestDTO {
    private Long kakaoId;
    private String nickname;
    private String profileImageUrl; // 카카오 기본 프로필 이미지 URL
    private String kakaoNickname;   // 카카오 기본 닉네임
    private List<Long> tagIds;
}