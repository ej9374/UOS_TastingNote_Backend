package groomton_univ.tasting_note.user.dto;

import groomton_univ.tasting_note.entity.UserTagEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserTagResponseDto {

    private Long tagId;
    private String name;

    public static UserTagResponseDto of(UserTagEntity tag) {
        return UserTagResponseDto.builder()
                .tagId(tag.getId())
                .name(tag.getName())
                .build();
    }
}