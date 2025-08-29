package com.joeun.api.vision.yolo;

public interface YoloClient {

    DetectCropResponse detectCrop(DetectCropRequest req);

    record DetectCropRequest(
            String imagePresignedUrl,
            String targetS3Prefix,
            Double padRatio,
            Integer topk,
            Double minConf,
            Integer outputSize
    ) {}
    record DetectCropResponse(
            String cropKey,
            String detectionMetaJson
    ) {}


}
