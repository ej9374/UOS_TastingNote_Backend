package groomton_univ.tasting_note.search.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class NoteDto {

    @Setter
    @Getter
    public static class NoteIdDto {
        private Long noteId;
    }

    @Getter
    @Setter
    public static class NoteSummaryDto {
        private Long noteId;
        private String name;
        private Integer rating;
        private String photo;
        private LocalDate date;
    }

    @Getter
    @Setter
    public static class NoteHomeResponseDto {
        private Long noteId;
        private String name;
        private String content;
        private Integer rating;
        private String photo;
        private LocalDate date;
        private Integer likes;
    }

    @Setter
    @Getter
    @Builder
    public static class NoteMoreDetailDto {

        private Long noteId;
        private String name;
        private String degree;
        private String category;
        private String categoryStyle;
        private String photo;
        private Integer rating;
        private String content;
        private List<NoteTagDto> tags;
        private String label;
        private LocalDate date;
        private Integer likes;
        private Integer bookmarks;
    }

    @Getter
    @Setter
    public static class NoteTagDto {
        private String name;
        private Integer value;
    }

    @Getter
    @Setter
    public static class likesResponseDto {
        private Long noteId;
        private Integer likes;
    }

    @Getter
    @Setter
    public static class bookmarkResponseDto {
        private Long noteId;
        private Integer bookmarks;
        private boolean bookmarked;
    }

}
