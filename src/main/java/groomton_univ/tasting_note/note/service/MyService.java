package groomton_univ.tasting_note.note.service;

import com.google.cloud.storage.*;
import groomton_univ.tasting_note.entity.NoteTagEntity;
import groomton_univ.tasting_note.note.dto.NoteDto;
import groomton_univ.tasting_note.entity.NoteEntity;
import groomton_univ.tasting_note.note.dto.NoteUpdateMetaDto;
import groomton_univ.tasting_note.note.repository.BookmarkRepository;
import groomton_univ.tasting_note.note.repository.LikeRepository;
import groomton_univ.tasting_note.note.repository.NoteRepository;
import groomton_univ.tasting_note.note.repository.NoteTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MyService {

    private final NoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    private final Storage storage;
    private final NoteTagRepository noteTagRepository;

    @Value("${gcp.storage.bucket.name}")
    private String bucketName;

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

    public void uploadImage(Long noteId, MultipartFile file) {
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

        NoteEntity note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        String oldUrl = note.getPhoto();
        note.setPhoto(publicUrl);
        noteRepository.save(note);

        // old 사진 삭제 전략
        try {
            if (oldUrl != null) {
                String prefix = String.format("https://storage.googleapis.com/%s/", bucketName);
                String oldObject = oldUrl.substring(prefix.length());

                storage.delete(BlobId.of(bucketName, oldObject));
            }
        } catch(Exception ignored) {}
    }

    @Transactional
    public NoteDto.NoteIdDto updateNote(Long userId, MultipartFile file, NoteUpdateMetaDto meta) {
        Long noteId = meta.getNoteId();
        NoteEntity note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        if (!note.getUser().getKakaoId().equals(userId)) {
            throw new IllegalArgumentException("본인 노트만 수정할 수 있습니다.");
        }
        if (file != null && !file.isEmpty()) {
            uploadImage(noteId, file);
        }

        // 노트 태그의 항목 수정 필요
        List<NoteDto.NoteTagDto> metaTags = meta.getTags();
        if (metaTags == null || metaTags.size() != 5) {
            throw new IllegalArgumentException("태그 5개가 전체 필요합니다.");
        }

        noteTagRepository.deleteByNote(note);

        List<NoteTagEntity> adds = metaTags.stream().map(d -> {
            NoteTagEntity tag = new NoteTagEntity();
            tag.setNote(note);
            tag.setName(d.getName());
            tag.setValue(d.getValue());
            return tag;
        }).toList();
        noteTagRepository.saveAll(adds);

        // 노트의 나머지 항목 수정 완
        note.setCategory(meta.getCategory());
        note.setCategoryStyle(meta.getCategoryStyle());
        note.setDegree(meta.getDegree());
        note.setName(meta.getName());
        note.setRating(meta.getRating());
        note.setContent(meta.getContent());
        note.setDate(meta.getDate());
        noteRepository.save(note);

        NoteDto.NoteIdDto result = new NoteDto.NoteIdDto();
        result.setNoteId(noteId);
        return result;
    }

    @Transactional
    public void deleteNote(Long userId, Long noteId) {
        NoteEntity note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        if (!note.getUser().getKakaoId().equals(userId)) {
            throw new IllegalArgumentException("본인 노트만 삭제할 수 있습니다.");
        }
        noteRepository.delete(note);
    }
}
