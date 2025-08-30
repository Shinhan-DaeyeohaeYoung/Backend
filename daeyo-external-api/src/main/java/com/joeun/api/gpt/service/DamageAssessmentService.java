package com.joeun.api.gpt.service;

import com.joeun.api.gpt.agent.ImageThresholdingAgent;
import com.joeun.api.gpt.dto.DamageSuggestionResult;
import com.joeun.service.returnrequest.ReturnRequestQueryService;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

// DamageAssessmentService.java (핵심만)
@Service
@RequiredArgsConstructor
public class DamageAssessmentService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient openai; // baseUrl=https://api.openai.com, Auth 헤더 세팅된 빈
    private final ResourceLoader resourceLoader; // mock 경로 파일 읽을 때 사용(선택)
    private final ReturnRequestQueryService queryService;
    private final ImageThresholdingAgent imageAgent;

    private String toDataUrl(byte[] bytes, String mimeType) {
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mimeType + ";base64," + base64;
    }

    public DamageSuggestionResult assess(Long returnRequestId) throws IOException {
        ReturnRequestQueryService.BeforeAfterKeys keys = queryService.getBeforeAfterKeys(returnRequestId);

        String beforeKey = keys.beforeKey();
        String afterKey  = keys.afterKey();

        Response beforeRes;
        Response afterRes;

        try {
            beforeRes = imageAgent.fetchProcessedFromS3(beforeKey);
            afterRes  = imageAgent.fetchProcessedFromS3(afterKey);
        } catch (Exception e) {
            throw new RuntimeException("Image processing failed: " + e.getMessage());
        }

        // 1) 바이트 → data URL
        String beforeImgDataUrl = toDataUrl(beforeRes, "image/jpeg"); // 헤더에서 MIME 추정도 가능
        String afterImgDataUrl  = toDataUrl(afterRes,  "image/jpeg");

        // 2) Responses API 페이로드
        Map<String, Object> body = Map.of(
                "model", "gpt-4.1-mini",
                "input", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "input_text", "text",
                                        "두 이미지(수거 전/후)를 비교해 아래 형식의 JSON만 출력해.\n" +
                                                "설명은 한국어로. 파손율은 0~100 사이 숫자(소수 허용).\n" +
                                                "{ \"detail\": \"상세 설명\", \"damageRate\": number, \"summary\": \"요약\" }"
                                ),
                                Map.of("type", "input_image", "image_url", beforeImgDataUrl),
                                Map.of("type", "input_image", "image_url", afterImgDataUrl)
                        )
                )),
                // 아래 포맷 섹션은 당신 코드 그대로 유지
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "DamageSummary",
                                "strict", true,
                                "schema", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "required", List.of("detail", "damageRate", "summary"),
                                        "properties", Map.of(
                                                "detail", Map.of("type", "string", "maxLength", 300),
                                                "damageRate", Map.of("type", "number", "minimum", 0, "maximum", 100),
                                                "summary", Map.of("type", "string", "maxLength", 150)
                                        )
                                )
                        )
                ),
                "temperature", 0.2,
                "max_output_tokens", 400
        );

        String respJson = openai.post()
                .uri("/v1/responses")
                .bodyValue(body)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        r -> r.bodyToMono(String.class).map(msg -> new RuntimeException("OpenAI error: " + msg)))
                .bodyToMono(String.class)
                .block();

        return parse(respJson);
    }

    private static String toDataUrl(Response feignRes, String defaultMime) throws IOException {
        // Content-Type 헤더에서 MIME 추정 (없으면 기본값 사용)
        String mime = feignRes.headers().getOrDefault("Content-Type", List.of(defaultMime))
                .stream().findFirst().orElse(defaultMime);
        if (mime == null || !mime.startsWith("image/")) mime = defaultMime != null ? defaultMime : "image/jpeg";

        try (var in = feignRes.body().asInputStream()) {
            byte[] bytes = in.readAllBytes();
            String b64 = Base64.getEncoder().encodeToString(bytes);
            return "data:" + mime + ";base64," + b64; // ★ data URL 완성
        }
    }

//    private byte[] readBytes(String url) {
//        // mock이면 로컬에서 읽고, 절대 URL(https)면 HTTP로 가져오거나 스킵
//        // 여기서는 mock만 가정 (필요 시 분기)
//        try {
//            if (url.startsWith("http://localhost:8082/mock/")) {
//                String cp = "classpath:/mock/" + url.substring(url.indexOf("/mock/") + 6);
//                Resource res = resourceLoader.getResource(cp);
//                return res.getContentAsByteArray();
//            }
//            throw new IllegalArgumentException("Only mock supported in this helper: " + url);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private byte[] readBytes(String url) {
        try {
            if (url.startsWith("http://localhost:8082/mock/")) {
                String rel = url.substring(url.indexOf("/mock/") + 6); // univ/1/items/...
                Resource res = resourceLoader.getResource("classpath:/mock/" + rel);
                if (!res.exists()) {
                    // 없으면 공용 샘플로 대체
                    res = resourceLoader.getResource("classpath:/mock/sample.jpg");
                }
                return res.getContentAsByteArray();
            }
            // (선택) http(s)면 다운로드해서 넘기기
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return WebClient.create().get().uri(url).retrieve().bodyToMono(byte[].class).block();
            }
            throw new IllegalArgumentException("Unsupported URL: " + url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 파서 교체 (output_json 우선, 그 외 텍스트 JSON도 처리)
    private DamageSuggestionResult parse(String respJson) {
        try {
            JsonNode root = mapper.readTree(respJson);
            JsonNode output = root.path("output");
            if (output.isArray() && output.size() > 0) {
                JsonNode content = output.get(0).path("content");
                if (content.isArray()) {
                    // 1) output_json 우선
                    for (JsonNode c : content) {
                        if ("output_json".equals(c.path("type").asText("")) && c.has("json")) {
                            return toResult(c.get("json"));
                        }
                    }
                    // 2) 텍스트가 JSON 문자열이면 파싱
                    for (JsonNode c : content) {
                        String txt = c.path("text").asText("");
                        if (!txt.isBlank()) {
                            try {
                                JsonNode maybe = mapper.readTree(txt);
                                if (maybe.isObject()) return toResult(maybe);
                            } catch (Exception ignore) { /* not json */ }
                            // 3) 텍스트를 그대로 detail/summary로
                            return new DamageSuggestionResult(txt, 0.0, txt);
                        }
                    }
                }
            }
            // 4) 상단 편의 필드
            String outputText = root.path("output_text").asText("");
            if (!outputText.isBlank()) {
                try {
                    JsonNode maybe = mapper.readTree(outputText);
                    if (maybe.isObject()) return toResult(maybe);
                } catch (Exception ignore) { /* not json */ }
                return new DamageSuggestionResult(outputText, 0.0, outputText);
            }
            // 5) 최후 수단
            return new DamageSuggestionResult("", 0.0, "");
        } catch (Exception e) {
            return new DamageSuggestionResult("", 0.0, "");
        }
    }

    private DamageSuggestionResult toResult(JsonNode json) {
        String detail = json.path("detail").asText("");
        double rate;
        JsonNode r = json.path("damageRate");
        if (r.isNumber()) rate = r.asDouble();
        else {
            try {
                rate = Double.parseDouble(r.asText("0"));
            } catch (Exception ignore) {
                rate = 0.0;
            }
        }
        rate = Math.max(0.0, Math.min(100.0, rate));
        String summary = json.path("summary").asText("");
        return new DamageSuggestionResult(detail, rate, summary);
    }

    private boolean looksLikeJson(String s) {
        String t = s.trim();
        return (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"));
    }

}