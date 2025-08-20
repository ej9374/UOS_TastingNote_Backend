package groomton_univ.tasting_note.auth.controller;

import groomton_univ.tasting_note.auth.dto.LoginResponseDTO;
import groomton_univ.tasting_note.auth.dto.RegisterRequestDTO;
import groomton_univ.tasting_note.auth.service.AuthService;
import groomton_univ.tasting_note.global.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<LoginResponseDTO>> register(
            @RequestPart("registerData") RegisterRequestDTO registerRequestDTO, // JSON 데이터
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage // 이미지 파일 (선택적)
    ) {
        String accessToken = authService.register(registerRequestDTO, profileImage);
        LoginResponseDTO response = new LoginResponseDTO(accessToken);
        return SuccessResponse.onSuccess("회원가입 및 로그인에 성공했습니다.", HttpStatus.CREATED, response);
    }
}