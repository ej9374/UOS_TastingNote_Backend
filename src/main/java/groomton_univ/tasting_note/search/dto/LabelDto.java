package groomton_univ.tasting_note.search.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class LabelDto {

    @Getter
    public static class LabelRequestDto {
        private String category;
        private String content;
    }

    @Getter
    @Setter
    public static class LabelResponseDto {
        private Long labelId;
        private String content;
    }

}
