package groomton_univ.tasting_note.note.repository;

import groomton_univ.tasting_note.entity.NoteEntity;

import java.util.List;

public interface NoteRepositoryCustom {
    List<NoteEntity> searchNotes(String category, String degree,Integer days, List<String> tags);
}
