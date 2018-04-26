package com.minitwit.config;

import com.minitwit.model.Message;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.eclipse.jetty.websocket.common.frames.DataFrame;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SpSql {

    private SparkSession spark;
    private static int i = 0;
    private static int j = 0;
    public SpSql(){


      spark = SparkSession
                .builder()
                .master("local")
                .appName("Berd")
                .config("spark.some.config.option", "")
                .enableHiveSupport()
                .getOrCreate();
        spark.sqlContext().setConf("spark.sql.shuffle.partitions","0");
        String warehouseLocation = new File("spark-warehouse").getAbsolutePath();
       spark.sql("drop table if exists message");
        spark.sql("create table message ("+
                "message_id integer ,"+
                "author_id integer ,"+
                "text varchar(160) ,"+
                "pub_date timestamp,"+
                "img varchar(160)"+
                 ")");
          spark.sql("drop table if exists hashtag");
        spark.sql("create table hashtag ("+
                        "tag varchar(160)"+
                        ")");
        spark.sql("drop table if exists follower");
        spark.sql("create table follower ("+
                "follower_id integer,"+
                "followee_id integer"+
        ")" );
        spark.sql("drop table if exists user");
        spark.sql("create table user (" +
                "  user_id integer," +
                "  username varchar(50) ," +
                "  email varchar(50) ," +
                "  pw varchar(255) " +
                ")");
        spark.sql("select * from message").printSchema();
        spark.sql("select * from follower").printSchema();
        spark.sql("select * from user").printSchema();
        spark.sql("select * from hashtag").printSchema();
    }
    public SparkSession get(){
        return spark;
    }
    public int UserID(){
        i++;
        return i;
    }
    public int MessageID(){
        j++;
        return j;
    }
}
