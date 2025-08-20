package groomton_univ.tasting_note.kakao.controller;

import groomton_univ.tasting_note.auth.dto.AuthResponseDTO;
import groomton_univ.tasting_note.auth.dto.LoginResponseDTO;
import groomton_univ.tasting_note.global.SuccessResponse;
import groomton_univ.tasting_note.kakao.dto.KakaoUserInfoResponseDTO;
import groomton_univ.tasting_note.kakao.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @GetMapping("/api/v1/auth/kakao")
    public ResponseEntity<SuccessResponse<AuthResponseDTO>> callback(@RequestParam("code") String code) {
        AuthResponseDTO authResponse = kakaoService.handleKakaoLogin(code);
        return SuccessResponse.onSuccess("카카오 로그인 요청에 성공했습니다.", HttpStatus.OK, authResponse);
    }
}