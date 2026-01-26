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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TripAiSuggestService {

    private static final Logger log =
            LoggerFactory.getLogger(TripAiSuggestService.class);

    // OpenAI SDK Client
    private final com.openai.client.OpenAIClient openAi;

    // JSON 파싱용 ObjectMapper (유연한 파싱 설정)
    private final ObjectMapper om = new ObjectMapper()
            .configure(
                    com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            )
            .configure(
                    com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS,
                    true
            );

    /**
     * AI 기반 여행 경유지 추천 메인 메서드
     */
    public List<SuggestedStopDTO> suggest(StopSuggestRequest req) {

        int days =
                (int) (ChronoUnit.DAYS.between(
                        req.getStartDate(),
                        req.getEndDate()
                ) + 1);

        // 1) 프롬프트 생성
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
                nz(req.getCity()),
                nz(req.getTheme()),
                req.getStartDate(),
                req.getEndDate(),
                req.getBudgetPerDay() == null
                        ? "80000"
                        : req.getBudgetPerDay().toString()
        );

        // 2) OpenAI Responses API 파라미터 구성
        var params =
                com.openai.models.responses.ResponseCreateParams.builder()
                        .model("gpt-4o-mini")
                        .input(prompt)
                        .build();

        try {
            // 3) API 호출
            var resp = openAi.responses().create(params);

            // 4) SDK 버전에 의존하지 않는 텍스트 추출
            String text = safeExtractText(resp);
            log.debug("AI raw text: {}", text);

            // 5) JSON 배열 부분만 추출
            String json = stripToJsonArray(text);
            log.debug("AI json {}", json);

            // 6) JSON → DTO 파싱
            List<SuggestedStopDTO> raw = parseJson(json);

            // 7) 서버단 검증 및 보정
            return sanitize(raw, days);

        } catch (Exception e) {
            // 실패 시 fallback
            log.warn("AI suggest failed, fallback, reason={}", e.toString());
            return fallbackRules(req);
        }
    }

    // ----------------- 헬퍼 메서드 -----------------

    /**
     * SDK 버전에 관계없이 텍스트를 최대한 안전하게 추출
     */
    private String safeExtractText(Object response) {
        try {
            var m = response.getClass().getMethod("outputText");
            Object v = m.invoke(response);
            if (v instanceof Optional<?> opt && opt.isPresent()) {
                return String.valueOf(opt.get());
            }
        } catch (Exception ignore) {}

        try {
            var output =
                    (List<?>) response.getClass().getMethod("output").invoke(response);
            if (output != null) {
                StringBuilder sb = new StringBuilder();
                for (Object item : output) {
                    try {
                        var content =
                                (List<?>) item.getClass().getMethod("content").invoke(item);
                        if (content != null) {
                            for (Object c : content) {
                                try {
                                    Object text =
                                            c.getClass().getMethod("text").invoke(c);
                                    if (text != null) {
                                        Object val =
                                                text.getClass().getMethod("value").invoke(text);
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

        return "";
    }

    /**
     * 코드 블록/설명 제거 후 순수 JSON 배열만 추출
     */
    private String stripToJsonArray(String text) {
        if (text == null) return "[]";
        String t = text.trim();

        if (t.startsWith("```")) {
            int first = t.indexOf('\n');
            int last = t.lastIndexOf("```");
            if (first >= 0 && last > first) {
                t = t.substring(first + 1, last).trim();
            }
        }

        int start = t.indexOf('[');
        int end = t.lastIndexOf(']');
        if (start >= 0 && end > start) {
            t = t.substring(start, end + 1);
        }
        return t;
    }

    /**
     * JSON 문자열을 SuggestedStopDTO 리스트로 변환
     */
    private List<SuggestedStopDTO> parseJson(String json) throws IOException {
        if (json == null || json.isBlank()) return List.of();
        JavaType t = om.getTypeFactory()
                .constructCollectionType(List.class, SuggestedStopDTO.class);
        return om.readValue(json, t);
    }

    /**
     * AI 응답값 검증 및 보정
     */
    private List<SuggestedStopDTO> sanitize(List<SuggestedStopDTO> in, int days) {
        if (in == null) return List.of();
        return in.stream().map(s -> {
            int d = s.dayOrder() == null
                    ? 1
                    : Math.min(Math.max(1, s.dayOrder()), days);
            BigDecimal cost =
                    s.cost() == null
                            ? BigDecimal.ZERO
                            : s.cost().max(BigDecimal.ZERO);
            TripCostCategory cat =
                    s.category() == null
                            ? TripCostCategory.OTHER
                            : s.category();
            return new SuggestedStopDTO(
                    d,
                    nz(s.placeName()),
                    s.address(),
                    nz(s.memo()),
                    cost,
                    cat
            );
        }).toList();
    }

    private static String nz(String s) {
        return (s == null) ? "" : s.trim();
    }

    /**
     * AI 실패 시 fallback 규칙
     */
    private List<SuggestedStopDTO> fallbackRules(StopSuggestRequest req) {
        // <-- 기존 더미 로직 그대로 유지
        return List.of();
    }
}

/**
 * 역할
여행 일정 기반 경유지 AI 추천
OpenAI Responses API 연동
응답 검증 및 보정

 * 핵심 설계 의도
AI 응답은 항상 불완전하거나 예측 불가능하다고 가정하고 설계했습니다.
JSON만 반환하도록 프롬프트 강제
SDK 버전 차이를 고려한 텍스트 추출
잘못된 값(dayOrder, cost, category) 서버단 보정
실패 시 fallback 로직 제공
 */