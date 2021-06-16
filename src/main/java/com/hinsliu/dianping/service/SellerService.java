package com.hinsliu.dianping.service;

import com.hinsliu.dianping.common.BusinessException;
import com.hinsliu.dianping.model.SellerModel;

import java.util.List;

/**
 * @Description: 卖家相关Service
 * @author: liuxuanming
 * @date: 2021/06/16 6:15 下午
 */
public interface SellerService {

    SellerModel create(SellerModel sellerModel);

    SellerModel get(Integer id);

    List<SellerModel> selectAll();

    SellerModel changeStatus(Integer id, Integer disabledFlag) throws BusinessException;

}
