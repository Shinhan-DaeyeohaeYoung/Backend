package com.joeun.infra.aws.s3.service;

import com.joeun.common.ImageType;
import com.joeun.common.PresignResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;


import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3ImageInfraService implements ImageInfraService {

    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.s3.bucket.name}")
    private String bucketName;

    public PresignResponse getUploadPresignedUrl(
            ImageType imageType, Long userId, String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        String baseName;
        String ext;
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            ext = fileName.substring(dotIndex + 1);
        } else {
            baseName = fileName;
            ext = "";
        }

        String contentType = getContentType(ext);

        String key = generateKey(imageType, userId, baseName, ext);

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putObjectRequest)
                        .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return PresignResponse.builder().url(presigned.url().toString()).key(key).build();
    }

    public String getDownloadPresignedUrl(String key) {

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder().bucket(bucketName).key(key).build();

        GetObjectPresignRequest getObjectPresignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presigned.url().toString();
    }

    private String generateKey(ImageType imageType, Long userId, String baseName, String ext) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString();

        return String.format(
                "%s/%s/%d-%s-%s.%s", imageType.getType(), datePath, userId, uuid, baseName, ext);
    }

    private String getContentType(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}

