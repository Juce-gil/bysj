package com.kecheng.market.upload.controller;

import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import com.kecheng.market.upload.service.UploadService;
import com.kecheng.market.upload.vo.UploadImageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Upload")
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Operation(summary = "Upload goods images")
    @PostMapping("/goods-images")
    @RequireLogin
    public ApiResponse<List<UploadImageVo>> uploadGoodsImages(@RequestParam("files") MultipartFile[] files) {
        return ApiResponse.success(uploadService.uploadGoodsImages(UserContext.getUserId(), files));
    }
}