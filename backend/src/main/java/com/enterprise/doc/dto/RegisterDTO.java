package com.enterprise.doc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度在6-100之间")
    private String password;

    @Size(max = 50, message = "昵称长度不超过50")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
}
