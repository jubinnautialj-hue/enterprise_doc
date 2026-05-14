package com.enterprise.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PermissionVO> permissions;
}
