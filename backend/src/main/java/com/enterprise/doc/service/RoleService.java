package com.enterprise.doc.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.doc.common.PageResult;
import com.enterprise.doc.entity.Role;
import com.enterprise.doc.entity.RolePermission;
import com.enterprise.doc.entity.UserRole;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.RoleMapper;
import com.enterprise.doc.mapper.RolePermissionMapper;
import com.enterprise.doc.mapper.UserRoleMapper;
import com.enterprise.doc.util.SecurityUtils;
import com.enterprise.doc.vo.PermissionVO;
import com.enterprise.doc.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionService permissionService;

    public PageResult<RoleVO> list(Long current, Long size, String keyword) {
        Page<Role> page = new Page<>(current, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Role::getName, keyword)
                    .or().like(Role::getCode, keyword);
        }
        wrapper.orderByDesc(Role::getCreateTime);

        Page<Role> result = roleMapper.selectPage(page, wrapper);
        PageResult<RoleVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setRecords(result.getRecords().stream()
                .map(this::toRoleVO)
                .collect(Collectors.toList()));
        return pageResult;
    }

    public List<RoleVO> listAll() {
        return roleMapper.selectList(
                        new LambdaQueryWrapper<Role>()
                                .eq(Role::getStatus, 1)
                                .orderByAsc(Role::getSort)
                                .orderByDesc(Role::getCreateTime))
                .stream()
                .map(this::toRoleVO)
                .collect(Collectors.toList());
    }

    public RoleVO getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        RoleVO vo = toRoleVO(role);
        vo.setPermissions(permissionService.getByRoleId(id));
        return vo;
    }

    @Transactional
    public RoleVO create(RoleVO vo) {
        Role exist = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getCode, vo.getCode()));
        if (exist != null) {
            throw new BusinessException("角色编码已存在");
        }
        Role role = new Role();
        BeanUtils.copyProperties(vo, role);
        role.setStatus(1);
        roleMapper.insert(role);
        return toRoleVO(role);
    }

    @Transactional
    public RoleVO update(Long id, RoleVO vo) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        Role exist = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getCode, vo.getCode())
                        .ne(Role::getId, id));
        if (exist != null) {
            throw new BusinessException("角色编码已存在");
        }
        BeanUtils.copyProperties(vo, role);
        roleMapper.updateById(role);
        return toRoleVO(role);
    }

    @Transactional
    public void delete(Long id) {
        if (userRoleMapper.selectCount(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id)) > 0) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
        roleMapper.deleteById(id);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    public List<RoleVO> getByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId).stream()
                .map(this::toRoleVO)
                .collect(Collectors.toList());
    }

    private RoleVO toRoleVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
