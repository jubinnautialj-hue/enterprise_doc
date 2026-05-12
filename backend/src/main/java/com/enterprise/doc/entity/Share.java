package com.enterprise.doc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doc_share")
public class Share {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer shareType;
    
    private Long targetId;
    
    private String shareCode;
    
    private String shareUrl;
    
    private String password;
    
    private Integer expireType;
    
    private LocalDateTime expireTime;
    
    private Long viewCount;
    
    private Long downloadCount;
    
    private Long createUserId;
    
    private String createUserName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
