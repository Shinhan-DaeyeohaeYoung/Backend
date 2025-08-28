package com.joeun.api.item.dto;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements());
    }
    public static <T, R> PageResponse<R> map(Page<T> p, Function<T, R> mapper) {
        return new PageResponse<>(p.map(mapper).getContent(), p.getNumber(), p.getSize(), p.getTotalElements());
    }
}
