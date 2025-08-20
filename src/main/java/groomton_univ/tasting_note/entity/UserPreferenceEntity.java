package groomton_univ.tasting_note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@IdClass(UserPreferenceId.class) // 복합 키 클래스를 지정
@Table(name = "user_preferences")
public class UserPreferenceEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private UserTagEntity userTag;

    @Column(nullable = false)
    private boolean isAiRecommended;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}