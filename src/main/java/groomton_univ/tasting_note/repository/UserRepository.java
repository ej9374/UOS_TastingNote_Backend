package groomton_univ.tasting_note.repository;

import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByKakaoId(Long kakaoId);
}
