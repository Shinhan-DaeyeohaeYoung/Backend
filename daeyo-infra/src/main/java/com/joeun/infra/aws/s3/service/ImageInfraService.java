package com.joeun.infra.aws.s3.service;


import com.joeun.common.ImageType;
import com.joeun.common.PresignResponse;

public interface ImageInfraService {

    public PresignResponse getUploadPresignedUrl(ImageType imageType, Long userId, String fileName);

    public String getDownloadPresignedUrl(String key);
}


