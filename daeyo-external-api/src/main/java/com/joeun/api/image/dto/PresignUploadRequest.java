package com.joeun.api.image.dto;

import com.joeun.common.ImageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignUploadRequest(
        @NotNull ImageType imageType,   // ITEM 또는 UNIT
        @NotBlank String fileName       // 예: "foo.jpg"
) {}