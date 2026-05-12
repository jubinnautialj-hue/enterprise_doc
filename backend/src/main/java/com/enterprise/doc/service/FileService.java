package com.enterprise.doc.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class FileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    public String getFileExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    public String getFileType(String extension) {
        if (StrUtil.isBlank(extension)) {
            return "other";
        }
        extension = extension.toLowerCase();
        switch (extension) {
            case "doc":
            case "docx":
                return "word";
            case "xls":
            case "xlsx":
                return "excel";
            case "ppt":
            case "pptx":
                return "ppt";
            case "pdf":
                return "pdf";
            case "csv":
                return "csv";
            case "md":
                return "markdown";
            case "txt":
                return "text";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "webp":
                return "image";
            case "mp4":
            case "avi":
            case "mov":
            case "wmv":
                return "video";
            case "mp3":
            case "wav":
            case "flac":
                return "audio";
            case "zip":
            case "rar":
            case "7z":
                return "archive";
            default:
                return "other";
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDir = Paths.get(uploadPath, datePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = IdUtil.simpleUUID() + (StrUtil.isNotBlank(extension) ? "." + extension : "");

        Path filePath = uploadDir.resolve(newFilename);
        file.transferTo(filePath.toFile());

        return datePath + "/" + newFilename;
    }

    public File getFile(String storagePath) {
        return new File(uploadPath, storagePath);
    }

    public boolean deleteFile(String storagePath) {
        File file = new File(uploadPath, storagePath);
        return file.exists() && file.delete();
    }
}
