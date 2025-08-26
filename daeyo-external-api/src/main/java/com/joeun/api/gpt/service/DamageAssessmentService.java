package com.joeun.api.gpt.service;

import com.joeun.api.gpt.dto.DamageSuggestionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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

    private String toDataUrl(byte[] bytes, String mimeType) {
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mimeType + ";base64," + base64;
    }
    public DamageSuggestionResult assess(String beforeUrl, String afterUrl) {
        // 1) mock URL이면 파일을 읽어 base64로
        String beforeImg = toDataUrl(readBytes(beforeUrl), "image/jpeg");
        String afterImg = toDataUrl(readBytes(afterUrl), "image/jpeg");

        // 2) Responses API 페이로드 구성 (이미지 2장 + 안내 텍스트)
        Map<String, Object> body = Map.of(
                "model", "gpt-4.1-mini", // 사용 모델
                "input", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "input_text", "text", "두 이미지(수거 전/후)를 비교해서 파손 가능성과 파손률을 설명하고, 보수/청소/부품교체 중 무엇이 필요한지 제안해줘."),
                                Map.of("type", "input_image", "image_url", beforeImg),
                                Map.of("type", "input_image", "image_url", afterImg)
                        )
                ))
        );

        String respJson = openai.post()
                .uri("/v1/responses")
                .bodyValue(body)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        r -> r.bodyToMono(String.class).map(msg -> new RuntimeException("OpenAI error: " + msg)))
                .bodyToMono(String.class)
                .block();

        // TODO: respJson 파싱해서 DamageSuggestionResult로 매핑
        return parse(respJson);
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

    private DamageSuggestionResult parse(String respJson) {
        try {
            JsonNode root = mapper.readTree(respJson);

            String text = "";
            if (root.has("output") && root.get("output").isArray()) {
                JsonNode outputArr = root.get("output");
                if (!outputArr.isEmpty()) {
                    JsonNode contentArr = outputArr.get(0).path("content");
                    if (contentArr.isArray() && !contentArr.isEmpty()) {
                        text = contentArr.get(0).path("text").asText("");
                    }
                }
            }

            return new DamageSuggestionResult(text);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + respJson, e);
        }
    }

}