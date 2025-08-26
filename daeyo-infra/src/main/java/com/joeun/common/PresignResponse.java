package com.joeun.common;

import lombok.Builder;

@Builder
public record PresignResponse(String key, String url) {}

