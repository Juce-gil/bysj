package com.kecheng.market.upload.vo;

public record UploadImageVo(
        String url,
        String originalName,
        String fileName,
        Long size
) {
}