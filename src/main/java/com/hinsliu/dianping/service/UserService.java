package com.hinsliu.dianping.service;

import com.hinsliu.dianping.common.BusinessException;
import com.hinsliu.dianping.model.UserModel;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @Description:
 * @author: liuxuanming
 * @date: 2021/06/16 11:05 上午
 */
public interface UserService {

    UserModel getUser(Integer id);

    UserModel register(UserModel registerUser) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException;

    UserModel login(String telphone,String password) throws UnsupportedEncodingException, NoSuchAlgorithmException, BusinessException;

    Integer countAllUser();

}
