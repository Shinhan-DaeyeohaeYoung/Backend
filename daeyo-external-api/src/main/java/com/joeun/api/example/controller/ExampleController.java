package com.joeun.api.example.controller;

import com.joeun.api.example.dto.ExampleDto;
import com.joeun.common.ImageType;
import com.joeun.common.PresignResponse;
import com.joeun.infra.aws.s3.service.ImageInfraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/examples")
public class ExampleController {

    private final ImageInfraService imageInfraService;

    @PostMapping
    public PresignResponse createExample() {
        return imageInfraService.getUploadPresignedUrl(ImageType.ITEM, 1L, "example.png");
    }

    @PostMapping("/download")
    public String getExample(@RequestBody ExampleDto key) {
        return imageInfraService.getDownloadPresignedUrl(key.key());
    }
}
