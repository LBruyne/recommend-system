package com.hinsliu.dianping.common;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * @Description: 通用方法
 * @author: liuxuanming
 * @date: 2021/06/16 5:07 下午
 */
public class CommonUtil {
    public static String processErrorString(BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            stringBuilder.append(fieldError.getDefaultMessage() + ",");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }
}
