package groomton_univ.tasting_note.note.controller;

import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.note.dto.NoteCreateMetaDto;
import groomton_univ.tasting_note.note.dto.NoteDto;
import groomton_univ.tasting_note.global.SuccessResponse;
import groomton_univ.tasting_note.note.dto.NoteUpdateMetaDto;
import groomton_univ.tasting_note.note.service.MyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/my/notes")
@RequiredArgsConstructor
public class MyController {

    private final MyService myService;

    @GetMapping("/{category}")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteSummaryDto>>> getCategoryNote(@PathVariable String category){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
        List<NoteDto.NoteSummaryDto> notes = myService.getCategoryNote(userId, category);
        return SuccessResponse.onSuccess("해당 카테고리에 대한 모든 노트를 조회했습니다.", HttpStatus.OK, notes);
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteSummaryDto>>> getBookmarksNote(){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
        List<NoteDto.NoteSummaryDto> notes = myService.getBookmarksNote(userId);
        return SuccessResponse.onSuccess("북마크한 모든 노트를 조회했습니다.", HttpStatus.OK, notes);
    }

    @GetMapping("/likes")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteSummaryDto>>> getLikesNote(){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
        List<NoteDto.NoteSummaryDto> notes = myService.getLikesNote(userId);
        return SuccessResponse.onSuccess("좋아요 누른 모든 노트를 조회했습니다.", HttpStatus.OK, notes);
    }

    @PutMapping("/update/{noteId}")
    public ResponseEntity<SuccessResponse<NoteDto.NoteIdDto>> updateNote(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "meta", required = false) NoteUpdateMetaDto meta
    ){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
        NoteDto.NoteIdDto responseDto = myService.updateNote(userId, file, meta);
        return SuccessResponse.onSuccess("해당 노트를 성공적으로 수정했습니다.", HttpStatus.OK, responseDto);
    }

    @DeleteMapping("/delete/{noteId}")
    public ResponseEntity<SuccessResponse<Void>> deleteNote(@PathVariable Long noteId){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
         myService.deleteNote(userId, noteId);
        return SuccessResponse.ok("노트를 삭제했습니다.");
    }

}
