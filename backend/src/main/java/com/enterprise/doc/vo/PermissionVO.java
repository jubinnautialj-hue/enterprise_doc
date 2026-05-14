package com.enterprise.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PermissionVO {
    private Long id;
    private String name;
    private String code;
    private Integer type;
    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private List<PermissionVO> children;
}
