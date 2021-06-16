package com.hinsliu.dianping.dal;

import com.hinsliu.dianping.model.SellerModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SellerModel record);

    int insertSelective(SellerModel record);

    SellerModel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SellerModel record);

    int updateByPrimaryKey(SellerModel record);

    List<SellerModel> selectAll();

    Integer countAllSeller();
}