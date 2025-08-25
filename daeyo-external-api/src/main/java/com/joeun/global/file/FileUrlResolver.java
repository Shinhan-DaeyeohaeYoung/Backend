package com.joeun.global.file;

import org.springframework.stereotype.Component;

@Component
public class FileUrlResolver {
    public String toPublicUrl(String key) {
        // S3 presign 준비되면 여기에 presign 구현
        if (key == null) throw new IllegalArgumentException("image key is null");
        if (key.startsWith("http://") || key.startsWith("https://")) return key;
        throw new IllegalStateException("S3 presign 미구현: key=" + key);
    }
}
