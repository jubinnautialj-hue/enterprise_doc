package com.enterprise.doc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.doc.dto.LoginDTO;
import com.enterprise.doc.dto.RegisterDTO;
import com.enterprise.doc.entity.User;
import com.enterprise.doc.exception.BusinessException;
import com.enterprise.doc.mapper.UserMapper;
import com.enterprise.doc.security.CustomUserDetails;
import com.enterprise.doc.security.JwtTokenProvider;
import com.enterprise.doc.vo.LoginVO;
import com.enterprise.doc.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginVO login(LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails.getId(), userDetails.getUsername());

        User user = userMapper.selectById(userDetails.getId());
        UserVO userVO = toUserVO(user);

        return new LoginVO(token, userVO);
    }

    public UserVO register(RegisterDTO dto) {
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        user.setStatus(1);
        userMapper.insert(user);

        return toUserVO(user);
    }

    public UserVO getCurrentUser() {
        Long userId = com.enterprise.doc.util.SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserVO(user);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
