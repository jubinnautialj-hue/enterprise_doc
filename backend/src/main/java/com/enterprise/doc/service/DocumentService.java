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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentMapper documentMapper;
    private final DocumentVersionMapper documentVersionMapper;
    private final UserMapper userMapper;
    private final FileService fileService;
    private final ElasticsearchService elasticsearchService;

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

        DocumentVO vo = toDocumentVO(document);
        elasticsearchService.indexDocument(document);
        return vo;
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

        DocumentVO vo = toDocumentVO(document);
        elasticsearchService.indexDocument(document);
        return vo;
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
        elasticsearchService.deleteDocument(id);
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

    public List<DocumentVO> search(String keyword, Long libraryId, Long current, Long size) {
        List<Long> ids = elasticsearchService.searchDocuments(keyword, libraryId);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Document::getId, ids);
        if (current != null && size != null) {
            wrapper.last("LIMIT " + (current - 1) * size + "," + size);
        }
        
        List<Document> documents = documentMapper.selectList(wrapper);
        return documents.stream()
                .map(this::toDocumentVO)
                .collect(Collectors.toList());
    }

    public void rebuildIndex() {
        elasticsearchService.rebuildIndex();
    }

    @Transactional
    public DocumentVO createEmptyDocument(Long libraryId, Long folderId, String name, String docType) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String extension;
        String fileType;
        String contentType;
        long fileSize = 0;
        String storagePath;

        try {
            switch (docType) {
                case "word":
                    extension = "docx";
                    fileType = "word";
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    storagePath = createEmptyWordDocument(name);
                    break;
                case "excel":
                    extension = "xlsx";
                    fileType = "excel";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    storagePath = createEmptyExcelDocument(name);
                    break;
                case "ppt":
                    extension = "pptx";
                    fileType = "ppt";
                    contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                    storagePath = createEmptyPptDocument(name);
                    break;
                case "csv":
                    extension = "csv";
                    fileType = "csv";
                    contentType = "text/csv";
                    String csvContent = ",";
                    storagePath = createEmptyTextFile(name + "." + extension, csvContent);
                    break;
                case "markdown":
                    extension = "md";
                    fileType = "markdown";
                    contentType = "text/markdown";
                    String mdContent = "# " + name + "\n\n请在这里编写文档内容...";
                    storagePath = createEmptyTextFile(name + "." + extension, mdContent);
                    break;
                case "flowchart":
                    extension = "flowchart";
                    fileType = "flowchart";
                    contentType = "application/json";
                    String flowContent = "{\n  \"data\": {\n    \"id\": \"root\",\n    \"label\": \"" + name + "\"\n  }\n}";
                    storagePath = createEmptyTextFile(name + "." + extension, flowContent);
                    break;
                case "mindmap":
                    extension = "mindmap";
                    fileType = "mindmap";
                    contentType = "application/json";
                    String mindContent = "{\n  \"data\": {\n    \"id\": \"root\",\n    \"label\": \"" + name + "\"\n  }\n}";
                    storagePath = createEmptyTextFile(name + "." + extension, mindContent);
                    break;
                default:
                    throw new BusinessException("不支持的文档类型");
            }

            File file = fileService.getFile(storagePath);
            fileSize = file.length();
            String filename = name + "." + extension;

            Document document = new Document();
            document.setLibraryId(libraryId);
            document.setFolderId(folderId == null ? 0L : folderId);
            document.setName(filename);
            document.setFileType(fileType);
            document.setFileExtension(extension);
            document.setFileSize(fileSize);
            document.setStoragePath(storagePath);
            document.setFileUrl("/api/doc/view/" + storagePath);
            document.setPreviewUrl("/api/doc/view/" + storagePath);
            document.setContentType(contentType);
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
            version.setFileSize(fileSize);
            version.setChangeLog("初始版本");
            version.setCreateUserId(userId);
            version.setCreateUserName(document.getCreateUserName());
            documentVersionMapper.insert(version);

            DocumentVO vo = toDocumentVO(document);
            elasticsearchService.indexDocument(document);
            return vo;
        } catch (IOException e) {
            throw new BusinessException("创建文档失败: " + e.getMessage());
        }
    }

    private String createEmptyWordDocument(String name) throws IOException {
        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(name);
        run.setBold(true);
        run.setFontSize(24);

        XWPFParagraph para2 = doc.createParagraph();
        XWPFRun run2 = para2.createRun();
        run2.setText("请在这里编写文档内容...");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.write(out);
        doc.close();

        byte[] content = out.toByteArray();
        return createFileFromBytes(name + ".docx", content);
    }

    private String createEmptyExcelDocument(String name) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(name);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        byte[] content = out.toByteArray();
        return createFileFromBytes(name + ".xlsx", content);
    }

    private String createEmptyPptDocument(String name) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFTextBox textBox = slide.createTextBox();
        textBox.setText(name);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        ppt.close();

        byte[] content = out.toByteArray();
        return createFileFromBytes(name + ".pptx", content);
    }

    private String createEmptyTextFile(String filename, String content) {
        String datePath = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = cn.hutool.core.util.IdUtil.simpleUUID() + "." + cn.hutool.core.io.FileUtil.extName(filename);
        String storagePath = datePath + "/" + newFilename;

        File file = fileService.getFile(storagePath);
        try {
            java.nio.file.Files.createDirectories(file.getParentFile().toPath());
            java.nio.file.Files.writeString(file.toPath(), content, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException("创建文件失败");
        }

        return storagePath;
    }

    private String createFileFromBytes(String filename, byte[] content) throws IOException {
        String datePath = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = cn.hutool.core.util.IdUtil.simpleUUID() + "." + cn.hutool.core.io.FileUtil.extName(filename);
        String storagePath = datePath + "/" + newFilename;

        File file = fileService.getFile(storagePath);
        java.nio.file.Files.createDirectories(file.getParentFile().toPath());
        java.nio.file.Files.write(file.toPath(), content);

        return storagePath;
    }

    public String getDocumentContent(Long id) throws IOException {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        File file = fileService.getFile(document.getStoragePath());
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }

        String fileType = document.getFileType();
        if ("markdown".equals(fileType) || "flowchart".equals(fileType) || "mindmap".equals(fileType) || "csv".equals(fileType)) {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }

        throw new BusinessException("该文档类型不支持在线编辑，请下载后使用专业软件编辑");
    }

    @Transactional
    public DocumentVO saveDocumentContent(Long id, String content) throws IOException {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        if (!document.getCreateUserId().equals(userId)) {
            throw new BusinessException("无权限编辑该文档");
        }

        String fileType = document.getFileType();
        if (!"markdown".equals(fileType) && !"flowchart".equals(fileType) && !"mindmap".equals(fileType) && !"csv".equals(fileType)) {
            throw new BusinessException("该文档类型不支持在线编辑，请下载后使用专业软件编辑");
        }

        String oldStoragePath = document.getStoragePath();
        String extension = document.getFileExtension();
        String filename = document.getName();
        String datePath = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = cn.hutool.core.util.IdUtil.simpleUUID() + "." + extension;
        String storagePath = datePath + "/" + newFilename;

        File newFile = fileService.getFile(storagePath);
        Files.createDirectories(newFile.getParentFile().toPath());
        Files.writeString(newFile.toPath(), content, StandardCharsets.UTF_8);

        long fileSize = newFile.length();
        Long newVersion = document.getVersion() + 1;

        document.setStoragePath(storagePath);
        document.setFileUrl("/api/doc/view/" + storagePath);
        document.setPreviewUrl("/api/doc/view/" + storagePath);
        document.setFileSize(fileSize);
        document.setVersion(newVersion);
        document.setUpdateUserId(userId);
        document.setUpdateUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        documentMapper.updateById(document);

        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(id);
        version.setVersion(newVersion);
        version.setStoragePath(storagePath);
        version.setFileUrl(document.getFileUrl());
        version.setFileSize(fileSize);
        version.setChangeLog("在线编辑保存");
        version.setCreateUserId(userId);
        version.setCreateUserName(document.getUpdateUserName());
        documentVersionMapper.insert(version);

        if (StrUtil.isNotBlank(oldStoragePath)) {
            fileService.deleteFile(oldStoragePath);
        }

        DocumentVO vo = toDocumentVO(document);
        elasticsearchService.indexDocument(document);
        return vo;
    }

    private DocumentVO toDocumentVO(Document document) {
        DocumentVO vo = new DocumentVO();
        BeanUtils.copyProperties(document, vo);
        return vo;
    }
}
