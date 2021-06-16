package com.hinsliu.dianping.common;

import lombok.Data;

/**
 * @Description: 通用返回结果
 * @author: liuxuanming
 * @date: 2021/06/16 5:02 下午
 */
@Data
public class CommonRes {

    //表明读经请求的返回处理结果，"success"或"fail"
    private String status;

    //若status=success时，表明对应的返回的json类数据
    //若status=fail时，则data内将使用通用的错误码对应的格式
    private Object data;

    //定义一个通用的创建返回对象的方法
    public static CommonRes create(Object result) {
        return CommonRes.create(result, "success");
    }

    public static CommonRes create(Object result, String status) {
        CommonRes commonRes = new CommonRes();
        commonRes.setStatus(status);
        commonRes.setData(result);

        return commonRes;
    }
}
