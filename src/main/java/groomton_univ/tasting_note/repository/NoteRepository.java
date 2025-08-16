package groomton_univ.tasting_note.repository;

import groomton_univ.tasting_note.entity.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long> {
    Optional<NoteEntity> findByNoteId(Long noteId);
}
