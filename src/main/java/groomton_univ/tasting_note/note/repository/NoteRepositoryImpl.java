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
        QNoteEntity n = QNoteEntity.noteEntity;
        QNoteTagEntity nt = QNoteTagEntity.noteTagEntity;

        BooleanBuilder builder = new BooleanBuilder();
        if (category != null && !category.isBlank()) builder.and(n.category.eq(category));
        if (degree != null && !degree.isBlank())     builder.and(n.degree.eq(degree));
        if (days != null)                             builder.and(n.date.goe(LocalDate.now().minusDays(days)));

        JPAQuery<NoteEntity> q = queryFactory
                .selectDistinct(n)
                .from(n)
                .where(builder);

        if (tags != null && !tags.isEmpty()) {
            q.innerJoin(nt).on(nt.note.eq(n))
                    .where(
                            nt.name.in(tags)
                            // nt.value가 Integer면 2.5 비교는 불가 → 3 이상으로 해석
                            // nt.value가 Double이면 아래 줄을 goe(2.5)로 바꾸세요.
                            , nt.value.goe(3)
                    )
                    .groupBy(n.noteId)
                    .having(nt.name.countDistinct().eq((long) tags.size()));
        }

        return q.orderBy(n.noteId.desc()).fetch();
    }

}
