package groomton_univ.tasting_note.user.dto;

import groomton_univ.tasting_note.entity.UserTagEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserTagResponseDto {

    /**
     * 개별 태그 정보를 담는 내부 클래스
     */
    @Getter
    @Builder
    public static class TagInfo {
        private Long tagId;
        private String name;

        public static TagInfo from(UserTagEntity tag) {
            return TagInfo.builder()
                    .tagId(tag.getId())
                    .name(tag.getName())
                    .build();
        }
    }

    /**
     * 전체 태그 목록을 카테고리별로 그룹화하여 반환하는 정적 메소드
     */
    public static Map<String, List<TagInfo>> from(List<UserTagEntity> tags) {
        return tags.stream()
                .collect(Collectors.groupingBy(
                        UserTagEntity::getCategory, // category 필드로 그룹화
                        Collectors.mapping(TagInfo::from, Collectors.toList())
                ));
    }
}