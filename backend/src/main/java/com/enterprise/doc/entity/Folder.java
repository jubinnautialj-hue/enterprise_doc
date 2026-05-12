package com.enterprise.doc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_folder")
public class Folder {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long libraryId;
    
    private String name;
    
    private Long parentId;
    
    private String path;
    
    private Long createUserId;
    
    private String createUserName;
    
    private Integer sort;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
