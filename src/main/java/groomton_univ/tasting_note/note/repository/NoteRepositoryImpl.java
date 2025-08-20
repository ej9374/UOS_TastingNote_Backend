package groomton_univ.tasting_note.note.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.entity.QNoteEntity;
import groomton_univ.tasting_note.entity.QNoteTagEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class NoteRepositoryImpl implements NoteRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<NoteEntity> searchNotes(String category, String degree, Integer days, List<String> tags) {
        QNoteEntity note = QNoteEntity.noteEntity;
        QNoteTagEntity noteTag = QNoteTagEntity.noteTagEntity;

        BooleanBuilder builder = new BooleanBuilder();

        if (category != null && !category.isBlank()) {
            builder.and(note.category.eq(category));
        }

        if (degree != null && !degree.isBlank()) {
            builder.and(note.degree.eq(degree));
        }

        if (days != null) {
            builder.and(note.date.goe(LocalDate.now().minusDays(days)));
        }

        JPAQuery<NoteEntity> query = queryFactory
                .selectFrom(note)
                .leftJoin(noteTag.note, note).on(noteTag.note.eq(note))
                .where(builder)
                .distinct();

        if (tags != null && !tags.isEmpty()) {
            query.where(
                    noteTag.name.in(tags).and(noteTag.value.goe(2.5))
            ).groupBy(note.noteId).having(noteTag.name.countDistinct().eq((long) tags.size()));
        }

        return query.orderBy(note.noteId.desc()).fetch();
    }

}
