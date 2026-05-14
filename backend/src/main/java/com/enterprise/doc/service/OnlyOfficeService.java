package com.enterprise.doc.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.enterprise.doc.config.OnlyOfficeConfig;
import com.enterprise.doc.entity.Document;
import com.enterprise.doc.entity.DocumentVersion;
import com.enterprise.doc.entity.User;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.DocumentMapper;
import com.enterprise.doc.mapper.DocumentVersionMapper;
import com.enterprise.doc.mapper.UserMapper;
import com.enterprise.doc.util.SecurityUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnlyOfficeService {

    private final OnlyOfficeConfig onlyOfficeConfig;
    private final DocumentMapper documentMapper;
    private final DocumentVersionMapper documentVersionMapper;
    private final UserMapper userMapper;
    private final FileService fileService;
    private final ElasticsearchService elasticsearchService;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    private SecretKey getSigningKey() {
        byte[] keyBytes = onlyOfficeConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Map<String, Object> payload) {
        return Jwts.builder()
                .claims(payload)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean verifyToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getEditorConfig(Long documentId, boolean canEdit, String callbackUrl) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Document docEntity = documentMapper.selectById(documentId);
        if (docEntity == null) {
            throw new BusinessException("文档不存在");
        }

        String fileType = getDocumentType(docEntity.getFileExtension());
        if (fileType == null) {
            throw new BusinessException("不支持的文档类型");
        }

        String documentKey = generateDocumentKey(docEntity);

        String fileUrl = callbackUrl.replace("/callback", "/download/" + documentId);
        String saveUrl = callbackUrl;

        Map<String, Object> documentConfig = new HashMap<>();
        documentConfig.put("fileType", docEntity.getFileExtension().toLowerCase());
        documentConfig.put("key", documentKey);
        documentConfig.put("title", docEntity.getName());
        documentConfig.put("url", fileUrl);
        documentConfig.put("permissions", Map.of(
                "comment", canEdit,
                "download", true,
                "edit", canEdit,
                "fillForms", canEdit,
                "modifyContentControl", canEdit,
                "modifyFilter", canEdit,
                "print", true,
                "review", canEdit
        ));

        Map<String, Object> editorConfig = new HashMap<>();
        editorConfig.put("lang", "zh-CN");
        editorConfig.put("mode", canEdit ? "edit" : "view");
        editorConfig.put("callbackUrl", saveUrl);
        editorConfig.put("createUrl", "");
        editorConfig.put("user", Map.of(
                "id", userId.toString(),
                "name", user.getNickname() != null ? user.getNickname() : user.getUsername()
        ));

        Map<String, Object> config = new HashMap<>();
        config.put("document", documentConfig);
        config.put("documentType", fileType);
        config.put("editorConfig", editorConfig);
        config.put("type", "desktop");
        config.put("height", "100%");
        config.put("width", "100%");

        if (onlyOfficeConfig.getSecret() != null && !onlyOfficeConfig.getSecret().isEmpty()) {
            String token = generateToken(config);
            config.put("token", token);
        }

        return config;
    }

    private String generateDocumentKey(Document document) {
        String key = document.getId() + "_" + document.getVersion() + "_" + document.getUpdateTime();
        return DigestUtil.md5Hex(key);
    }

    private String getDocumentType(String extension) {
        if (extension == null) return null;
        String ext = extension.toLowerCase();
        List<String> wordTypes = List.of("doc", "docx", "docm", "dot", "dotx", "dotm", "odt", "rtf", "txt", "html", "htm", "mht", "xml", "pdf");
        List<String> cellTypes = List.of("xls", "xlsx", "xlsm", "xlt", "xltx", "xltm", "ods", "csv", "tsv");
        List<String> slideTypes = List.of("ppt", "pptx", "pptm", "pot", "potx", "potm", "pps", "ppsx", "ppsm", "odp");

        if (wordTypes.contains(ext)) return "word";
        if (cellTypes.contains(ext)) return "cell";
        if (slideTypes.contains(ext)) return "slide";
        return null;
    }

    public boolean isOnlyOfficeSupported(String extension) {
        return getDocumentType(extension) != null;
    }

    @Transactional
    public void handleCallback(Long documentId, Map<String, Object> callbackData) {
        Integer status = (Integer) callbackData.get("status");
        if (status == null) {
            return;
        }

        if (status == 2 || status == 3) {
            String downloadUrl = (String) callbackData.get("url");
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                return;
            }

            saveDocumentFromUrl(documentId, downloadUrl);
        }
    }

    @Transactional
    public void saveDocumentFromUrl(Long documentId, String downloadUrl) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            user = userMapper.selectById(1L);
        }

        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        try {
            String oldStoragePath = document.getStoragePath();
            String storagePath = fileService.downloadAndSaveFile(downloadUrl, document.getFileExtension());
            java.io.File newFile = fileService.getFile(storagePath);
            long fileSize = newFile.length();
            Long newVersion = document.getVersion() + 1;

            document.setStoragePath(storagePath);
            document.setFileUrl(contextPath + "/doc/view/" + storagePath);
            document.setPreviewUrl(contextPath + "/doc/view/" + storagePath);
            document.setFileSize(fileSize);
            document.setVersion(newVersion);
            if (user != null) {
                document.setUpdateUserId(userId);
                document.setUpdateUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
            documentMapper.updateById(document);

            DocumentVersion version = new DocumentVersion();
            version.setDocumentId(documentId);
            version.setVersion(newVersion);
            version.setStoragePath(storagePath);
            version.setFileUrl(document.getFileUrl());
            version.setFileSize(fileSize);
            version.setChangeLog("OnlyOffice在线编辑保存");
            if (user != null) {
                version.setCreateUserId(userId);
                version.setCreateUserName(document.getUpdateUserName());
            } else {
                version.setCreateUserId(0L);
                version.setCreateUserName("系统");
            }
            documentVersionMapper.insert(version);

            if (oldStoragePath != null && !oldStoragePath.isEmpty()) {
                fileService.deleteFile(oldStoragePath);
            }

            elasticsearchService.indexDocument(document);
        } catch (Exception e) {
            throw new BusinessException("保存文档失败: " + e.getMessage());
        }
    }

    public String getApiUrl() {
        return onlyOfficeConfig.getApiUrl();
    }
}
