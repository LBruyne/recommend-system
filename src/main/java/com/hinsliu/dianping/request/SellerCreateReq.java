package com.hinsliu.dianping.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Description: 创建商家请求
 * @author: liuxuanming
 * @date: 2021/06/16 6:29 下午
 */
@Data
public class SellerCreateReq {

    @NotBlank(message = "商户名不能为空")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
