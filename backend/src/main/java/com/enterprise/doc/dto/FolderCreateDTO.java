package com.enterprise.doc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FolderCreateDTO {
    @NotNull(message = "文库ID不能为空")
    private Long libraryId;

    private Long parentId = 0L;

    @NotBlank(message = "文件夹名称不能为空")
    private String name;
}
