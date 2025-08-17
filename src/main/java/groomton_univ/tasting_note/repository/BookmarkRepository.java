package groomton_univ.tasting_note.repository;

import groomton_univ.tasting_note.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, Long> {

    BookmarkEntity findByUser_KakaoIdAndNote_NoteId(Long userId, Long noteId);

    Integer countByNote_NoteId(Long noteId);
}
