package com.enterprise.doc.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.doc.common.PageResult;
import com.enterprise.doc.entity.Document;
import com.enterprise.doc.entity.DocumentVersion;
import com.enterprise.doc.entity.User;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.DocumentMapper;
import com.enterprise.doc.mapper.DocumentVersionMapper;
import com.enterprise.doc.mapper.UserMapper;
import com.enterprise.doc.util.SecurityUtils;
import com.enterprise.doc.vo.DocumentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentMapper documentMapper;
    private final DocumentVersionMapper documentVersionMapper;
    private final UserMapper userMapper;
    private final FileService fileService;

    @Transactional
    public DocumentVO upload(Long libraryId, Long folderId, MultipartFile file) throws IOException {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException("文件名不能为空");
        }

        String storagePath = fileService.uploadFile(file);
        String extension = fileService.getFileExtension(originalFilename);
        String fileType = fileService.getFileType(extension);

        Document document = new Document();
        document.setLibraryId(libraryId);
        document.setFolderId(folderId == null ? 0L : folderId);
        document.setName(originalFilename);
        document.setFileType(fileType);
        document.setFileExtension(extension);
        document.setFileSize(file.getSize());
        document.setStoragePath(storagePath);
        document.setFileUrl("/api/doc/view/" + storagePath);
        document.setPreviewUrl("/api/doc/view/" + storagePath);
        document.setContentType(file.getContentType());
        document.setVersion(1L);
        document.setCreateUserId(userId);
        document.setCreateUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        document.setViewCount(0);
        document.setDownloadCount(0);
        documentMapper.insert(document);

        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(document.getId());
        version.setVersion(1L);
        version.setStoragePath(storagePath);
        version.setFileUrl(document.getFileUrl());
        version.setFileSize(file.getSize());
        version.setChangeLog("初始版本");
        version.setCreateUserId(userId);
        version.setCreateUserName(document.getCreateUserName());
        documentVersionMapper.insert(version);

        return toDocumentVO(document);
    }

    @Transactional
    public DocumentVO update(Long id, MultipartFile file, String changeLog) throws IOException {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        String oldStoragePath = document.getStoragePath();
        String storagePath = fileService.uploadFile(file);
        String originalFilename = file.getOriginalFilename();
        String extension = fileService.getFileExtension(originalFilename);
        String fileType = fileService.getFileType(extension);

        Long newVersion = document.getVersion() + 1;

        document.setName(originalFilename);
        document.setFileType(fileType);
        document.setFileExtension(extension);
        document.setFileSize(file.getSize());
        document.setStoragePath(storagePath);
        document.setFileUrl("/api/doc/view/" + storagePath);
        document.setPreviewUrl("/api/doc/view/" + storagePath);
        document.setContentType(file.getContentType());
        document.setVersion(newVersion);
        document.setUpdateUserId(userId);
        document.setUpdateUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        documentMapper.updateById(document);

        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(id);
        version.setVersion(newVersion);
        version.setStoragePath(storagePath);
        version.setFileUrl(document.getFileUrl());
        version.setFileSize(file.getSize());
        version.setChangeLog(StrUtil.isNotBlank(changeLog) ? changeLog : "更新版本");
        version.setCreateUserId(userId);
        version.setCreateUserName(document.getUpdateUserName());
        documentVersionMapper.insert(version);

        if (StrUtil.isNotBlank(oldStoragePath)) {
            fileService.deleteFile(oldStoragePath);
        }

        return toDocumentVO(document);
    }

    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        if (!document.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权限删除该文档");
        }

        if (StrUtil.isNotBlank(document.getStoragePath())) {
            fileService.deleteFile(document.getStoragePath());
        }
        documentMapper.deleteById(id);
    }

    public DocumentVO rename(Long id, String name) {
        Long userId = SecurityUtils.getCurrentUserId();
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        if (!document.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权限重命名该文档");
        }

        document.setName(name);
        documentMapper.updateById(document);
        return toDocumentVO(document);
    }

    public DocumentVO move(Long id, Long folderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        if (!document.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权限移动该文档");
        }

        document.setFolderId(folderId == null ? 0L : folderId);
        documentMapper.updateById(document);
        return toDocumentVO(document);
    }

    public PageResult<DocumentVO> list(Long libraryId, Long folderId, Long current, Long size, String keyword) {
        Page<Document> page = new Page<>(current, size);
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getLibraryId, libraryId)
                .eq(Document::getFolderId, folderId == null ? 0L : folderId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Document::getName, keyword);
        }
        wrapper.orderByDesc(Document::getCreateTime);

        Page<Document> result = documentMapper.selectPage(page, wrapper);
        PageResult<DocumentVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setRecords(result.getRecords().stream()
                .map(this::toDocumentVO)
                .collect(Collectors.toList()));
        return pageResult;
    }

    public DocumentVO getById(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        documentMapper.update(null, new LambdaUpdateWrapper<Document>()
                .eq(Document::getId, id)
                .setSql("view_count = view_count + 1"));

        return toDocumentVO(document);
    }

    public File getFileForDownload(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        documentMapper.update(null, new LambdaUpdateWrapper<Document>()
                .eq(Document::getId, id)
                .setSql("download_count = download_count + 1"));

        return fileService.getFile(document.getStoragePath());
    }

    public String getFilenameForDownload(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        return document.getName();
    }

    private DocumentVO toDocumentVO(Document document) {
        DocumentVO vo = new DocumentVO();
        BeanUtils.copyProperties(document, vo);
        return vo;
    }
}
