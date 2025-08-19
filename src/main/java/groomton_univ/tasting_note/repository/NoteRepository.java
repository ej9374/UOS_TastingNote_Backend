package groomton_univ.tasting_note.repository;

import groomton_univ.tasting_note.entity.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long>, NoteRepositoryCustom {
    Optional<NoteEntity> findByNoteId(Long noteId);

    List<NoteEntity> findAllByUser_KakaoIdAndCategoryOrderByNoteIdDesc(Long userId, String category);

    List<NoteEntity> findAllByOrderByNoteIdDesc();
}
