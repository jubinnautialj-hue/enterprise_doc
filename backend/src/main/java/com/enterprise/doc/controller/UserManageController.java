package com.enterprise.doc.controller;

import com.enterprise.doc.common.PageResult;
import com.enterprise.doc.common.Result;
import com.enterprise.doc.service.UserManageService;
import com.enterprise.doc.vo.RoleVO;
import com.enterprise.doc.vo.UserDetailVO;
import com.enterprise.doc.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManageController {

    private final UserManageService userManageService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(userManageService.list(current, size, keyword));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserDetailVO> getById(@PathVariable Long id) {
        return Result.success(userManageService.getById(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<UserVO> create(@RequestBody Map<String, Object> params) {
        UserVO vo = new UserVO();
        vo.setUsername((String) params.get("username"));
        vo.setNickname((String) params.get("nickname"));
        vo.setEmail((String) params.get("email"));
        vo.setPhone((String) params.get("phone"));
        vo.setStatus(params.get("status") != null ? ((Number) params.get("status")).intValue() : 1);
        
        String password = (String) params.get("password");
        return Result.success(userManageService.create(vo, password));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @RequestBody UserVO vo) {
        return Result.success(userManageService.update(id, vo));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userManageService.delete(id);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        userManageService.resetPassword(id, params.get("password"));
        return Result.success();
    }

    @Operation(summary = "分配角色")
    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        userManageService.assignRoles(id, params.get("roleIds"));
        return Result.success();
    }

    @Operation(summary = "获取用户角色")
    @GetMapping("/{id}/roles")
    public Result<List<RoleVO>> getUserRoles(@PathVariable Long id) {
        return Result.success(userManageService.getUserRoles(id));
    }
}
