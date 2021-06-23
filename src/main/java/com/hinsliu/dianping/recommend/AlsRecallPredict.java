package com.hinsliu.dianping.recommend;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class AlsRecallPredict {

    public static void main(String[] args) {
        // 初始化spark运行环境
        SparkSession spark = SparkSession.builder().master("local").appName("DianpingApp").getOrCreate();

        // 加载模型进内存
        ALSModel alsModel = ALSModel.load("file:///Users/bytedance/Developer/java/recommend-system/src/main/resources/data/alsmode l");

        // 加载数据集
        JavaRDD<String> csvFile = spark.read().textFile("file:///Users/bytedance/Developer/java/recommend-system/src/main/resources/data/behavior.csv").toJavaRDD();
        JavaRDD<Rating> ratingJavaRDD = csvFile.map((Function<String, Rating>) Rating::parseRating);

        Dataset<Row> rating = spark.createDataFrame(ratingJavaRDD,Rating.class);
        // 给5个用户做离线的召回结果预测
        Dataset<Row> users = rating.select(alsModel.getUserCol()).distinct().limit(5);
        Dataset<Row> userRecs = alsModel.recommendForUserSubset(users,20);

        userRecs.foreachPartition((ForeachPartitionFunction<Row>) t -> {
            // 新建数据库链接
            Connection connection = DriverManager.
                    getConnection("jdbc:mysql://127.0.0.1:3306/dianpingdb?user=root&password=lxmxx1008&useUnicode=true&characterEncoding=UTF-8");
            PreparedStatement preparedStatement = connection.
                    prepareStatement("insert into recommend(id, recommend) values (?, ?)");

            List<Map<String,Object>> data =  new ArrayList<Map<String, Object>>();

            t.forEachRemaining(action->{
                int userId = action.getInt(0);
                List<GenericRowWithSchema> recommendationList = action.getList(1);
                List<Integer> shopIdList = new ArrayList<Integer>();
                recommendationList.forEach(row->{
                    Integer shopId = row.getInt(0);
                    shopIdList.add(shopId);
                });
                String recommendData = StringUtils.join(shopIdList,",");
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("userId",userId);
                map.put("recommend",recommendData);
                data.add(map);
            });

            data.forEach(stringObjectMap -> {
                try {
                    preparedStatement.setInt(1, (Integer) stringObjectMap.get("userId"));
                    preparedStatement.setString(2, (String) stringObjectMap.get("recommend"));

                    preparedStatement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            preparedStatement.executeBatch();
            connection.close();
        });
    }

    @Data
    public static class Rating implements Serializable {

        private int userId;

        private int shopId;

        private int rating;

        public static Rating parseRating(String str){
            str = str.replace("\"","");
            String[] strArr = str.split(",");
            int userId = Integer.parseInt(strArr[0]);
            int shopId = Integer.parseInt(strArr[1]);
            int rating = Integer.parseInt(strArr[2]);
            return new Rating(userId,shopId,rating);
        }

        public Rating(int userId, int shopId, int rating) {
            this.userId = userId;
            this.shopId = shopId;
            this.rating = rating;
        }
    }
}
