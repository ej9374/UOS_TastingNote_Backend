package groomton_univ.tasting_note.user.util;

import groomton_univ.tasting_note.entity.UserTagEntity;
import groomton_univ.tasting_note.user.repository.UserTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserTagRepository userTagRepository;

    @Override
    public void run(String... args) throws Exception {
        // "Tag1"부터 "Tag20"까지의 태그 목록 생성
        List<String> tagNames = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            tagNames.add("Tag" + i);
        }

        for (String name : tagNames) {
            // 이미 존재하는 태그인지 확인
            if (userTagRepository.findByName(name).isEmpty()) {
                UserTagEntity newTag = new UserTagEntity();
                newTag.setName(name);
                userTagRepository.save(newTag);
            }
        }
    }
}