package com.enterprise.doc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.doc.entity.Permission;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.PermissionMapper;
import com.enterprise.doc.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;

    public List<PermissionVO> listTree() {
        List<Permission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getStatus, 1)
                        .orderByAsc(Permission::getSort)
                        .orderByDesc(Permission::getCreateTime));
        return buildTree(all, 0L);
    }

    public List<PermissionVO> getByRoleId(Long roleId) {
        List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
        return permissions.stream()
                .map(this::toPermissionVO)
                .collect(Collectors.toList());
    }

    public List<PermissionVO> getByUserId(Long userId) {
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        return buildTree(permissions, 0L);
    }

    public List<String> getPermissionCodesByUserId(Long userId) {
        return permissionMapper.selectPermissionCodesByUserId(userId);
    }

    public PermissionVO getById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        return toPermissionVO(permission);
    }

    @Transactional
    public PermissionVO create(PermissionVO vo) {
        Permission permission = new Permission();
        BeanUtils.copyProperties(vo, permission);
        permission.setStatus(1);
        permissionMapper.insert(permission);
        return toPermissionVO(permission);
    }

    @Transactional
    public PermissionVO update(Long id, PermissionVO vo) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        BeanUtils.copyProperties(vo, permission);
        permissionMapper.updateById(permission);
        return toPermissionVO(permission);
    }

    @Transactional
    public void delete(Long id) {
        if (permissionMapper.selectCount(
                new LambdaQueryWrapper<Permission>().eq(Permission::getParentId, id)) > 0) {
            throw new BusinessException("存在子权限，无法删除");
        }
        permissionMapper.deleteById(id);
    }

    public boolean hasPermission(Long userId, String permissionCode) {
        List<String> codes = getPermissionCodesByUserId(userId);
        return codes.contains(permissionCode);
    }

    private List<PermissionVO> buildTree(List<Permission> permissions, Long parentId) {
        List<PermissionVO> result = new ArrayList<>();
        Map<Long, PermissionVO> idMap = new HashMap<>();
        
        for (Permission p : permissions) {
            PermissionVO vo = toPermissionVO(p);
            idMap.put(p.getId(), vo);
        }
        
        for (Permission p : permissions) {
            PermissionVO vo = idMap.get(p.getId());
            if (p.getParentId() == null || p.getParentId().equals(parentId)) {
                result.add(vo);
            } else {
                PermissionVO parent = idMap.get(p.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                }
            }
        }
        
        result.sort(Comparator.comparing(v -> v.getSort() == null ? 0 : v.getSort()));
        return result;
    }

    private PermissionVO toPermissionVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }
}
