package com.hinsliu.dianping.dal;

import com.hinsliu.dianping.model.RecommendDO;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RecommendDO record);

    int insertSelective(RecommendDO record);

    RecommendDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RecommendDO record);

    int updateByPrimaryKey(RecommendDO record);
}