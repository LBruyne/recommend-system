package com.hinsliu.dianping.dal;

import com.hinsliu.dianping.model.ShopModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ShopModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ShopModel record);

    int insertSelective(ShopModel record);

    ShopModel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ShopModel record);

    int updateByPrimaryKey(ShopModel record);

    Integer countAllShop();

    List<ShopModel> selectAll();

    List<ShopModel> recommend(@Param("longitude") BigDecimal longitude, @Param("latitude") BigDecimal latitude);

    List<ShopModel> search(@Param("longitude") BigDecimal longitude,
                           @Param("latitude") BigDecimal latitude,
                           @Param("keyword")String keyword,
                           @Param("orderby")Integer orderby,
                           @Param("categoryId")Integer categoryId,
                           @Param("tags")String tags);

    List<Map<String,Object>> searchGroupByTags(@Param("keyword")String keyword,
                                               @Param("categoryId")Integer categoryId,
                                               @Param("tags")String tags);
}