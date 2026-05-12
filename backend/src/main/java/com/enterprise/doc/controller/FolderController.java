package com.enterprise.doc.controller;

import com.enterprise.doc.common.Result;
import com.enterprise.doc.dto.FolderCreateDTO;
import com.enterprise.doc.service.FolderService;
import com.enterprise.doc.vo.FolderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文件夹管理")
@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @Operation(summary = "创建文件夹")
    @PostMapping
    public Result<FolderVO> create(@Valid @RequestBody FolderCreateDTO dto) {
        return Result.success(folderService.create(dto));
    }

    @Operation(summary = "删除文件夹")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        folderService.delete(id);
        return Result.success();
    }

    @Operation(summary = "重命名文件夹")
    @PutMapping("/{id}/rename")
    public Result<FolderVO> rename(@PathVariable Long id, @RequestParam String name) {
        return Result.success(folderService.rename(id, name));
    }

    @Operation(summary = "获取文库下所有文件夹（树形结构）")
    @GetMapping("/library/{libraryId}")
    public Result<List<FolderVO>> listByLibrary(@PathVariable Long libraryId) {
        return Result.success(folderService.listByLibrary(libraryId));
    }

    @Operation(summary = "获取指定父文件夹下的子文件夹")
    @GetMapping("/library/{libraryId}/parent/{parentId}")
    public Result<List<FolderVO>> listByParent(
            @PathVariable Long libraryId,
            @PathVariable Long parentId) {
        return Result.success(folderService.listByParent(libraryId, parentId));
    }
}
