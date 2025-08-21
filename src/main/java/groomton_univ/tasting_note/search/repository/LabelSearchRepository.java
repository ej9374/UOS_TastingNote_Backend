package groomton_univ.tasting_note.search.repository;

import groomton_univ.tasting_note.entity.LabelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabelSearchRepository extends JpaRepository<LabelEntity, Long> {
    Optional<LabelEntity> findByLabelId(Long labelId);
}
