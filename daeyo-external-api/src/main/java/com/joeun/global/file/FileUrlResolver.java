package com.joeun.global.file;

import org.springframework.stereotype.Component;

@Component
public class FileUrlResolver {
    private final FileProperties props;

    public FileUrlResolver(FileProperties props) {
        this.props = props;
    }
    public String toPublicUrl(String key) {
        if (key == null || key.isBlank()) return null;
        if (key.startsWith("http://") || key.startsWith("https://")) return key;

        String storage = props.getStorage();          // ← 이제 컴파일 OK
        if ("mock".equalsIgnoreCase(storage)) {
            String k = key.startsWith("/") ? key.substring(1) : key;
            // 서버 포트는 너 서비스 포트에 맞춰 조정
            return "http://localhost:8082/mock/" + k;
        }

        if ("s3".equalsIgnoreCase(storage)) {
            // 아직 S3 미구현이면 예외 대신 임시 URL로 우회하거나, 명확히 예외 던지기
            throw new IllegalStateException("S3 presign 미구현: key=" + key);
            // 혹은 임시로:
            // String cdn = "https://example-cdn";
            // return cdn + (key.startsWith("/") ? key : "/" + key);
        }

        // local 등 기타
        return "/files/" + (key.startsWith("/") ? key.substring(1) : key);
    }
}
