package com.hinsliu.dianping.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hinsliu.dianping.common.BusinessException;
import com.hinsliu.dianping.common.EmBusinessError;
import com.hinsliu.dianping.dal.ShopModelMapper;
import com.hinsliu.dianping.model.CategoryModel;
import com.hinsliu.dianping.model.SellerModel;
import com.hinsliu.dianping.model.ShopModel;
import com.hinsliu.dianping.recommend.RecommendService;
import com.hinsliu.dianping.recommend.RecommendSortService;
import com.hinsliu.dianping.service.CategoryService;
import com.hinsliu.dianping.service.SellerService;
import com.hinsliu.dianping.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private RestHighLevelClient highLevelClient;

    @Autowired
    private ShopModelMapper shopModelMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private RecommendSortService recommendSortService;

    @Autowired
    private RecommendService recommendService;

    @Override
    @Transactional
    public ShopModel create(ShopModel shopModel) throws BusinessException {
        shopModel.setCreatedAt(new Date());
        shopModel.setUpdatedAt(new Date());

        // 校验商家是否存在正确
        SellerModel sellerModel = sellerService.get(shopModel.getSellerId());
        if (sellerModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商户不存在");
        }

        if (sellerModel.getDisabledFlag() == 1) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商户已禁用");
        }

        // 校验类目
        CategoryModel categoryModel = categoryService.get(shopModel.getCategoryId());
        if (categoryModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "类目不存在");
        }
        shopModelMapper.insertSelective(shopModel);

        return get(shopModel.getId());
    }

    @Override
    public ShopModel get(Integer id) {
        ShopModel shopModel = shopModelMapper.selectByPrimaryKey(id);
        if (shopModel == null) {
            return null;
        }
        shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
        shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        return shopModel;
    }

    @Override
    public List<ShopModel> selectAll() {
        List<ShopModel> shopModelList = shopModelMapper.selectAll();
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

    @Override
    public List<ShopModel> recommend(BigDecimal longitude, BigDecimal latitude) {
        List<Integer> shopIdList = recommendService.recall(148);
        shopIdList = recommendSortService.sort(shopIdList,148);
        List<ShopModel> shopModelList = shopIdList.stream().map(id->{
            ShopModel shopModel = get(id);
            shopModel.setIconUrl("/static/image/shopcover/xchg.jpg");
            shopModel.setDistance(100);
            return shopModel;
        }).collect(Collectors.toList());
        return shopModelList;
    }

    @Override
    public List<Map<String, Object>> searchGroupByTags(String keyword, Integer categoryId, String tags) {
        return shopModelMapper.searchGroupByTags(keyword, categoryId, tags);
    }

    @Override
    public Integer countAllShop() {
        return shopModelMapper.countAllShop();
    }

    @Override
    public List<ShopModel> search(BigDecimal longitude,
                                  BigDecimal latitude, String keyword, Integer orderby,
                                  Integer categoryId, String tags) {
        List<ShopModel> shopModelList = shopModelMapper.search(longitude, latitude, keyword, orderby, categoryId, tags);
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

    @Override
    public Map<String, Object> searchES(BigDecimal longitude, BigDecimal latitude, String keyword, Integer orderby, Integer categoryId, String tags) throws IOException {
        Map<String, Object> result = new HashMap<>();

//        SearchRequest searchRequest = new SearchRequest("shop");
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.query(QueryBuilders.matchQuery("name",keyword));
//        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//        searchRequest.source(sourceBuilder);
//
//        List<Integer> shopIdsList = new ArrayList<>();
//        SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        SearchHit[] hits =  searchResponse.getHits().getHits();
//        for(SearchHit hit : hits){
//            shopIdsList.add(new Integer(hit.getSourceAsMap().get("id").toString()));
//        }

        Request request = new Request("GET", "/shop/_search");

        // 构建请求
        JSONObject requestBody = new JSONObject();

        // 构建source部分
        requestBody.put("_source", "*");

        // 构建自定义距离字段
        requestBody.put("script_fields", new JSONObject());
        requestBody.getJSONObject("script_fields").put("distance", new JSONObject());
        requestBody.getJSONObject("script_fields").getJSONObject("distance").put("script", new JSONObject());
        requestBody.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("source", "haversin(lat, lon, doc['location'].lat, doc['location'].lon)");
        requestBody.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("lang", "expression");
        requestBody.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("params", new JSONObject());
        requestBody.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .getJSONObject("params").put("lat", latitude);
        requestBody.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .getJSONObject("params").put("lon", longitude);

        // 构建query
        Map<String, Object> cixingMap = analyzeCategoryKeyword(keyword);
        boolean isAffectFilter = false;
        boolean isAffectOrder = true;
        requestBody.put("query", new JSONObject());


        // 构建function score
        requestBody.getJSONObject("query").put("function_score", new JSONObject());

        // 构建function score内的query
        requestBody.getJSONObject("query").getJSONObject("function_score").put("query", new JSONObject());
        requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").put("bool", new JSONObject());
        requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool").put("must", new JSONArray());
        requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").add(new JSONObject());

        //构建match query
        int queryIndex = 0;
        if (cixingMap.keySet().size() > 0 && isAffectFilter) {
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("bool", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").put("should", new JSONArray());
            int filterQueryIndex = 0;
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").add(new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .put("match", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").put("name", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").getJSONObject("name").put("query", keyword);
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").getJSONObject("name").put("boost", 0.1);

            for (String key : cixingMap.keySet()) {
                filterQueryIndex++;
                Integer cixingCategoryId = (Integer) cixingMap.get(key);
                requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").add(new JSONObject());
                requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .put("term", new JSONObject());
                requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").put("category_id", new JSONObject());
                requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").getJSONObject("category_id").put("value", cixingCategoryId);
                requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").getJSONObject("category_id").put("boost", 0);
            }


        } else {
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("match", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").put("name", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").getJSONObject("name").put("query", keyword);
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").getJSONObject("name").put("boost", 0.1);

        }

        queryIndex++;
        // 构建第二个query的条件
        requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").add(new JSONObject());
        requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").getJSONObject(queryIndex).put("term", new JSONObject());
        requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("seller_disabled_flag", 0);

        if (tags != null) {
            queryIndex++;
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").add(new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("term", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("tags", tags);
        }
        if (categoryId != null) {
            queryIndex++;
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").add(new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("term", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("category_id", categoryId);
        }


        // 构建functions部分
        requestBody.getJSONObject("query").getJSONObject("function_score").put("functions", new JSONArray());

        int functionIndex = 0;
        if (orderby == null) {
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("gauss", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss").put("location", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("origin", latitude.toString() + "," + longitude.toString());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("scale", "100km");
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("offset", "0km");
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("decay", "0.5");
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 9);

            functionIndex++;
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field", "remark_score");
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.2);

            functionIndex++;
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field", "seller_remark_score");
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.1);


            if (cixingMap.keySet().size() > 0 && isAffectOrder) {
                for (String key : cixingMap.keySet()) {
                    functionIndex++;
                    requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
                    requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("filter", new JSONObject());
                    requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("filter")
                            .put("term", new JSONObject());
                    requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("filter")
                            .getJSONObject("term").put("category_id", cixingMap.get(key));
                    requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 3);

                }

            }


            requestBody.getJSONObject("query").getJSONObject("function_score").put("score_mode", "sum");
            requestBody.getJSONObject("query").getJSONObject("function_score").put("boost_mode", "sum");
        } else {
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
            requestBody.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field", "price_per_man");

            requestBody.getJSONObject("query").getJSONObject("function_score").put("score_mode", "sum");
            requestBody.getJSONObject("query").getJSONObject("function_score").put("boost_mode", "replace");
        }

        // 排序字段
        requestBody.put("sort", new JSONArray());
        requestBody.getJSONArray("sort").add(new JSONObject());
        requestBody.getJSONArray("sort").getJSONObject(0).put("_score", new JSONObject());
        if (orderby == null) {
            requestBody.getJSONArray("sort").getJSONObject(0).getJSONObject("_score").put("order", "desc");
        } else {
            requestBody.getJSONArray("sort").getJSONObject(0).getJSONObject("_score").put("order", "asc");
        }

        // 聚合字段
        requestBody.put("aggs", new JSONObject());
        requestBody.getJSONObject("aggs").put("group_by_tags", new JSONObject());
        requestBody.getJSONObject("aggs").getJSONObject("group_by_tags").put("terms", new JSONObject());
        requestBody.getJSONObject("aggs").getJSONObject("group_by_tags").getJSONObject("terms").put("field", "tags");

        // 得到字符串形式的response body
        String requestBodyStr = requestBody.toJSONString();

        // 设置response body
        request.setJsonEntity(requestBodyStr);

        // 发送请求并得到搜索结果
        Response response = highLevelClient.getLowLevelClient().performRequest(request);

        // 得到搜索结果的对象形式
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject responseBody = JSONObject.parseObject(responseStr);

        // 解析得到每个搜索结果单项
        JSONArray jsonArr = responseBody.getJSONObject("hits").getJSONArray("hits");

        // 解析并获取结果
        List<ShopModel> shopModelList = new ArrayList<>();
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            Integer id = new Integer(jsonObj.get("_id").toString());
            BigDecimal distance = new BigDecimal(jsonObj.getJSONObject("fields").getJSONArray("distance").get(0).toString());
            ShopModel shopModel = get(id);
            shopModel.setDistance(distance.multiply(new BigDecimal(1000).setScale(0, BigDecimal.ROUND_CEILING)).intValue());
            shopModelList.add(shopModel);
        }
        List<Map> tagsList = new ArrayList<>();
        JSONArray tagsJsonArray = responseBody.getJSONObject("aggregations").getJSONObject("group_by_tags").getJSONArray("buckets");
        for (int i = 0; i < tagsJsonArray.size(); i++) {
            JSONObject jsonObj = tagsJsonArray.getJSONObject(i);
            Map<String, Object> tagMap = new HashMap<>();
            tagMap.put("tags", jsonObj.getString("key"));
            tagMap.put("num", jsonObj.getInteger("doc_count"));
            tagsList.add(tagMap);
        }
        result.put("tags", tagsList);
        result.put("shop", shopModelList);

        return result;
    }


    // 构造分词函数识别器
    private Map<String, Object> analyzeCategoryKeyword(String keyword) throws IOException {
        Map<String, Object> res = new HashMap<>();

        Request request = new Request("GET", "/shop/_analyze");
        request.setJsonEntity("{" + "  \"field\": \"name\"," + "  \"text\":\"" + keyword + "\"\n" + "}");
        Response response = highLevelClient.getLowLevelClient().performRequest(request);
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        JSONArray jsonArray = jsonObject.getJSONArray("tokens");
        for (int i = 0; i < jsonArray.size(); i++) {
            String token = jsonArray.getJSONObject(i).getString("token");
            Integer categoryId = getCategoryIdByToken(token);
            if (categoryId != null) {
                res.put(token, categoryId);
            }
        }

        return res;
    }

    private Integer getCategoryIdByToken(String token) {
        for (Integer key : categoryWorkMap.keySet()) {
            List<String> tokenList = categoryWorkMap.get(key);
            if (tokenList.contains(token)) {
                return key;
            }
        }
        return null;
    }

    private Map<Integer, List<String>> categoryWorkMap = new HashMap<>();

    @PostConstruct
    public void init() {
        categoryWorkMap.put(1, new ArrayList<>());
        categoryWorkMap.put(2, new ArrayList<>());

        categoryWorkMap.get(1).add("吃饭");
        categoryWorkMap.get(1).add("下午茶");

        categoryWorkMap.get(2).add("休息");
        categoryWorkMap.get(2).add("睡觉");
        categoryWorkMap.get(2).add("住宿");

    }
}
