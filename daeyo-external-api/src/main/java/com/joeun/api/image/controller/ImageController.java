package com.joeun.api.image.controller;


import com.joeun.common.ImageType;
import com.joeun.common.PresignResponse;
import com.joeun.infra.aws.s3.service.ImageInfraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Image")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageInfraService imageInfraService;

    @Operation(summary = "업로드용 Presigned URL 발급")
    @PostMapping("/presign/upload")
    public PresignResponse presignUpload(
            @Valid @RequestBody com.joeun.api.image.dto.PresignUploadRequest body
//            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        Long userId = 1L;
        return imageInfraService.getUploadPresignedUrl(
                body.imageType(), userId, body.fileName()
        );
    }

    @Operation(summary = "다운로드용 Presigned URL 발급")
    @GetMapping("/presign/download")
    public String presignDownload(@RequestParam String key) {
        return imageInfraService.getDownloadPresignedUrl(key);
    }
}