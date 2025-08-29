package com.joeun.api.vision.yolo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class HttpYoloClient implements YoloClient {
    private final WebClient webClient;
    public HttpYoloClient(@Value("${vision.yolo.base-url}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }
    @Override
    public DetectCropResponse detectCrop(DetectCropRequest req) {
        return webClient.post()
                .uri("/detect-crop")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "imagePresignedUrl", req.imagePresignedUrl(),
                        "targetS3Prefix", req.targetS3Prefix(),
                        "padRatio", req.padRatio(),
                        "topk", req.topk(),
                        "minConf", req.minConf(),
                        "outputSize", req.outputSize()
                ))
                .retrieve()
                .bodyToMono(DetectCropResponse.class)
                .block();
    }
}
