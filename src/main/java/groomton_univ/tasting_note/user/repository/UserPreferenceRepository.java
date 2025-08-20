package groomton_univ.tasting_note.user.repository;

import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.entity.UserPreferenceEntity;
import groomton_univ.tasting_note.entity.UserPreferenceId;
import groomton_univ.tasting_note.entity.UserTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, UserPreferenceId> {

    // 특정 사용자의 모든 취향 정보를 조회하는 메소드
    List<UserPreferenceEntity> findByUser(UserEntity user);

    // 특정 사용자와 특정 태그의 관계가 존재하는지 확인하는 메소드
    boolean existsByUserAndUserTag(UserEntity user, UserTagEntity userTag);
}