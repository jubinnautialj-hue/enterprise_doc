package com.enterprise.doc.controller;

import com.enterprise.doc.common.PageResult;
import com.enterprise.doc.common.Result;
import com.enterprise.doc.service.DocumentService;
import com.enterprise.doc.vo.DocumentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "文档管理")
@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public Result<DocumentVO> upload(
            @RequestParam Long libraryId,
            @RequestParam(required = false) Long folderId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(documentService.upload(libraryId, folderId, file));
    }

    @Operation(summary = "更新文档版本")
    @PostMapping("/{id}/update")
    public Result<DocumentVO> update(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String changeLog) throws IOException {
        return Result.success(documentService.update(id, file, changeLog));
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return Result.success();
    }

    @Operation(summary = "重命名文档")
    @PutMapping("/{id}/rename")
    public Result<DocumentVO> rename(@PathVariable Long id, @RequestParam String name) {
        return Result.success(documentService.rename(id, name));
    }

    @Operation(summary = "移动文档")
    @PutMapping("/{id}/move")
    public Result<DocumentVO> move(@PathVariable Long id, @RequestParam(required = false) Long folderId) {
        return Result.success(documentService.move(id, folderId));
    }

    @Operation(summary = "文档列表")
    @GetMapping("/list")
    public Result<PageResult<DocumentVO>> list(
            @RequestParam Long libraryId,
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(documentService.list(libraryId, folderId, current, size, keyword));
    }

    @Operation(summary = "文档详情")
    @GetMapping("/{id}")
    public Result<DocumentVO> getById(@PathVariable Long id) {
        return Result.success(documentService.getById(id));
    }

    @Operation(summary = "下载文档")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        File file = documentService.getFileForDownload(id);
        String filename = documentService.getFilenameForDownload(id);
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentLength(file.length())
                .body(new FileSystemResource(file));
    }
}
