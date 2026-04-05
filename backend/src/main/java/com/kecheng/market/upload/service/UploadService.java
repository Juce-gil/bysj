package com.kecheng.market.upload.service;

import com.kecheng.market.upload.vo.UploadImageVo;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    List<UploadImageVo> uploadGoodsImages(Long userId, MultipartFile[] files);
}