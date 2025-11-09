package com.example.boardTest.service.trip;

import com.example.boardTest.domain.trip.TripCostCategory;
import com.example.boardTest.dto.trip.StopSuggestRequest;
import com.example.boardTest.dto.trip.SuggestedStopDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripAiSuggestService {

    // 간단 키워드-카테고리 매핑
    private TripCostCategory guessCategory(String place) {
        String p = place.toLowerCase();
        if (p.contains("역") || p.contains("공항") || p.contains("station")) return TripCostCategory.TRANSPORT;
        if (p.contains("성") || p.contains("castle") || p.contains("museum")) return TripCostCategory.SIGHTSEEING;
        if (p.contains("맛집") || p.contains("식당") || p.contains("dining")) return TripCostCategory.FOOD;
        if (p.contains("쇼핑") || p.contains("몰") || p.contains("mall")) return TripCostCategory.SHOPPING;
        if (p.contains("호텔") || p.contains("inn") || p.contains("stay")) return TripCostCategory.LODGING;
        return TripCostCategory.OTHER;
    }

    public List<SuggestedStopDTO> suggest(StopSuggestRequest req) {
        int days = (int)(ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1);
        int budget = (req.getBudgetPerDay() != null ? req.getBudgetPerDay() : 80000);

        // 더미 후보(도시/테마에 맞춰 간략 생성)
        String city = (req.getCity() == null || req.getCity().isBlank()) ? "Osaka" : req.getCity().trim();
        String theme = (req.getTheme() == null ? "" : req.getTheme().toLowerCase());

        List<String[]> pool = new ArrayList<>();
        if (city.toLowerCase().contains("osaka")) {
            pool.add(new String[]{"도톤보리", "신사이바시 상점가"});
            pool.add(new String[]{"오사카성", "우메다 스카이빌딩"});
            pool.add(new String[]{"난바 파크스", "구로몬 시장"});
        } else {
            // fallback
            pool.add(new String[]{"대표 명소 A", "현지 식당 A"});
            pool.add(new String[]{"대표 명소 B", "현지 식당 B"});
            pool.add(new String[]{"대표 명소 C", "현지 식당 C"});
        }
        if (theme.contains("food")) {
            pool.forEach(arr -> arr[1] = arr[1] + " (현지 맛집)");
        } else if (theme.contains("night")) {
            pool.forEach(arr -> arr[0] = arr[0] + " (야경)");
        }

        List<SuggestedStopDTO> out = new ArrayList<>();
        for (int d = 1; d <= days; d++) {
            String[] picks = pool.get((d-1) % pool.size());
            // 1일 2개 권장(명소+식당)
            String a = picks[0], b = picks[1];
            out.add(new SuggestedStopDTO(
                    d, a, null, "사진 스팟 / 인생샷", BigDecimal.valueOf(Math.round(budget * 0.15)), guessCategory(a)));
            out.add(new SuggestedStopDTO(
                    d, b, null, "현지 인기 메뉴", BigDecimal.valueOf(Math.round(budget * 0.18)), guessCategory(b)));
        }
        return out;
    }
}
