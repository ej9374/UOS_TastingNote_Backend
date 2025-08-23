package groomton_univ.tasting_note.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PreferenceUpdateRequestDto {
    private List<Long> tagIds;
}