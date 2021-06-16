package com.hinsliu.dianping.service;

import com.hinsliu.dianping.common.BusinessException;
import com.hinsliu.dianping.model.CategoryModel;

import java.util.List;

/**
 * @Description: 品类服务
 * @author: liuxuanming
 * @date: 2021/06/16 10:12 下午
 */
public interface CategoryService {

    CategoryModel create(CategoryModel categoryModel) throws BusinessException;

    CategoryModel get(Integer id);

    List<CategoryModel> selectAll();

    Integer countAllCategory();
}
