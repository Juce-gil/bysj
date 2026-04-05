package com.kecheng.market.upload.service.impl;

import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.upload.service.UploadService;
import com.kecheng.market.upload.vo.UploadImageVo;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalUploadService implements UploadService {

    private static final int MAX_IMAGE_COUNT = 6;
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Path storageRoot;

    public LocalUploadService(@Value("${market.upload.base-dir:upload}") String baseDir) {
        this.storageRoot = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public List<UploadImageVo> uploadGoodsImages(Long userId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(400, "请至少选择一张图片");
        }
        if (files.length > MAX_IMAGE_COUNT) {
            throw new BusinessException(400, "单次最多上传 6 张图片");
        }

        try {
            Files.createDirectories(storageRoot);
        } catch (IOException exception) {
            throw new BusinessException(500, "上传目录初始化失败");
        }

        Path targetDirectory = storageRoot.resolve(Paths.get("goods", LocalDate.now().format(DATE_FORMATTER))).normalize();
        try {
            Files.createDirectories(targetDirectory);
        } catch (IOException exception) {
            throw new BusinessException(500, "创建图片目录失败");
        }

        List<UploadImageVo> uploadedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            String originalName = StringUtils.hasText(file.getOriginalFilename())
                    ? Path.of(file.getOriginalFilename()).getFileName().toString()
                    : "image";
            String extension = getExtension(originalName);
            String fileName = userId + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path targetFile = targetDirectory.resolve(fileName).normalize();
            if (!targetFile.startsWith(storageRoot)) {
                throw new BusinessException(400, "非法的文件路径");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new BusinessException(500, "保存图片失败");
            }

            String relativeUrl = "/uploads/" + storageRoot.relativize(targetFile).toString().replace('\\', '/');
            uploadedImages.add(new UploadImageVo(relativeUrl, originalName, fileName, file.getSize()));
        }
        return List.copyOf(uploadedImages);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "图片不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(400, "单张图片不能超过 5MB");
        }
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "仅支持 JPG、PNG、WEBP 图片");
        }
        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType) && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(400, "图片格式不受支持");
        }
    }

    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BusinessException(400, "图片文件缺少扩展名");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
