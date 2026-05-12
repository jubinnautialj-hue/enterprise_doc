package com.enterprise.doc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_library_member")
public class LibraryMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long libraryId;
    
    private Long userId;
    
    private String userName;
    
    private Integer role;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
