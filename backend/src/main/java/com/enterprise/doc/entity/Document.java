package com.enterprise.doc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_document")
public class Document {
    @TableId(type = IdType.AUTO)
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
    
    private String storagePath;
    
    private Long version;
    
    private Long createUserId;
    
    private String createUserName;
    
    private Long updateUserId;
    
    private String updateUserName;
    
    private Integer viewCount;
    
    private Integer downloadCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
