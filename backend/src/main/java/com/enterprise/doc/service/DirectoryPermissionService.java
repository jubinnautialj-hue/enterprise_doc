package com.enterprise.doc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.doc.entity.DirectoryPermission;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.DirectoryPermissionMapper;
import com.enterprise.doc.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryPermissionService {

    private final DirectoryPermissionMapper directoryPermissionMapper;

    public static final int PERMISSION_READ = 1;
    public static final int PERMISSION_WRITE = 2;
    public static final int PERMISSION_ADMIN = 4;

    public List<DirectoryPermission> listByTarget(Integer targetType, Long targetId) {
        return directoryPermissionMapper.selectByTarget(targetType, targetId);
    }

    @Transactional
    public DirectoryPermission add(DirectoryPermission permission) {
        Long userId = SecurityUtils.getCurrentUserId();
        permission.setCreateUserId(userId);

        DirectoryPermission exist = directoryPermissionMapper.selectOne(
                new LambdaQueryWrapper<DirectoryPermission>()
                        .eq(DirectoryPermission::getTargetType, permission.getTargetType())
                        .eq(DirectoryPermission::getTargetId, permission.getTargetId())
                        .eq(DirectoryPermission::getSubjectType, permission.getSubjectType())
                        .eq(DirectoryPermission::getSubjectId, permission.getSubjectId()));

        if (exist != null) {
            exist.setPermissionType(permission.getPermissionType());
            directoryPermissionMapper.updateById(exist);
            return exist;
        }

        directoryPermissionMapper.insert(permission);
        return permission;
    }

    @Transactional
    public void update(Long id, Integer permissionType) {
        DirectoryPermission permission = directoryPermissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限记录不存在");
        }
        permission.setPermissionType(permissionType);
        directoryPermissionMapper.updateById(permission);
    }

    @Transactional
    public void delete(Long id) {
        directoryPermissionMapper.deleteById(id);
    }

    public boolean hasPermission(Long userId, Integer targetType, Long targetId, int requiredPermission) {
        List<DirectoryPermission> permissions =
                directoryPermissionMapper.selectByUserAndTarget(userId, targetType, targetId);

        for (DirectoryPermission p : permissions) {
            if ((p.getPermissionType() & requiredPermission) == requiredPermission) {
                return true;
            }
        }
        return false;
    }

    public Integer getUserPermissionType(Long userId, Integer targetType, Long targetId) {
        List<DirectoryPermission> permissions =
                directoryPermissionMapper.selectByUserAndTarget(userId, targetType, targetId);

        int result = 0;
        for (DirectoryPermission p : permissions) {
            result |= p.getPermissionType();
        }
        return result;
    }
}
