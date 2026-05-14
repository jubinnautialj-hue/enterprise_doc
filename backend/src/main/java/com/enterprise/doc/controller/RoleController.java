package com.enterprise.doc.controller;

import com.enterprise.doc.common.PageResult;
import com.enterprise.doc.common.Result;
import com.enterprise.doc.service.RoleService;
import com.enterprise.doc.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色列表")
    @GetMapping
    public Result<PageResult<RoleVO>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(roleService.list(current, size, keyword));
    }

    @Operation(summary = "获取所有角色")
    @GetMapping("/all")
    public Result<List<RoleVO>> listAll() {
        return Result.success(roleService.listAll());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<RoleVO> create(@RequestBody RoleVO vo) {
        return Result.success(roleService.create(vo));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<RoleVO> update(@PathVariable Long id, @RequestBody RoleVO vo) {
        return Result.success(roleService.update(id, vo));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "分配权限")
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        roleService.assignPermissions(id, params.get("permissionIds"));
        return Result.success();
    }
}
