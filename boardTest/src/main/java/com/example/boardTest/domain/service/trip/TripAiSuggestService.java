package com.example.boardTest.domain.service.trip;

import com.example.boardTest.domain.entity.trip.TripCostCategory;
import com.example.boardTest.domain.dto.trip.StopSuggestRequest;
import com.example.boardTest.domain.dto.trip.SuggestedStopDTO;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripAiSuggestService {

    private static final Logger log = LoggerFactory.getLogger(TripAiSuggestService.class);
    private final com.openai.client.OpenAIClient openAi;
    private final ObjectMapper om = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);

    public List<SuggestedStopDTO> suggest(StopSuggestRequest req) {
        int days = (int)(ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1);

        // 1) 프롬프트
        String prompt = """
            You are a Korean travel planner.
            Propose 2 stops per day and RETURN ONLY a JSON array (no markdown, no explanations).
            Keys:
            - dayOrder: int in [1,%d]
            - placeName: string
            - address: string|null
            - memo: string
            - cost: number (KRW, >=0)
            - category: one of {TRANSPORT, LODGING, FOOD, SIGHTSEEING, SHOPPING, OTHER}

            Trip:
            - City: %s
            - Theme: %s
            - Dates: %s ~ %s
            - BudgetPerDay(₩): %s
            """.formatted(
                days,
                nz(req.getCity()), nz(req.getTheme()),
                req.getStartDate(), req.getEndDate(),
                req.getBudgetPerDay() == null ? "80000" : req.getBudgetPerDay().toString()
        );

        // 2) Responses API 호출
        var params = com.openai.models.responses.ResponseCreateParams.builder()
                .model("gpt-4o-mini")  // 문자열로 지정
                .input(prompt)
                .build();

        try {
            var resp = openAi.responses().create(params);

            // 3) SDK 버전 차이를 흡수하는 안전 추출
            String text = safeExtractText(resp);
            log.debug("AI raw text: {}", text);
            // 4) 혹시 코드블록/설명 섞여도 JSON 배열만 뽑아내기
            String json = stripToJsonArray(text);
            log.debug("AI json {}", json);
            // 5) JSON → DTO
            List<SuggestedStopDTO> raw = parseJson(json);

            // 6) 서버 검증/보정
            return sanitize(raw, days);

        } catch (Exception e) {
            // 실패 시 더미로 폴백
            log.warn("AI suggest failed, fallback, reason={}", e.toString());
            return fallbackRules(req);
        }
    }

    // ----------------- 헬퍼들 -----------------

    /** SDK 버전에 상관없이 텍스트를 최대한 안전하게 추출 */
    private String safeExtractText(Object response) {
        // 1) outputText(): Optional<String>
        try {
            var m = response.getClass().getMethod("outputText");
            Object v = m.invoke(response);
            if (v instanceof java.util.Optional<?> opt && opt.isPresent()) {
                return String.valueOf(opt.get());
            }
        } catch (Exception ignore) {}

        // 2) output(): List<...> → 각 item.content().text().value()
        try {
            var output = (java.util.List<?>) response.getClass().getMethod("output").invoke(response);
            if (output != null) {
                StringBuilder sb = new StringBuilder();
                for (Object item : output) {
                    try {
                        var content = (java.util.List<?>) item.getClass().getMethod("content").invoke(item);
                        if (content != null) {
                            for (Object c : content) {
                                try {
                                    Object text = c.getClass().getMethod("text").invoke(c);
                                    if (text != null) {
                                        Object val = text.getClass().getMethod("value").invoke(text);
                                        if (val != null) sb.append(val.toString());
                                    }
                                } catch (Exception ignore2) {}
                            }
                        }
                    } catch (Exception ignore2) {}
                }
                return sb.toString();
            }
        } catch (Exception ignore) {}

        // 3) 실패 시 빈 문자열
        return "";
    }

    /** 백틱/설명 제거하고 순수 JSON 배열만 추출 */
    private String stripToJsonArray(String text) {
        if (text == null) return "[]";
        String t = text.trim();

        // 코드펜스 제거 ```json ... ```
        if (t.startsWith("```")) {
            int first = t.indexOf('\n');
            int last = t.lastIndexOf("```");
            if (first >= 0 && last > first) {
                t = t.substring(first + 1, last).trim();
            }
        }

        // 배열 경계만 남기기
        int start = t.indexOf('[');
        int end = t.lastIndexOf(']');
        if (start >= 0 && end > start) {
            t = t.substring(start, end + 1);
        }
        return t;
    }

    private List<SuggestedStopDTO> parseJson(String json) throws IOException {
        if (json == null || json.isBlank()) return List.of();
        JavaType t = om.getTypeFactory()
                .constructCollectionType(List.class, SuggestedStopDTO.class);
        return om.readValue(json, t);
    }

    private List<SuggestedStopDTO> sanitize(List<SuggestedStopDTO> in, int days) {
        if (in == null) return List.of();
        return in.stream().map(s -> {
            int d = s.dayOrder() == null ? 1 : Math.min(Math.max(1, s.dayOrder()), days);
            BigDecimal cost = s.cost() == null ? BigDecimal.ZERO : s.cost().max(BigDecimal.ZERO);
            TripCostCategory cat = s.category() == null ? TripCostCategory.OTHER : s.category();
            return new SuggestedStopDTO(d, nz(s.placeName()), s.address(), nz(s.memo()), cost, cat);
        }).toList();
    }

    private static String nz(String s){ return (s==null) ? "" : s.trim(); }

    private List<SuggestedStopDTO> fallbackRules(StopSuggestRequest req) {
        // <-- 여기에 기존 더미 로직 그대로 두세요
        return List.of();
    }
}
