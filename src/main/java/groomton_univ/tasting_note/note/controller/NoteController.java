package groomton_univ.tasting_note.note.controller;

import groomton_univ.tasting_note.entity.UserEntity;
import groomton_univ.tasting_note.note.dto.*;
import groomton_univ.tasting_note.global.SuccessResponse;
import groomton_univ.tasting_note.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/labels")
    public ResponseEntity<SuccessResponse<LabelDto.LabelResponseDto>> createLabel(@RequestBody LabelDto.LabelRequestDto requestDto) {
        LabelDto.LabelResponseDto responseDto = noteService.createLabel(requestDto.getCategory(), requestDto.getContent());
        return SuccessResponse.onSuccess("라벨을 생성했습니다.", HttpStatus.CREATED, responseDto);
    }

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<NoteDto.NoteIdDto>> createNote(
            @RequestPart("file") MultipartFile file,
            @RequestPart("meta") NoteCreateMetaDto meta
    ){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
        Long noteId = noteService.uploadImage(file);
        NoteDto.NoteIdDto responseDto = noteService.createNote(noteId, userId, meta);
        return SuccessResponse.onSuccess("노트가 생성되었습니다.", HttpStatus.CREATED, responseDto);
    }

    @PostMapping("likes/{noteId}")
    public ResponseEntity<SuccessResponse<NoteDto.likesResponseDto>> likeNote(@PathVariable Long noteId){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
        NoteDto.likesResponseDto responseDto = noteService.addLike(noteId, userId);
        return SuccessResponse.onSuccess("해당 노트에 좋아요를 눌렀습니다.", HttpStatus.OK, responseDto);
    }

    @PostMapping("bookmarks/{noteId}")
    public ResponseEntity<SuccessResponse<NoteDto.bookmarkResponseDto>> bookmarkNote(@PathVariable Long noteId){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
        NoteDto.bookmarkResponseDto responseDto = noteService.addBookmark(noteId, userId);
        return SuccessResponse.onSuccess("해당 노트에 북마크를 눌렀습니다.", HttpStatus.OK, responseDto);
    }

    @GetMapping("/note/{noteId}")
    public ResponseEntity<SuccessResponse<NoteDto.NoteMoreDetailDto>> getNote(@PathVariable Long noteId) {
        NoteDto.NoteMoreDetailDto responseDto = noteService.getNote(noteId);
        return SuccessResponse.onSuccess("해당 노트를 조회했습니다.", HttpStatus.OK, responseDto);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteHomeResponseDto>>> getHomeNotes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String degree,
            @RequestParam(required = false) Integer time,
            @RequestParam(required = false) String tags
    ) {
        List<NoteDto.NoteHomeResponseDto> results = noteService.findHomeNotes(category, degree, time, tags);
        return SuccessResponse.onSuccess("홈 화면을 조회했습니다.", HttpStatus.OK, results);
    }

    @GetMapping("/recommend")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteHomeResponseDto>>> getRecommendNotes(){
        UserEntity user = ((UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long userId = user.getKakaoId();
        List<NoteDto.NoteHomeResponseDto> responseDto = noteService.getRecommendHomeNotes(userId);
        return SuccessResponse.onSuccess("유저의 취향에 맞는 노트를 조회했습니다.", HttpStatus.OK, responseDto);
    }
}
