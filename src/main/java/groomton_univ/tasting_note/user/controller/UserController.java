package groomton_univ.tasting_note.user.controller;

import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.global.SuccessResponse;
import groomton_univ.tasting_note.user.dto.*;
import groomton_univ.tasting_note.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인된 사용자의 정보를 반환하는 API
     * @AuthenticationPrincipal 어노테이션을 통해 SecurityContextHolder에 저장된 사용자 정보를 바로 주입받을 수 있습니다.
     */
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserResponseDto>> getMyInfo(@AuthenticationPrincipal UserEntity user) {
        UserResponseDto userInfo = userService.getMyInfo(user);
        return SuccessResponse.onSuccess("현재 로그인된 사용자 정보 조회에 성공했습니다.", HttpStatus.OK, userInfo);
    }

    /**
     * 사용자 취향(태그) 정보를 수정하는 API
     */
    @PutMapping("/me/preferences")
    public ResponseEntity<SuccessResponse<UserResponseDto>> updateMyPreferences(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody PreferenceUpdateRequestDto requestDto) {

        UserResponseDto updatedUserInfo = userService.updateUserPreferences(user, requestDto);
        return SuccessResponse.onSuccess("사용자 취향 정보 수정에 성공했습니다.", HttpStatus.OK, updatedUserInfo);
    }

    /**
     * 전체 태그 목록 조회 API
     */
    @GetMapping("/tags")
    public ResponseEntity<SuccessResponse<List<UserTagResponseDto>>> getAllUserTags() {
        List<UserTagResponseDto> allTags = userService.getAllUserTags();
        return SuccessResponse.onSuccess("전체 태그 목록 조회에 성공했습니다.", HttpStatus.OK, allTags);
    }

    /**
     * 사용자 닉네임, 프로필 이미지 수정 API (새로 추가)
     */
    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse<UserResponseDto>> updateMyInfo(
            @AuthenticationPrincipal UserEntity user,
            @RequestPart(value = "nickname", required = false) UserUpdateRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        UserResponseDto updatedUserInfo = userService.updateMyInfo(user, requestDto, profileImage);
        return SuccessResponse.onSuccess("사용자 정보 수정에 성공했습니다.", HttpStatus.OK, updatedUserInfo);
    }

    /**
     * 닉네임 중복 확인 API (POST 방식으로 수정)
     */
    @PostMapping("/nickname/check")
    public ResponseEntity<SuccessResponse<NicknameCheckDto.Response>> checkNickname(@Valid @RequestBody NicknameCheckDto.Request requestDto) {
        boolean isAvailable = userService.isNicknameAvailable(requestDto);
        NicknameCheckDto.Response responseDto = new NicknameCheckDto.Response(isAvailable);
        return SuccessResponse.onSuccess("닉네임 사용 가능 여부를 확인했습니다.", HttpStatus.OK, responseDto);
    }
}
