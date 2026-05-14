package com.enterprise.doc.controller;

import com.enterprise.doc.common.Result;
import com.enterprise.doc.entity.User;
import com.enterprise.doc.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/reset-password")
    public Result<String> resetPassword(
            @RequestParam(defaultValue = "admin") String username,
            @RequestParam(defaultValue = "admin123") String newPassword) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setNickname(username);
            user.setStatus(1);
            userMapper.insert(user);
            String encoded = passwordEncoder.encode(newPassword);
            return Result.success("用户不存在，已创建新用户。密码哈希: " + encoded);
        }
        String encoded = passwordEncoder.encode(newPassword);
        user.setPassword(encoded);
        userMapper.updateById(user);
        return Result.success("密码已重置为: " + newPassword + "。哈希值: " + encoded);
    }

    @GetMapping("/generate-hash")
    public Result<String> generateHash(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        return Result.success(encoded);
    }
}
