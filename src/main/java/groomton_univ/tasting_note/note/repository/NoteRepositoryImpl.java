package groomton_univ.tasting_note.note.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.entity.NoteTagEntity;
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
                            ,nt.value.goe(3)
                    )
                    .groupBy(n.noteId)
                    .having(nt.name.countDistinct().eq((long) tags.size()));
        }

        return q.orderBy(n.noteId.desc()).fetch();
    }

    @Override
    public List<NoteEntity> recommendNotes(Long userId) {
        QNoteEntity n = QNoteEntity.noteEntity;
        QNoteTagEntity bt = new QNoteTagEntity("bt");
        QNoteTagEntity ct = new QNoteTagEntity("ct");

        NoteEntity base = queryFactory
                .selectFrom(n)
                .where(n.user.kakaoId.eq(userId))
                .orderBy(n.noteId.desc())
                .limit(1)
                .fetchOne();

        if (base == null)
            return List.of();

        // 2. base.category와 같은 후보들 중에서 태그 비교
        BooleanExpression sameCategory = n.category.eq(base.getCategory());

        BooleanExpression tagSuperset = JPAExpressions
                .selectOne()
                .from(bt)
                .where(bt.note.eq(base)
                        .and(
                                JPAExpressions.selectOne()
                                        .from(ct)
                                        .where(
                                                ct.note.eq(n)
                                                .and(ct.name.eq(bt.name))
                                                .and(ct.value.goe(bt.value))
                                        )
                                        .notExists()
                        )
                ).notExists();

        return queryFactory
                .selectFrom(n)
                .where(
                        sameCategory,
                        n.noteId.ne(base.getNoteId()),
                        tagSuperset
                )
                .orderBy(n.noteId.desc())
                .fetch();
    }
}
