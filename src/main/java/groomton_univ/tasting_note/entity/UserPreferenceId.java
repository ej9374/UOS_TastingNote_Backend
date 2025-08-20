package groomton_univ.tasting_note.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class UserPreferenceId implements Serializable {

    private Long user; // UserEntity의 @Id 필드명과 일치시켜야 합니다.
    private Long userTag; // UserTagEntity의 @Id 필드명과 일치시켜야 합니다.

    public UserPreferenceId(Long user, Long userTag) {
        this.user = user;
        this.userTag = userTag;
    }
}