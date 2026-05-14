package com.enterprise.doc.controller;

import cn.hutool.json.JSONUtil;
import com.enterprise.doc.common.Result;
import com.enterprise.doc.service.DocumentService;
import com.enterprise.doc.service.OnlyOfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "OnlyOffice文档编辑")
@RestController
@RequestMapping("/onlyoffice")
@RequiredArgsConstructor
public class OnlyOfficeController {

    private final OnlyOfficeService onlyOfficeService;
    private final DocumentService documentService;

    @Operation(summary = "获取OnlyOffice编辑器配置")
    @GetMapping("/config/{documentId}")
    public Result<Map<String, Object>> getConfig(
            @PathVariable Long documentId,
            @RequestParam(required = false, defaultValue = "true") boolean canEdit,
            HttpServletRequest request) {

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        String baseUrl = scheme + "://" + serverName + (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "") + contextPath;
        String callbackUrl = baseUrl + "/onlyoffice/callback/" + documentId;

        log.info("OnlyOffice callback URL: {}", callbackUrl);

        Map<String, Object> config = onlyOfficeService.getEditorConfig(documentId, canEdit, callbackUrl);
        Map<String, Object> result = new HashMap<>();
        result.put("config", config);
        result.put("apiUrl", onlyOfficeService.getApiUrl());

        return Result.success(result);
    }

    @Operation(summary = "OnlyOffice文件下载（供文档服务器读取）")
    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadForOnlyOffice(@PathVariable Long documentId) throws IOException {
        log.info("OnlyOffice downloading document: {}", documentId);

        File file = documentService.getFileForDownload(documentId);
        String filename = documentService.getFilenameForDownload(documentId);
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentLength(file.length())
                .body(new FileSystemResource(file));
    }

    @Operation(summary = "OnlyOffice回调（保存文档）")
    @PostMapping("/callback/{documentId}")
    public Result<Map<String, Object>> callback(
            @PathVariable Long documentId,
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {

        log.info("OnlyOffice callback received for document: {}", documentId);
        log.info("Callback body: {}", JSONUtil.toJsonStr(body));

        if (body == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", 1);
            error.put("message", "Empty body");
            return Result.success(error);
        }

        try {
            Integer status = (Integer) body.get("status");
            log.info("OnlyOffice callback status: {}", status);

            if (status == 2 || status == 3) {
                onlyOfficeService.handleCallback(documentId, body);
                log.info("Document saved successfully: {}", documentId);
            }

            Map<String, Object> success = new HashMap<>();
            success.put("error", 0);
            return Result.success(success);
        } catch (Exception e) {
            log.error("OnlyOffice callback error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", 1);
            error.put("message", e.getMessage());
            return Result.success(error);
        }
    }

    @Operation(summary = "检查文档是否支持OnlyOffice编辑")
    @GetMapping("/support/{documentId}")
    public Result<Map<String, Object>> checkSupport(@PathVariable Long documentId) {
        try {
            var doc = documentService.getById(documentId);
            boolean supported = onlyOfficeService.isOnlyOfficeSupported(doc.getFileExtension());
            Map<String, Object> result = new HashMap<>();
            result.put("supported", supported);
            return Result.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("supported", false);
            return Result.success(result);
        }
    }
}
