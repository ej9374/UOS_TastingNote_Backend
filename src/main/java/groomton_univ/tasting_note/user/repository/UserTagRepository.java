package groomton_univ.tasting_note.user.repository;

import groomton_univ.tasting_note.entity.UserTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTagRepository extends JpaRepository<UserTagEntity, Long> {

    // 태그 이름으로 태그를 찾는 메소드
    Optional<UserTagEntity> findByName(String name);
}