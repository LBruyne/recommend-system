package com.hinsliu.dianping.recommend;

import lombok.Data;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.Serializable;

//ALS召回算法的训练
public class AlsRecallTrain implements Serializable {

    public static void main(String[] args) throws IOException {
        // 初始化Spark
        SparkSession spark = SparkSession.builder().master("local").appName("DianpingApp").getOrCreate();

        // 读取数据文件
        JavaRDD<String> csvFile = spark.read().textFile("file:///Users/bytedance/Developer/java/recommend-system/src/main/resources/data/behavior.csv").toJavaRDD();

        JavaRDD<Rating> ratingJavaRDD = csvFile.map((Function<String, Rating>) Rating::parseRating);

        Dataset<Row> rating = spark.createDataFrame(ratingJavaRDD, Rating.class);

        // 将所有的rating数据分成数据集和测试集
        Dataset<Row>[] splits = rating.randomSplit(new double[]{0.8, 0.2});
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testingData = splits[1];

        // 过拟合：增大数据规模，减少RANK，增大正则化的系数
        // 欠拟合：增加RANK，减少正则化系数
        ALS als = new ALS()
                    .setMaxIter(10)
                    .setRank(5)
                    .setRegParam(0.01)
                    .setUserCol("userId")
                    .setItemCol("shopId")
                    .setRatingCol("rating");

        // 模型训练
        ALSModel alsModel = als.fit(trainingData);

        // 模型评测
        Dataset<Row> predictions = alsModel.transform(testingData);

        // 均方根误差，预测值与真实值的偏差的平方除以观测次数，开个根号
        RegressionEvaluator evaluator = new RegressionEvaluator()
                                        .setMetricName("rmse")
                                        .setLabelCol("rating")
                                        .setPredictionCol("prediction");
        double rmse = evaluator.evaluate(predictions);
        System.out.println("rmse = " + rmse);

        alsModel.save("file:///Users/bytedance/Developer/java/recommend-system/src/main/resources/data/alsmodel");
    }

    @Data
    public static class Rating implements Serializable {

        private int userId;

        private int shopId;

        private int rating;

        public static Rating parseRating(String str) {
            // 输入为csv中的一行
            str = str.replace("\"", "");
            String[] strArr = str.split(",");
            int userId = Integer.parseInt(strArr[0]);
            int shopId = Integer.parseInt(strArr[1]);
            int rating = Integer.parseInt(strArr[2]);
            return new Rating(userId, shopId, rating);
        }

        public Rating(int userId, int shopId, int rating) {
            this.userId = userId;
            this.shopId = shopId;
            this.rating = rating;
        }
    }
}
