package com.minitwit.config;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SpSql {

    private SparkSession spark;
    public SpSql(){
        spark = SparkSession
                .builder()
                .master("local")
                .appName("Berd")
                .config("spark.some.config.option", "")
                .getOrCreate();
        Dataset<Row> df0 = spark.read().json("follower.json");
        Dataset<Row> df1 = spark.read().json("message.json");
        Dataset<Row> df2 = spark.read().json("user.json");
        Dataset<Row> df3 = spark.read().json("hashtag.json");
        df0.createOrReplaceTempView("follower");
        df1.createOrReplaceTempView("message");
        df2.createOrReplaceTempView("user");
        df3.createOrReplaceTempView("hashtag");
        System.out.print("ok Spsql");

    }
    public SparkSession get(){
        return spark;
    }

}
