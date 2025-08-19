package groomton_univ.tasting_note.controller;

import groomton_univ.tasting_note.dto.AutoDto;
import groomton_univ.tasting_note.dto.LabelDto;
import groomton_univ.tasting_note.dto.NoteDto;
import groomton_univ.tasting_note.global.SuccessResponse;
import groomton_univ.tasting_note.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/notes/{name}")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteHomeResponseDto>>> searchHome(@PathVariable String name) {
        List<NoteDto.NoteHomeResponseDto> responseDto = searchService.getSearchNotes(name);
        return SuccessResponse.onSuccess("검색어에 해당하는 모든 노트를 조회했습니다.", HttpStatus.OK, responseDto);
    }

    @GetMapping("/suggest-name/{q}")
    public ResponseEntity<SuccessResponse<AutoDto>> searchAutoComplete(@PathVariable String q) {
        AutoDto names = searchService.searchAuto(q);
        return SuccessResponse.onSuccess("오토 컴플리트를 통한 검색어를 조회했습니다.", HttpStatus.OK, names);
    }

    @GetMapping("/labels")
    public ResponseEntity<SuccessResponse<List<LabelDto.LabelResponseDto>>> searchLabels() {
        List<LabelDto.LabelResponseDto> responseDto = searchService.getLabels();
        return SuccessResponse.onSuccess("ai가 생성한 라벨들을 전체 조회하였습니다.", HttpStatus.OK, responseDto);
    }

    @GetMapping("/labels/{labelId}")
    public ResponseEntity<SuccessResponse<List<NoteDto.NoteHomeResponseDto>>> searchLabel(@PathVariable Long labelId) {
        List<NoteDto.NoteHomeResponseDto> responseDto = searchService.searchNotesLabel(labelId);
        return SuccessResponse.onSuccess("ai 라벨에 해당하는 노트들을 조회했습니다.", HttpStatus.OK, responseDto);
    }
}
