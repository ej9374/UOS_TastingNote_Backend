package groomton_univ.tasting_note.repository;

import groomton_univ.tasting_note.entity.LikeEntity;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    // 존재 여부만 확인 (더 가볍고 빠름)
    boolean existsByUserAndNote(UserEntity user, NoteEntity note);

    Integer countByNote_NoteId(Long noteId);
}
