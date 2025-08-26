package com.joeun.api.gpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeun.api.gpt.dto.DamageSuggestionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

// com.joeun.api.gpt.service.DamageAssessmentService
@Service
@RequiredArgsConstructor
public class DamageAssessmentService {
    private final WebClient openAiWebClient;
    private final ObjectMapper om = new ObjectMapper();

    public DamageSuggestionResult assess(String beforeUrl, String afterUrl) {
        var body = """
        {
          "model": "gpt-4o-mini",
          "input": [
            {
              "role": "system",
              "content": [
                {"type":"text","text":"You are an expert inspector for rental returns. Compare BEFORE and AFTER photos of the SAME item. Output strictly valid JSON for the given schema."}
              ]
            },
            {
              "role": "user",
              "content": [
                {"type":"text","text":"Assess damage. Ignore lighting/angle unless they indicate damage. Respond JSON only."},
                {"type":"input_image","image_url":"%s","detail":"auto"},
                {"type":"input_image","image_url":"%s","detail":"auto"}
              ]
            }
          ],
          "response_format": {
            "type": "json_schema",
            "json_schema": {
              "name": "DamageReport",
              "schema": {
                "type": "object",
                "properties": {
                  "damage_rate": {"type":"number","minimum":0,"maximum":1},
                  "verdict": {"type":"string","enum":["NO_DAMAGE","MINOR","MODERATE","SEVERE"]},
                  "observations": {"type":"array","items":{"type":"string"}},
                  "suggested_action": {"type":"string"}
                },
                "required": ["damage_rate","verdict"],
                "additionalProperties": false
              },
              "strict": true
            }
          },
          "temperature": 0.1,
          "max_output_tokens": 400
        }
        """.formatted(beforeUrl, afterUrl);

        String raw = openAiWebClient.post()
                .uri("/responses")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = om.readTree(raw);
            // Responses API: output[0].content[0].text 에 JSON string 있다고 가정
            String jsonText = root.path("output").get(0).path("content").get(0).path("text").asText();
            JsonNode obj = om.readTree(jsonText);

            double rate = obj.path("damage_rate").asDouble(0.0);
            String verdict = obj.path("verdict").asText("NO_DAMAGE");

            List<String> obs = obj.has("observations") && obj.get("observations").isArray()
                    ? om.convertValue(obj.get("observations"),
                    om.getTypeFactory().constructCollectionType(List.class, String.class))
                    : List.of();

            String action = obj.path("suggested_action").asText("");

            return new DamageSuggestionResult(rate, verdict, obs, action);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }
}
