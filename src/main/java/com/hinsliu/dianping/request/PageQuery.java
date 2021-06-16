package com.hinsliu.dianping.request;

import lombok.Data;

/**
 * @Description: 分页查询请求
 * @author: liuxuanming
 * @date: 2021/06/16 6:33 下午
 */

@Data
public class PageQuery {

    private Integer page = 1;

    private Integer size = 20;
}
