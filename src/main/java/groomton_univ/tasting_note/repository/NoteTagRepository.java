package groomton_univ.tasting_note.repository;

import groomton_univ.tasting_note.entity.NoteTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteTagRepository extends JpaRepository<NoteTagEntity, Long> {
    List<NoteTagEntity> findAllByNote_NoteId(Long noteId);
}
