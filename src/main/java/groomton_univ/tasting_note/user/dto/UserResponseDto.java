package groomton_univ.tasting_note.user.dto;

import groomton_univ.tasting_note.entity.UserPreferenceEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class UserResponseDto {

    private Long kakaoId;
    private String nickname;
    private String kakaoNickname;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private List<PreferenceDto> preferences;

    @Getter
    @Builder
    public static class PreferenceDto {
        private Long tagId;
        private String name;
        private boolean isAiRecommended;

        public static PreferenceDto of(UserPreferenceEntity preference) {
            return PreferenceDto.builder()
                    .tagId(preference.getUserTag().getId())
                    .name(preference.getUserTag().getName())
                    .isAiRecommended(preference.isAiRecommended())
                    .build();
        }
    }

    public static List<PreferenceDto> preferences(List<UserPreferenceEntity> preferences) {
        return preferences.stream()
                .map(PreferenceDto::of)
                .collect(Collectors.toList());
    }
}