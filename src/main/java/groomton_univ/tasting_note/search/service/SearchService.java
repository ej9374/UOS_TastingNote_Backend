package groomton_univ.tasting_note.search.service;

import groomton_univ.tasting_note.search.dto.AutoDto;
import groomton_univ.tasting_note.search.dto.LabelDto;
import groomton_univ.tasting_note.search.dto.NoteDto;
import groomton_univ.tasting_note.entity.LabelEntity;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.search.repository.LabelSearchRepository;
import groomton_univ.tasting_note.search.repository.NoteSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Qualifier;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final LabelSearchRepository labelRepository;

    private final NoteSearchRepository noteRepository;

    public List<NoteDto.NoteHomeResponseDto> getSearchNotes(String name){
        List<NoteEntity> notes = noteRepository.findByNameContainingOrderByNoteIdDesc(name);
        List<NoteDto.NoteHomeResponseDto> results = entityToDto(notes);
        return results;
    }

    public AutoDto searchAuto(String p){
        List<String> names = noteRepository.suggestNames(p);
        AutoDto autoDto = new AutoDto();
        autoDto.setNames(names);
        return autoDto;
    }

    public List<LabelDto.LabelResponseDto> getLabels() {
        List<LabelEntity> labels = labelRepository.findAll();
        List<LabelDto.LabelResponseDto> results = new ArrayList<>();
        for (LabelEntity label : labels) {
            LabelDto.LabelResponseDto result = new LabelDto.LabelResponseDto();
            result.setLabelId(label.getLabelId());
            result.setContent(label.getContent());
            results.add(result);
        }
        return results;
    }

    public List<NoteDto.NoteHomeResponseDto> searchNotesLabel(Long labelId) {
        List<NoteEntity> notes = noteRepository.findAllByLabel_LabelIdOrderByNoteIdDesc(labelId);
        List<NoteDto.NoteHomeResponseDto> results = entityToDto(notes);
        return results;
    }

    public List<NoteDto.NoteHomeResponseDto> entityToDto(List<NoteEntity> notes) {
        List<NoteDto.NoteHomeResponseDto> results = new ArrayList<>();
        for (NoteEntity note : notes) {
            NoteDto.NoteHomeResponseDto result = new NoteDto.NoteHomeResponseDto();
            result.setNoteId(note.getNoteId());
            result.setName(note.getName());
            result.setContent(note.getContent());
            result.setRating(note.getRating());
            result.setPhoto(note.getPhoto());
            result.setDate(note.getDate());
            result.setLikes(note.getLikesCount());
            results.add(result);
        }
        return results;
    }
}
