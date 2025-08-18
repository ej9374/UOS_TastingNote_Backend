package groomton_univ.tasting_note.service;

import groomton_univ.tasting_note.dto.NoteDto;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.repository.BookmarkRepository;
import groomton_univ.tasting_note.repository.LikeRepository;
import groomton_univ.tasting_note.repository.NoteRepository;
import groomton_univ.tasting_note.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyService {

    private final NoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;

    public List<NoteDto.NoteSummaryDto> getCategoryNote(Long userId, String category){
        List<NoteEntity> notes = noteRepository.findAllByUser_KakaoIdAndCategoryOrderByNoteIdDesc(userId, category);
        return setListNote(notes);
    }

    public List<NoteDto.NoteSummaryDto> getBookmarksNote(Long userId) {
        List<NoteEntity> notes = bookmarkRepository.findAllByUser_KakaoId(userId);
        return setListNote(notes);
    }

    public List<NoteDto.NoteSummaryDto> getLikesNote(Long userId) {
        List<NoteEntity> notes = likeRepository.findAllByUser_KakaoId(userId);
        return setListNote(notes);
    }

    public List<NoteDto.NoteSummaryDto> setListNote(List<NoteEntity> notes) {
        List<NoteDto.NoteSummaryDto> result = new ArrayList<>();
        for (NoteEntity note : notes) {
            NoteDto.NoteSummaryDto noteSummaryDto = new NoteDto.NoteSummaryDto();
            noteSummaryDto.setNoteId(note.getNoteId());
            noteSummaryDto.setName(note.getName());
            noteSummaryDto.setRating(note.getRating());
            noteSummaryDto.setPhoto(note.getPhoto());
            noteSummaryDto.setDate(note.getDate());
            result.add(noteSummaryDto);
        }
        return result;
    }
}
