package com.minitwit.config;

import com.minitwit.model.Message;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.eclipse.jetty.websocket.common.frames.DataFrame;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SpSql {

    private SparkSession spark;
    private static int i = 0;
    private static int j = 0;
    public SpSql(){
        String warehouseLocation = new File("spark-warehouse").getAbsolutePath();

      spark = SparkSession
                .builder()
                .master("local")
                .appName("Berd")
                .config("spark.some.config.option", "")
                .enableHiveSupport()
                .getOrCreate();
        spark.sqlContext().setConf("spark.sql.shuffle.partitions","0");
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
        spark.sql("insert into hashtag values ('1')," +
                "('1')," +
                "('1')," +
                "('2')," +
                "('2')," +
                "('3')");
        spark.sql("select * from hashtag").show();
        String sql = "select * from hashtag where tag!='1'";
        Dataset<Row> testDS = spark.sql(sql);
        testDS.show();
       /* try {
            testDS.createGlobalTempView("hashtag");
            System.out.println("ok");
        }catch(Exception e){
            System.out.println("not ok");
        }
        spark.newSession().sql("select * from global_temp.hashtag").show();

        //spark.sql("Alter table hashtag ADD PARTITION (i='1')");
        //spark.sql("ALTER TABLE hashtag DROP PARTITION (i='1')");
        //spark.sql("select * from hashtag").show();


        //spark.sql("insert into user values ('"+UserID()+"','user001','user001@email.com','$2a$10$IHdRfnhNgQesPFD5hrUcMOvyx5RrRcklkpXfs9YX4j1qXvouEeVIa'),"+
          //              "('"+UserID()+"','user002','user002@email.com','$2a$10$NlU0bdBUiegZWZvl6CGpj.wV5YfbDGZ8lYznxWp2NNE4F9cYJJFOe')"
            //    );

        //spark.sql("insert into follower values ('1','2'),('2','1')");
        //spark.sql("insert into follower values ('1','2'),('2','1')");
      //spark.sql("select * from user").show();
      /*  JavaRDD<Message> messageRDD = spark.read()
                .textFile("msg.txt")
                .javaRDD()
                .map(line -> {
                    String[] parts = line.split(",");
                    Message m = new Message();
                    m.setId(Integer.parseInt(parts[0]));
                    m.setUserId(Integer.parseInt(parts[1]));
                    m.setText(parts[2]);
                    //String sql = "insert into message values ('"+this.MessageID()+"','"+parts[1]+"', '"+parts[2]+"', '"+parts[3]+"', '"+"')";
                    //spark.sql(sql);
                    //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    //m.setPubDate(dateFormat.parse(parts[3]));
                    return m;
                });
        System.out.println("okok");
        Dataset<Row> messageDF = spark.createDataFrame(messageRDD, Message.class);
        messageDF.show();
        spark.sql("select * from message").show();
        */
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
