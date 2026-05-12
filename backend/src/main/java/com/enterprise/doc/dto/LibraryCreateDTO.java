package com.enterprise.doc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LibraryCreateDTO {
    @NotBlank(message = "文库名称不能为空")
    private String name;

    private String description;

    private String cover;

    private Integer type;
}
