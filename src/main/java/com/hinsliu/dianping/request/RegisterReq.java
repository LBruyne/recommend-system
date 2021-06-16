package com.hinsliu.dianping.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Description: 注册表单
 * @author: liuxuanming
 * @date: 2021/06/16 5:56 下午
 */
@Data
public class RegisterReq {

    @NotBlank(message = "手机号不能为空")
    private String telphone;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickName;

    @NotNull(message = "性别不能为空")
    private Integer gender;
}
