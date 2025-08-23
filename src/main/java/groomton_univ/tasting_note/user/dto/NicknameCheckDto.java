package groomton_univ.tasting_note.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 컨테이너 역할을 하는 외부 클래스
public class NicknameCheckDto {

    /**
     * 닉네임 중복 확인 요청을 위한 DTO
     */
    @Getter
    @NoArgsConstructor
    public static class Request {

        @Size(min = 2, max = 8, message = "닉네임은 두 글자 이상이어야 합니다.")
        @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
        private String nickname;
        private String kakaoNickname;
    }

    /**
     * 닉네임 중복 확인 응답을 위한 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class Response {
        private boolean isAvailable;
    }
}