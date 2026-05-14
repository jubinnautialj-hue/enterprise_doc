package com.enterprise.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDetailVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private List<RoleVO> roles;
    private List<String> permissions;
}
