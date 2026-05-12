package com.enterprise.doc.controller;

import com.enterprise.doc.common.PageResult;
import com.enterprise.doc.common.Result;
import com.enterprise.doc.dto.LibraryCreateDTO;
import com.enterprise.doc.service.LibraryService;
import com.enterprise.doc.vo.LibraryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文库管理")
@RestController
@RequestMapping("/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @Operation(summary = "创建文库")
    @PostMapping
    public Result<LibraryVO> create(@Valid @RequestBody LibraryCreateDTO dto) {
        return Result.success(libraryService.create(dto));
    }

    @Operation(summary = "删除文库")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        libraryService.delete(id);
        return Result.success();
    }

    @Operation(summary = "更新文库")
    @PutMapping("/{id}")
    public Result<LibraryVO> update(@PathVariable Long id, @Valid @RequestBody LibraryCreateDTO dto) {
        return Result.success(libraryService.update(id, dto));
    }

    @Operation(summary = "获取文库详情")
    @GetMapping("/{id}")
    public Result<LibraryVO> getById(@PathVariable Long id) {
        return Result.success(libraryService.getById(id));
    }

    @Operation(summary = "公共文库列表")
    @GetMapping("/public")
    public Result<PageResult<LibraryVO>> listPublic(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(libraryService.listPublic(current, size, keyword));
    }

    @Operation(summary = "个人文库列表")
    @GetMapping("/personal")
    public Result<PageResult<LibraryVO>> listPersonal(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(libraryService.listPersonal(current, size, keyword));
    }
}
