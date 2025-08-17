package groomton_univ.tasting_note.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import groomton_univ.tasting_note.dto.LabelDto;
import groomton_univ.tasting_note.dto.NoteCreateMetaDto;
import groomton_univ.tasting_note.dto.NoteDto;
import groomton_univ.tasting_note.entity.*;
import groomton_univ.tasting_note.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Value("${gcp.storage.bucket.name}")
    private String bucketName;

    public LabelDto.LabelResponseDto createLabel(String category, String content) {

        // GPT 통신을 통해서 category, content 보내고 label 뽑아오기

        LabelEntity label = new LabelEntity();

        String response = "abc"; //GPT
        label.setContent(response);
        labelRepository.save(label);

        LabelDto.LabelResponseDto result = new LabelDto.LabelResponseDto();
        result.setLabelId(label.getLabelId());
        result.setContent(label.getContent());
        return result;
    }


    public Long uploadImage(MultipartFile file) {

        String uuid = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String newFilename = uuid + "_" + originalFilename;

        Blob blob = storage.create(BlobInfo.newBuilder(BlobId.of(bucketName, newFilename))
                .setContentType(file.getContentType())
                .build());

        String uploadLink = blob.getMediaLink();

        NoteEntity note = new NoteEntity();
        note.setPhoto(uploadLink);
        noteRepository.save(note);
        return note.getNoteId();
    }

    public NoteDto.NoteIdDto createNote(Long noteId, Long userId, NoteCreateMetaDto meta) {
        NoteEntity note = noteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new IllegalArgumentException("note가 존재하지 않습니다."));
        note.setUser(userRepository.findByKakaoId(userId)
                .orElseThrow(() -> new IllegalArgumentException("user가 존재하지 않습니다."))
        );
        note.setLabel(labelRepository.findByLabelId(meta.getLabelId())
                .orElseThrow(()-> new IllegalArgumentException("label이 존재하지 않습니다.")));

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

        if (likeRepository.existsByUser_KakaoIdAndNote_NoteId(userId, noteId)) {
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

        if (bookmarkRepository.findByUser_KakaoIdAndNote_NoteId(userId, noteId) != null) {
            BookmarkEntity mark = bookmarkRepository.findByUser_KakaoIdAndNote_NoteId(userId,noteId);
            bookmarkRepository.delete(mark);
            throw new IllegalArgumentException("해당 북마크가 취소되었습니다.");
        }

        BookmarkEntity bookmark = new BookmarkEntity();
        bookmark.setNote(note);
        bookmark.setUser(user);
        bookmarkRepository.save(bookmark);

        Integer bookmarksCount = bookmarkRepository.countByNote_NoteId(noteId);
        note.setBookMarksCount(bookmarksCount);
        noteRepository.save(note);

        NoteDto.bookmarkResponseDto result = new NoteDto.bookmarkResponseDto();
        result.setNoteId(note.getNoteId());
        result.setBookmarks(note.getBookMarksCount());
        return result;
    }

    public NoteDto.NoteMoreDetailDto getNote(Long noteId) {
        NoteEntity note = noteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new IllegalArgumentException("note가 존재하지 않습니다."));

        List<NoteTagEntity> findTags = noteTagRepository.findAllByNoteId(noteId);
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
