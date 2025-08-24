package groomton_univ.tasting_note.user.dto;

import groomton_univ.tasting_note.entity.UserPreferenceEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class UserResponseDto {

    private Long kakaoId;
    private String nickname;
    private String kakaoNickname;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private Map<String, List<PreferenceDto>> preferences;

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

    /**
     * 사용자의 취향 목록을 카테고리별로 그룹화하여 반환하는 정적 메소드
     */
    public static Map<String, List<PreferenceDto>> preferencesByCategory(List<UserPreferenceEntity> preferences) {
        return preferences.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getUserTag().getCategory(), // 카테고리로 그룹화
                        Collectors.mapping(PreferenceDto::of, Collectors.toList())
                ));
    }
}