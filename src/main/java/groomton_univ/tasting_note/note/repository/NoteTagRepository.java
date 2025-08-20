package groomton_univ.tasting_note.note.repository;

import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.entity.NoteTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteTagRepository extends JpaRepository<NoteTagEntity, Long> {
    List<NoteTagEntity> findAllByNote_NoteId(Long noteId);

    NoteTagEntity findByName(String name);

    Integer findValueByName(String name);

    List<Long> findTagIdsByNote_NoteId(Long noteId);

    void deleteByNote(NoteEntity note);
}
