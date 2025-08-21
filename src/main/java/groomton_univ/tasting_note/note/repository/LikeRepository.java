package groomton_univ.tasting_note.note.repository;

import groomton_univ.tasting_note.entity.LikeEntity;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    // 존재 여부만 확인 (더 가볍고 빠름)
    boolean existsByUserAndNote(UserEntity user, NoteEntity note);

    Integer countByNote_NoteId(Long noteId);

    @Query("select l.note from LikeEntity l where l.user.kakaoId = :userId order by l.likeId desc")
    List<NoteEntity> findAllByUser_KakaoId(@Param("userId") Long userId);
}
