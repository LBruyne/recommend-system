package com.hinsliu.dianping.controller;

import com.hinsliu.dianping.common.BusinessException;
import com.hinsliu.dianping.common.CommonRes;
import com.hinsliu.dianping.common.CommonUtil;
import com.hinsliu.dianping.common.EmBusinessError;
import com.hinsliu.dianping.model.UserModel;
import com.hinsliu.dianping.request.LoginReq;
import com.hinsliu.dianping.request.RegisterReq;
import com.hinsliu.dianping.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @Description:
 * @author: liuxuanming
 * @date: 2021/06/16 11:06 上午
 */
@Controller("/user")
@RequestMapping("/user")
public class UserController {

    public static final String CURRENT_USER_SESSION = "currentUserSession";

    @Resource
    private HttpServletRequest httpServletRequest;

    @Resource
    private UserService userService;


    @RequestMapping("/test")
    @ResponseBody
    public String test() {
        return "test";
    }

    @RequestMapping("/index")
    public ModelAndView index() {
        String userName = "lxm";
        ModelAndView modelAndView = new ModelAndView("/index.html");
        modelAndView.addObject("name", userName);
        return modelAndView;
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonRes getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        UserModel userModel = userService.getUser(id);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.NO_OBJECT_FOUND);
        } else {
            return CommonRes.create(userModel);
        }

    }

    @RequestMapping("/register")
    @ResponseBody
    public CommonRes register(@Validated @RequestBody RegisterReq registerReq, BindingResult bindingResult) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if (bindingResult.hasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtil.processErrorString(bindingResult));
        }

        UserModel registerUser = new UserModel();
        registerUser.setTelphone(registerReq.getTelphone());
        registerUser.setPassword(registerReq.getPassword());
        registerUser.setNickName(registerReq.getNickName());
        registerUser.setGender(registerReq.getGender());

        UserModel resUserModel = userService.register(registerUser);

        return CommonRes.create(resUserModel);
    }

    @RequestMapping("/login")
    @ResponseBody
    public CommonRes login(@RequestBody @Validated LoginReq loginReq, BindingResult bindingResult) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if (bindingResult.hasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtil.processErrorString(bindingResult));
        }
        UserModel userModel = userService.login(loginReq.getTelphone(), loginReq.getPassword());
        httpServletRequest.getSession().setAttribute(CURRENT_USER_SESSION, userModel);

        return CommonRes.create(userModel);
    }

    @RequestMapping("/logout")
    @ResponseBody
    public CommonRes logout() throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        httpServletRequest.getSession().invalidate();
        return CommonRes.create(null);
    }

    //获取当前用户信息
    @RequestMapping("/getcurrentuser")
    @ResponseBody
    public CommonRes getCurrentUser() {
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute(CURRENT_USER_SESSION);
        return CommonRes.create(userModel);
    }
}
