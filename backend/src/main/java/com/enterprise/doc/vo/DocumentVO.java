package com.enterprise.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentVO {
    private Long id;
    private Long libraryId;
    private Long folderId;
    private String name;
    private String fileType;
    private String fileExtension;
    private Long fileSize;
    private String fileUrl;
    private String previewUrl;
    private String thumbnail;
    private String contentType;
    private Long version;
    private Long createUserId;
    private String createUserName;
    private Long updateUserId;
    private String updateUserName;
    private Integer viewCount;
    private Integer downloadCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
