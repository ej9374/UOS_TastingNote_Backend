package groomton_univ.tasting_note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteId;

    @ManyToOne
    UserEntity user;

    @OneToOne
    LabelEntity label;

    private String category;
    private String categoryStyle;
    private String degree;
    private String name;
    private String photo;
    private Integer rating;
    private String content;
    private Integer likesCount = 0;
    private Integer bookMarksCount = 0;

}
