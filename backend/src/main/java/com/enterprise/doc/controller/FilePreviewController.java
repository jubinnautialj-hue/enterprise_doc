package com.enterprise.doc.controller;

import cn.hutool.core.io.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/doc")
public class FilePreviewController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @GetMapping("/view/**")
    public void viewFile(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = false) String download) throws IOException {
        String requestUri = request.getRequestURI();
        String prefix = "/api/doc/view/";
        int idx = requestUri.indexOf(prefix);
        if (idx < 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String relativePath = URLDecoder.decode(requestUri.substring(idx + prefix.length()), StandardCharsets.UTF_8);
        File file = new File(uploadPath, relativePath);

        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String filename = file.getName();
        String ext = FileUtil.extName(filename).toLowerCase();
        String contentType = getContentType(ext);
        String encodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);

        response.setContentType(contentType);
        if ("true".equals(download)) {
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFilename);
        } else {
            response.setHeader("Content-Disposition", "inline; filename=" + encodedFilename);
        }
        response.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    private String getContentType(String ext) {
        switch (ext) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "svg":
                return "image/svg+xml";
            case "txt":
                return "text/plain; charset=UTF-8";
            case "html":
            case "htm":
                return "text/html; charset=UTF-8";
            case "css":
                return "text/css; charset=UTF-8";
            case "js":
                return "application/javascript; charset=UTF-8";
            case "json":
                return "application/json; charset=UTF-8";
            case "md":
                return "text/markdown; charset=UTF-8";
            case "csv":
                return "text/csv; charset=UTF-8";
            case "xml":
                return "application/xml; charset=UTF-8";
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "mp4":
                return "video/mp4";
            case "webm":
                return "video/webm";
            case "ogg":
                return "video/ogg";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            case "7z":
                return "application/x-7z-compressed";
            default:
                return "application/octet-stream";
        }
    }
}
