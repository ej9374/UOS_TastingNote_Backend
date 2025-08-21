package groomton_univ.tasting_note.note.repository;

import groomton_univ.tasting_note.entity.BookmarkEntity;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {

    BookmarkEntity findByUserAndNote(UserEntity user, NoteEntity note);

    Integer countByNote_NoteId(Long noteId);

    @Query("select b.note from BookmarkEntity b where b.user.kakaoId = :userId order by b.bookmarkId desc")
    List<NoteEntity> findAllByUser_KakaoId(@Param("userId") Long userId);
}
