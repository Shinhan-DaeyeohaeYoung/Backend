package com.joeun.global.file;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /** mock | s3 | local */
    private String storage = "mock";

    private final S3 s3 = new S3();
    public static class S3 {
        private String bucket;
        private String region;
        private Integer presignExpirySeconds = 900;
        private String publicBaseUrl = "";

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public Integer getPresignExpirySeconds() { return presignExpirySeconds; }
        public void setPresignExpirySeconds(Integer v) { this.presignExpirySeconds = v; }
        public String getPublicBaseUrl() { return publicBaseUrl; }
        public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    }

    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public S3 getS3() { return s3; }
}
