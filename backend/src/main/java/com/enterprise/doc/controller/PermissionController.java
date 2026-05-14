package com.enterprise.doc.controller;

import com.enterprise.doc.common.Result;
import com.enterprise.doc.service.PermissionService;
import com.enterprise.doc.vo.PermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取权限树")
    @GetMapping("/tree")
    public Result<List<PermissionVO>> listTree() {
        return Result.success(permissionService.listTree());
    }

    @Operation(summary = "获取权限详情")
    @GetMapping("/{id}")
    public Result<PermissionVO> getById(@PathVariable Long id) {
        return Result.success(permissionService.getById(id));
    }

    @Operation(summary = "创建权限")
    @PostMapping
    public Result<PermissionVO> create(@RequestBody PermissionVO vo) {
        return Result.success(permissionService.create(vo));
    }

    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    public Result<PermissionVO> update(@PathVariable Long id, @RequestBody PermissionVO vo) {
        return Result.success(permissionService.update(id, vo));
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取当前用户权限")
    @GetMapping("/me")
    public Result<List<PermissionVO>> getCurrentUserPermissions() {
        Long userId = com.enterprise.doc.util.SecurityUtils.getCurrentUserId();
        return Result.success(permissionService.getByUserId(userId));
    }
}
