package com.enterprise.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FolderVO {
    private Long id;
    private Long libraryId;
    private String name;
    private Long parentId;
    private String path;
    private Long createUserId;
    private String createUserName;
    private Integer sort;
    private LocalDateTime createTime;
    private List<FolderVO> children;
}
