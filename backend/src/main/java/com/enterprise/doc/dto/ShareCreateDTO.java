package com.enterprise.doc.dto;

import lombok.Data;

@Data
public class ShareCreateDTO {
    private Integer shareType;
    private Long targetId;
    private String password;
    private Integer expireType = 0;
}
