package com.hinsliu.dianping.recommend;

import com.hinsliu.dianping.dal.RecommendDOMapper;
import com.hinsliu.dianping.model.RecommendDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendService{

    @Autowired
    private RecommendDOMapper recommendDOMapper;

    // 召回数据，根据userid 召回shopidList
    public List<Integer> recall(Integer userId){
        RecommendDO recommendDO = recommendDOMapper.selectByPrimaryKey(userId);
        if(recommendDO == null){
            // 默认推荐门店，在数据库中添加默认列
            recommendDO = recommendDOMapper.selectByPrimaryKey(9999999);
        }
        String[] shopIdArr = recommendDO.getRecommend().split(",");
        List<Integer> shopIdList = new ArrayList<>();
        for (String s : shopIdArr) {
            shopIdList.add(Integer.valueOf(s));
        }
        return shopIdList;
    }
}
