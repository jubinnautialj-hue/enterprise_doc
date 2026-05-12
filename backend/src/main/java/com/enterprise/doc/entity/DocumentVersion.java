package com.enterprise.doc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_document_version")
public class DocumentVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long documentId;
    
    private Long version;
    
    private String fileUrl;
    
    private String storagePath;
    
    private Long fileSize;
    
    private String changeLog;
    
    private Long createUserId;
    
    private String createUserName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
