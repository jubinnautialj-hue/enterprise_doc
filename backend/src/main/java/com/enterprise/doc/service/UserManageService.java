package com.enterprise.doc.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.doc.common.PageResult;
import com.enterprise.doc.entity.User;
import com.enterprise.doc.entity.UserRole;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.UserMapper;
import com.enterprise.doc.mapper.UserRoleMapper;
import com.enterprise.doc.vo.RoleVO;
import com.enterprise.doc.vo.UserDetailVO;
import com.enterprise.doc.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManageService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVO> list(Long current, Long size, String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(page, wrapper);
        PageResult<UserVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setRecords(result.getRecords().stream()
                .map(this::toUserVO)
                .collect(Collectors.toList()));
        return pageResult;
    }

    public UserDetailVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserDetailVO vo = new UserDetailVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoles(roleService.getByUserId(id));
        vo.setPermissions(permissionService.getPermissionCodesByUserId(id));
        return vo;
    }

    @Transactional
    public UserVO create(UserVO vo, String password) {
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, vo.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(vo, user);
        user.setPassword(passwordEncoder.encode(
                StrUtil.isNotBlank(password) ? password : "123456"));
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        user.setStatus(1);
        userMapper.insert(user);
        return toUserVO(user);
    }

    @Transactional
    public UserVO update(Long id, UserVO vo) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, vo.getUsername())
                        .ne(User::getId, id));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        BeanUtils.copyProperties(vo, user);
        user.setPassword(null);
        userMapper.updateById(user);
        return toUserVO(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("超级管理员不能删除");
        }
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        userMapper.deleteById(id);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(
                StrUtil.isNotBlank(newPassword) ? newPassword : "123456"));
        userMapper.updateById(user);
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    public List<RoleVO> getUserRoles(Long userId) {
        return roleService.getByUserId(userId);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
