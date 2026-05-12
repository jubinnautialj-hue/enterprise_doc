package com.enterprise.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareVO {
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
    private LocalDateTime createTime;
}
