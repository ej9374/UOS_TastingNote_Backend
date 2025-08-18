package groomton_univ.tasting_note.service;

import com.google.cloud.storage.*;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import groomton_univ.tasting_note.dto.LabelDto;
import groomton_univ.tasting_note.dto.NoteCreateMetaDto;
import groomton_univ.tasting_note.dto.NoteDto;
import groomton_univ.tasting_note.entity.*;
import groomton_univ.tasting_note.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final LabelRepository labelRepository;
    private final UserRepository userRepository;
    private final NoteTagRepository noteTagRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final Storage storage;
    private final GenerativeModel generativeModel;

    @Value("${gcp.storage.bucket.name}")
    private String bucketName;

    @Transactional
    public LabelDto.LabelResponseDto createLabel(String category, String content) {

        String prompt = makePrompt(category, content);
        String response;
        try {
            var res = generativeModel.generateContent(prompt);
            response = res.getCandidatesList().get(0).getContent().getParts(0).getText();
        } catch(Exception e){
            throw new IllegalArgumentException("Vertex AI 호출 중 오류가 발생했습니다.");
        }

        LabelEntity label = new LabelEntity();
        label.setContent(response);
        labelRepository.save(label);

        LabelDto.LabelResponseDto result = new LabelDto.LabelResponseDto();
        result.setLabelId(label.getLabelId());
        result.setContent(label.getContent());
        return result;
    }

    public String makePrompt(String category, String content){
        return """
                너는 사용자의 음료 카테고리와 시음 소감을 분석해, 그 상황에 맞는 한국어 추천 라벨을 '정확히 1개' 생성한다.
                
                출력 형식은 반드시 한글 문자열만 허락한다.
                
                절대 규칙
                1) "content" 값은 한 줄(single line) 문자열이어야 하며 개행(\\\\n, \\\\r), 탭(\\\\t), 앞뒤 공백을 포함하지 않는다.
                2) 길이: 5~14자.
                3) 문장부호, 따옴표, 이모지, 숫자, 영어, 고유명사/브랜드 금지. 존댓말·종결어미("~다") 금지.
                4) 형식: 명사구 또는 "~할 때", "~좋은 날", "~하는" 패턴의 간결한 구절.
                5) 주제는 날씨, 장소/환경(실내·실외·조도·소음), 동반자, 시간대, 분위기 중 1가지만 선택.
                6) 입력에 날씨·시간대·동반자·기분 단서가 있으면 우선 반영.
                7) 평가/평점 표현(맛있다/별점/최고 등)과 부정 표현 남발 금지. 상황 추천에 집중.
                
                해석 가이드(소감 → 라벨 톤 예시)
                - 쓴맛이 강하고 단 게 땡긴다 → "달달한 디저트와 함께"
                - 산미/상큼함 선호 → "가벼운 오후 산책", "맑은 날 테라스"
                - 진하고 무거움 선호 → "조용한 밤 독서", "집중이 필요한 시간"
                - 나와 안 맞음(부정) → 회피가 아닌 중립적 상황 추천으로 전환(예: "단맛 땡기는 날")
                
                입력 정보:
                category: %s
                tasting_note: %s
                """.formatted(category, content);
    }


    public Long uploadImage(MultipartFile file) {

        String newFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        BlobId blobId = BlobId.of(bucketName, newFilename);
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, newFilename)
                .setContentType(file.getContentType())
                .build();

        try {
            Blob blob = storage.create(blobInfo, file.getBytes());
            if (blob == null || blob.getSize() == 0) {
                throw new IllegalArgumentException("GCS 업로드 오류가 발생했습니다.");
            }
        } catch (IOException | StorageException e){
            throw new IllegalArgumentException("GCS 업로드 중 오류가 발생했습니다.");
        }

        String publicUrl = String.format("https://storage.googleapis.com/%s/%s",
                bucketName, URLEncoder.encode(newFilename, StandardCharsets.UTF_8));

        NoteEntity note = new NoteEntity();
        note.setPhoto(publicUrl);
        noteRepository.save(note);
        return note.getNoteId();
    }

    public NoteDto.NoteIdDto createNote(Long noteId, Long userId, NoteCreateMetaDto meta) {
        NoteEntity note = noteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트가 존재하지 않습니다."));
        note.setUser(userRepository.findByKakaoId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."))
        );
        note.setLabel(labelRepository.findByLabelId(meta.getLabelId())
                .orElseThrow(()-> new IllegalArgumentException("해당 라벨이 존재하지 않습니다.")));

        List<NoteDto.NoteTagDto> metaTags = meta.getTags();
        for (NoteDto.NoteTagDto tag : metaTags) {
            NoteTagEntity noteTag = new NoteTagEntity();
            noteTag.setNote(note);
            noteTag.setName(tag.getName());
            noteTag.setValue(tag.getValue());
            noteTagRepository.save(noteTag);
        }

        note.setCategory(meta.getCategory());
        note.setCategoryStyle(meta.getCategoryStyle());
        note.setDegree(meta.getDegree());
        note.setName(meta.getName());
        note.setRating(meta.getRating());
        note.setContent(meta.getContent());
        note.setDate(meta.getDate());
        noteRepository.save(note);

        NoteDto.NoteIdDto result = new NoteDto.NoteIdDto();
        result.setNoteId(note.getNoteId());

        return result;
    }

    @Transactional
    public NoteDto.likesResponseDto addLike(Long noteId, Long userId) {

        NoteEntity note = noteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트가 존재하지 않습니다."));
        UserEntity user = userRepository.findByKakaoId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        if (likeRepository.existsByUserAndNote(user, note)) {
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
        }

        LikeEntity like = new LikeEntity();
        like.setNote(note);
        like.setUser(user);
        likeRepository.save(like);

        Integer likesCount = likeRepository.countByNote_NoteId(noteId);
        note.setLikesCount(likesCount);
        noteRepository.save(note);

        NoteDto.likesResponseDto result = new NoteDto.likesResponseDto();
        result.setNoteId(note.getNoteId());
        result.setLikes(note.getLikesCount());
        return result;
    }

    @Transactional
    public NoteDto.bookmarkResponseDto addBookmark(Long noteId, Long userId) {
        NoteEntity note = noteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트가 존재하지 않습니다."));
        UserEntity user = userRepository.findByKakaoId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        BookmarkEntity existing = bookmarkRepository.findByUserAndNote(user, note);
        boolean bookmarked;
        if (existing != null) {
            bookmarkRepository.delete(existing);
            bookmarked = false;
        } else {
            BookmarkEntity bookmark = new BookmarkEntity();
            bookmark.setNote(note);
            bookmark.setUser(user);
            bookmarkRepository.save(bookmark);
            bookmarked = true;
        }

        Integer bookmarksCount = bookmarkRepository.countByNote_NoteId(noteId);
        note.setBookMarksCount(bookmarksCount);
        noteRepository.save(note);

        NoteDto.bookmarkResponseDto result = new NoteDto.bookmarkResponseDto();
        result.setNoteId(note.getNoteId());
        result.setBookmarks(note.getBookMarksCount());
        result.setBookmarked(bookmarked);
        return result;
    }

    public NoteDto.NoteMoreDetailDto getNote(Long noteId) {
        NoteEntity note = noteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트가 존재하지 않습니다."));

        List<NoteTagEntity> findTags = noteTagRepository.findAllByNote_NoteId(noteId);
        List<NoteDto.NoteTagDto> tags = new ArrayList<>();
        for (NoteTagEntity tag : findTags) {
            NoteDto.NoteTagDto tagDto = new NoteDto.NoteTagDto();
            tagDto.setName(tag.getName());
            tagDto.setValue(tag.getValue());
            tags.add(tagDto);
        }

        NoteDto.NoteMoreDetailDto result = NoteDto.NoteMoreDetailDto.builder()
                .name(note.getName())
                .degree(note.getDegree())
                .category(note.getCategory())
                .categoryStyle(note.getCategoryStyle())
                .photo(note.getPhoto())
                .rating(note.getRating())
                .content(note.getContent())
                .tags(tags)
                .label(note.getLabel().getContent())
                .date(note.getDate())
                .likes(note.getLikesCount())
                .bookmarks(note.getBookMarksCount())
                .build();

        return result;
    }

}
