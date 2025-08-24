package groomton_univ.tasting_note.user.util;

import groomton_univ.tasting_note.entity.UserTagEntity;
import groomton_univ.tasting_note.user.repository.UserTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserTagRepository userTagRepository;

    @Override
    public void run(String... args) throws Exception {
        // LinkedHashMap을 사용하여 카테고리와 태그 순서를 UI와 동일하게 유지
        Map<String, List<String>> tagData = new LinkedHashMap<>();

        // Q1: 음료 종류
        tagData.put("음료 종류", List.of(
                "커피", "차", "위스키", "와인", "기타"
        ));

        // Q2: 맛/향
        tagData.put("맛/향", List.of(
                "달콤하고 부드러운 맛", "신선하고 상큼한 맛", "진하고 묵직한 맛",
                "씁쓸하고 드라이한 맛", "향이 강렬하고 개성 있는 맛"
        ));

        // Q3: 농도/강도
        tagData.put("농도/강도", List.of(
                "연하고 가벼운 스타일", "중간 정도의 균형 잡힌 스타일", "진하고 강한 스타일"
        ));

        // Q4: 상황
        tagData.put("상황", List.of(
                "아침 시작할 때", "식사와 함께", "휴식/독서/작업 시간에",
                "모임/파티/친구와 함께", "특별한 날이나 기념일에"
        ));

        // 각 카테고리와 태그를 순회하며 DB에 저장
        for (Map.Entry<String, List<String>> entry : tagData.entrySet()) {
            String category = entry.getKey();
            List<String> tagNames = entry.getValue();

            for (String name : tagNames) {
                // 이미 존재하는 태그인지 이름으로 확인
                if (userTagRepository.findByName(name).isEmpty()) {
                    UserTagEntity newTag = new UserTagEntity();
                    newTag.setName(name);
                    newTag.setCategory(category); // 카테고리 설정
                    userTagRepository.save(newTag);
                }
            }
        }
    }
}