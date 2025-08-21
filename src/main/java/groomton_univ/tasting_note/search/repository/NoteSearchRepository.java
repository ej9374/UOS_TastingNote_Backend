package groomton_univ.tasting_note.search.repository;

import groomton_univ.tasting_note.entity.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteSearchRepository extends JpaRepository<NoteEntity, Long> {
    Optional<NoteEntity> findByNoteId(Long noteId);

    List<NoteEntity> findAllByUser_KakaoIdAndCategoryOrderByNoteIdDesc(Long userId, String category);

    List<NoteEntity> findAllByOrderByNoteIdDesc();

    List<NoteEntity> findAllByLabel_LabelIdOrderByNoteIdDesc(Long labelId);

    List<NoteEntity> findByNameContainingOrderByNoteIdDesc(String name);

    @Query("select distinct n.name from NoteEntity n where lower(n.name) like lower(concat('%', :q, '%')) order by n.name asc")
    List<String> suggestNames(String q);
}
