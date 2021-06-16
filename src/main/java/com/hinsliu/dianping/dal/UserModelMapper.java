package com.hinsliu.dianping.dal;

import com.hinsliu.dianping.model.UserModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserModel record);

    int insertSelective(UserModel record);

    UserModel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserModel record);

    int updateByPrimaryKey(UserModel record);

    UserModel selectByTelphoneAndPassword(@Param("telphone") String telphone, @Param("password")String password);

    Integer countAllUser();
}