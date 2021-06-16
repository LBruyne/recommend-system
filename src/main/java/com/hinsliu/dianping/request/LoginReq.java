package com.hinsliu.dianping.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Description: 登录表单
 * @author: liuxuanming
 * @date: 2021/06/16 6:01 下午
 */
@Data
public class LoginReq {

    @NotBlank(message = "手机号不能为空")
    private String telphone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
