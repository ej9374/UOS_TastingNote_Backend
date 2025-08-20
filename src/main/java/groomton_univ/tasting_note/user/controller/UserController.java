package groomton_univ.tasting_note.user.controller;

import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.global.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    /**
     * 현재 로그인된 사용자의 정보를 반환하는 테스트 API
     * @AuthenticationPrincipal 어노테이션을 통해 SecurityContextHolder에 저장된 사용자 정보를 바로 주입받을 수 있습니다.
     */
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getMyInfo(@AuthenticationPrincipal UserEntity user) {

        // 1. @AuthenticationPrincipal을 통해 주입받은 UserEntity가 null인지 확인
        if (user == null) {
            // 이 경우는 JwtAuthenticationFilter에서 토큰이 유효하지 않다고 판단하여 요청이 거부되므로,
            // 사실상 이 컨트롤러 메소드까지 요청이 도달했다면 user는 항상 null이 아닙니다.
            // 하지만 방어적인 코드를 위해 체크할 수 있습니다.
            log.warn("인증된 사용자 정보를 찾을 수 없습니다.");
            // SecurityConfig에서 .anyRequest().authenticated() 설정에 따라 401 Unauthorized가 자동으로 응답됩니다.
            // 따라서 별도의 에러 응답을 만들 필요는 없습니다.
        }

        log.info("현재 로그인된 사용자: Nickname = {}, KakaoID = {}", user.getNickname(), user.getKakaoId());

        // 2. 클라이언트에게 반환할 사용자 정보 맵 생성
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("kakaoId", user.getKakaoId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("profileImageUrl", user.getProfileImageUrl());
        userInfo.put("createdAt", user.getCreatedAt());

        // 3. 성공 응답 반환
        return SuccessResponse.onSuccess("현재 로그인된 사용자 정보 조회에 성공했습니다.", HttpStatus.OK, userInfo);
    }
}
