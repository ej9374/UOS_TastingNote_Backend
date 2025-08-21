package groomton_univ.tasting_note.note.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class NoteCreateMetaDto {

    private String category;
    private String categoryStyle;
    private String degree;
    private String name;
    private Integer rating;
    private String content;
    private Long labelId;
    private List<NoteDto.NoteTagDto> tags;
    private LocalDate date;
}
