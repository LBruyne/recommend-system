package com.hinsliu.dianping.service;

import com.hinsliu.dianping.common.BusinessException;
import com.hinsliu.dianping.model.SellerModel;
import com.hinsliu.dianping.model.ShopModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Description: 商品服务
 * @author: liuxuanming
 * @date: 2021/06/16 10:13 下午
 */
public interface ShopService {

    ShopModel create(ShopModel shopModel) throws BusinessException;

    ShopModel get(Integer id);

    List<ShopModel> selectAll();

    List<ShopModel> recommend(BigDecimal longitude, BigDecimal latitude);

    List<Map<String, Object>> searchGroupByTags(String keyword, Integer categoryId, String tags);

    Integer countAllShop();

    List<ShopModel> search(BigDecimal longitude, BigDecimal latitude,
                           String keyword, Integer orderby, Integer categoryId, String tags);

}
