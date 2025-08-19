package groomton_univ.tasting_note.controller;

import groomton_univ.tasting_note.dto.NoteDto;
import groomton_univ.tasting_note.global.SuccessResponse;
import groomton_univ.tasting_note.service.MyService;
import groomton_univ.tasting_note.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my/notes")
@RequiredArgsConstructor
public class MyController {

    private final MyService myService;

    @GetMapping("/{category}")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteSummaryDto>>> getCategoryNote(@PathVariable String category){
        Long userId = 123L;
        List<NoteDto.NoteSummaryDto> notes = myService.getCategoryNote(userId, category);
        return SuccessResponse.onSuccess("해당 카테고리에 대한 모든 노트를 조회했습니다.", HttpStatus.OK, notes);
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteSummaryDto>>> getBookmarksNote(){
        Long userId = 123L;
        List<NoteDto.NoteSummaryDto> notes = myService.getBookmarksNote(userId);
        return SuccessResponse.onSuccess("북마크한 모든 노트를 조회했습니다.", HttpStatus.OK, notes);
    }

    @GetMapping("/likes")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteSummaryDto>>> getLikesNote(){
        Long userId = 123L;
        List<NoteDto.NoteSummaryDto> notes = myService.getLikesNote(userId);
        return SuccessResponse.onSuccess("좋아요 누른 모든 노트를 조회했습니다.", HttpStatus.OK, notes);
    }
}
