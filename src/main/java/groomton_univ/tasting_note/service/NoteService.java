package groomton_univ.tasting_note.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import groomton_univ.tasting_note.dto.LabelDto;
import groomton_univ.tasting_note.dto.NoteCreateMetaDto;
import groomton_univ.tasting_note.dto.NoteDto;
import groomton_univ.tasting_note.entity.LabelEntity;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.entity.NoteTagEntity;
import groomton_univ.tasting_note.repository.LabelRepository;
import groomton_univ.tasting_note.repository.NoteRepository;
import groomton_univ.tasting_note.repository.NoteTagRepository;
import groomton_univ.tasting_note.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    public NoteDto.NoteDetailDto createNote(Long noteId, Long userId, NoteCreateMetaDto meta) {
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

        List<NoteTagEntity> findTags = noteTagRepository.findAllByNoteId(noteId);
        List<NoteDto.NoteTagDto> tags = new ArrayList<>();
        for (NoteTagEntity tag : findTags) {
            NoteDto.NoteTagDto tagDto = new NoteDto.NoteTagDto();
            tagDto.setName(tag.getName());
            tagDto.setValue(tag.getValue());
            tags.add(tagDto);
        }

        NoteDto.NoteDetailDto result = NoteDto.NoteDetailDto.builder()
                .noteId(note.getNoteId())
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
                .build();

        return result;
    }

    public NoteDto.likesResponseDto addLike(Long noteId, Long userId) {

    }

    public NoteDto.bookmarkResponseDto addBookmark(Long noteId, Long userId) {

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
