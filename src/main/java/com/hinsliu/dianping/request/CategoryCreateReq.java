package com.hinsliu.dianping.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Description: 创建分类请求
 * @author: liuxuanming
 * @date: 2021/06/16 10:25 下午
 */
@Data
public class CategoryCreateReq {

    @NotBlank(message = "名字不能为空")
    private String name;

    @NotBlank(message = "iconUrl不能为空")
    private String iconUrl;

    @NotNull(message = "权重不能为空")
    private Integer sort;
}
