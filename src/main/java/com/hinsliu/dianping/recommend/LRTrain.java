package com.hinsliu.dianping.recommend;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;

public class LRTrain {

    public static void main(String[] args) throws IOException {
        // 初始化spark运行环境
        SparkSession spark = SparkSession.builder().master("local").appName("DianpingApp").getOrCreate();

        // 加载特征及label训练文件，特征分为离散特征和连续特征，完整的特征在featurevalue.csv中
        // feature.csv中为处理过后的特征向量
        JavaRDD<String> csvFile = spark.read().textFile("file:///Users/bytedance/Developer/java/recommend-system/src/main/resources/data/feature.csv").toJavaRDD();

        // 做转化
        JavaRDD<Row> rowJavaRDD = csvFile.map((Function<String, Row>) v1 -> {
            v1 = v1.replace("\"", "");
            String[] strArr = v1.split(",");
            return RowFactory.create(new Double(strArr[11]), Vectors.dense(Double.parseDouble(strArr[0]), Double.parseDouble(strArr[1]),
                    Double.parseDouble(strArr[2]), Double.parseDouble(strArr[3]), Double.parseDouble(strArr[4]), Double.parseDouble(strArr[5]),
                    Double.parseDouble(strArr[6]), Double.parseDouble(strArr[7]), Double.parseDouble(strArr[8]), Double.parseDouble(strArr[9]), 10.0));
        });
        StructType schema = new StructType(
                new StructField[]{
                        new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
                        new StructField("features", new VectorUDT(), false, Metadata.empty())
                }
        );

        Dataset<Row> data = spark.createDataFrame(rowJavaRDD, schema);

        // 分开训练和测试集
        Dataset<Row>[] dataArr = data.randomSplit(new double[]{0.8, 0.2});
        Dataset<Row> trainData = dataArr[0];
        Dataset<Row> testData = dataArr[1];

        LogisticRegression lr = new LogisticRegression()
                .setMaxIter(10)
                .setRegParam(0.3)
                .setElasticNetParam(0.8)
                .setFamily("multinomial");

        LogisticRegressionModel lrModel = lr.fit(trainData);

        lrModel.save("file:///Users/bytedance/Developer/java/recommend-system/src/main/resources/data/lrmode");

        // 测试评估
        Dataset<Row> predictions = lrModel.transform(testData);

        // 评价指标
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator();
        double accuracy = evaluator.setMetricName("accuracy").evaluate(predictions);
        System.out.println("auc=" + accuracy);
    }
}
