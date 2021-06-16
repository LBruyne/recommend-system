package com.hinsliu.dianping.common;

import com.hinsliu.dianping.controller.admin.AdminController;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @Description:
 * @author: liuxuanming
 * @date: 2021/06/16 5:17 下午
 */
@Aspect
@Configuration
public class ControllerAspect {

    @Resource
    private HttpServletRequest httpServletRequest;

    @Resource
    private HttpServletResponse httpServletResponse;

    @Around("execution(* com.hinsliu.dianping.controller.admin.*.*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object adminControllerBeforeValidation(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AdminPermission adminPermission = method.getAnnotation(AdminPermission.class);
        if (adminPermission == null) {
            //公共方法
            Object resultObject = joinPoint.proceed();
            return resultObject;
        }
        //判断当前管理员是否登录
        String email = (String) httpServletRequest.getSession().getAttribute(AdminController.CURRENT_ADMIN_SESSION);
        if (email == null) {
            if (adminPermission.produceType().equals("text/html")) {
                httpServletResponse.sendRedirect("/admin/admin/loginpage");
                return null;
            } else {
                CommonError commonError = new CommonError(EmBusinessError.ADMIN_SHOULD_LOGIN);
                return CommonRes.create(commonError, "fail");
            }

        } else {
            Object resultObject = joinPoint.proceed();
            return resultObject;
        }
    }
}
