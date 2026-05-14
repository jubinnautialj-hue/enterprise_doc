package com.enterprise.doc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_directory_permission")
public class DirectoryPermission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer targetType;

    private Long targetId;

    private Integer subjectType;

    private Long subjectId;

    private Integer permissionType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long createUserId;

    @TableLogic
    private Integer deleted;
}
