package com.enterprise.doc.controller;

import com.enterprise.doc.common.Result;
import com.enterprise.doc.entity.DirectoryPermission;
import com.enterprise.doc.service.DirectoryPermissionService;
import com.enterprise.doc.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "目录权限管理")
@RestController
@RequestMapping("/directory-permissions")
@RequiredArgsConstructor
public class DirectoryPermissionController {

    private final DirectoryPermissionService directoryPermissionService;

    @Operation(summary = "获取目标权限列表")
    @GetMapping
    public Result<List<DirectoryPermission>> listByTarget(
            @RequestParam Integer targetType,
            @RequestParam Long targetId) {
        return Result.success(directoryPermissionService.listByTarget(targetType, targetId));
    }

    @Operation(summary = "添加/更新权限")
    @PostMapping
    public Result<DirectoryPermission> add(@RequestBody DirectoryPermission permission) {
        return Result.success(directoryPermissionService.add(permission));
    }

    @Operation(summary = "更新权限类型")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        directoryPermissionService.update(id, params.get("permissionType"));
        return Result.success();
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        directoryPermissionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "检查当前用户权限")
    @GetMapping("/check")
    public Result<Integer> checkPermission(
            @RequestParam Integer targetType,
            @RequestParam Long targetId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(directoryPermissionService.getUserPermissionType(userId, targetType, targetId));
    }
}
