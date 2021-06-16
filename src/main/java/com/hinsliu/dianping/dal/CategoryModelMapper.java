package com.hinsliu.dianping.dal;

import com.hinsliu.dianping.model.CategoryModel;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CategoryModel record);

    int insertSelective(CategoryModel record);

    CategoryModel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CategoryModel record);

    int updateByPrimaryKey(CategoryModel record);
}