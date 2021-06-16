package com.hinsliu.dianping.dal;

import com.hinsliu.dianping.model.ShopModel;

public interface ShopModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ShopModel record);

    int insertSelective(ShopModel record);

    ShopModel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ShopModel record);

    int updateByPrimaryKey(ShopModel record);
}