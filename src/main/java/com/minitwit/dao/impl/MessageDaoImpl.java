package com.minitwit.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;


import com.minitwit.config.SpSql;
import com.minitwit.dao.MessageDao;
import com.minitwit.model.Message;
import com.minitwit.model.User;
import com.minitwit.util.GravatarUtil;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;

import static jdk.nashorn.internal.objects.NativeDebug.map;
import static org.apache.spark.sql.functions.*;

public class MessageDaoImpl implements MessageDao {
	
	private static final String GRAVATAR_DEFAULT_IMAGE_TYPE = "monsterid";
	private static final int GRAVATAR_SIZE = 48;
	private static SpSql spark;


	public MessageDaoImpl(SpSql spark) {
		this.spark = spark ;
	}

	public List<Message> getTrendingtags(String search){
		String sql = "select row_number() over(order by tag asc) as a,hashtag.tag , COUNT(hashtag.tag) as c from hashtag group by hashtag.tag order by c desc LIMIT "+search;
		return HashtagMapper(spark.get().newSession().sql(sql));
	}


    public List<Message> getSearchUserFollowees(String search){
        String sql = "select message.*,user.* from message, user where " +
                "user.user_id = message.author_id "+
                "and user.user_id in (select followee_id from follower,user " +
                "where username = '" + search +"' and follower_id = user_id )" +
                "and message.message_id in (Select max(message_id) from message group by author_id)"+
                "order by message.pub_date desc";
		spark.get().sql(sql).show();
        return UserMapper(spark.get().newSession().sql(sql));
    }
    public List<Message> getSearchUserFollowers(String search){
        String sql = "select message.*,user.* from message, user where " +
                "user.user_id = message.author_id "+
                "and user.user_id in (select follower_id from follower,user " +
                "where username = '" + search + "' and followee_id = user_id )" +
                "and message.message_id in (Select max(message_id) from message group by author_id)"+
                "order by message.pub_date desc";
		spark.get().sql(sql).show();
        return UserMapper(spark.get().newSession().sql(sql));
    }
    // search by user
	public List<Message> getSearchUser(String search){

		String sql = "select message.*,user.* from message, user where " +
				"user.user_id = message.author_id and user.username like Concat('%','"+search+"','%')"+
				"and message.message_id in (Select max(message_id) from message group by author_id)"+
				"order by message.pub_date desc";
		spark.get().sql(sql).show();
		return UserMapper(spark.get().newSession().sql(sql));
	}

	// search by message

	public List<Message> getSearchMessage(String search){

		String sql = "select message.*,user.* from message, user where " +
				"user.user_id = message.author_id and message.text like Concat('%','"+search+"','%')"+
				"order by message.pub_date desc";

		spark.get().sql(sql).show();
		return messageMapper(spark.get().sql(sql));
	}


	public List<Message> getUserTimelineMessages(User user) {

        
		String sql = "select message.*, user.* from message, user where " +
				"user.user_id = message.author_id and user.user_id = '"+user.getId()+"'" +
				"order by message.pub_date desc";
		spark.get().sql(sql).show();
		return messageMapper(spark.get().sql(sql));
	}

	public List<Message> getUserFullTimelineMessages(User user) {
		String sql = "select message.*, user.* from message, user " +
				"where message.author_id = user.user_id and ( " +
				"user.user_id = '"+user.getId()+"' or " +
				"user.user_id in (select followee_id from follower " +
                                    "where follower_id = '"+user.getId()+"'))" +
                "order by message.pub_date desc";
		spark.get().sql(sql).show();
		return messageMapper(spark.get().sql(sql));
	}


	public List<Message> getPublicTimelineMessages() {

        
		String sql = "select message.*, user.* from message, user " +
				"where message.author_id = user.user_id " +
				"order by message.pub_date desc";
		spark.get().sql(sql).show();
		return messageMapper(spark.get().sql(sql));
	}


	public void insertMessage(Message m) {
        String sql = "insert into message values ('"+spark.MessageID()+"','"+m.getUserId()+"', '"+m.getText()+"', TIMESTAMP '"+m.getPubDateStr()+"', '"+m.getImg()+"')";
        spark.get().sql(sql);

		String str = m.getText() ;
		Pattern MY_PATTERN = Pattern.compile("#(\\S+)");
		Matcher mat = MY_PATTERN.matcher(str);
		while (mat.find()) {
			sql = "insert into Hashtag values ('"+mat.group(1)+"')";
			spark.get().sql(sql);


		}

	}

	private List<Message> messageMapper( Dataset<Row> s) {

		Dataset<Message> res = s.map(
				(MapFunction<Row, Message>) rs -> {
					Message m = new Message();
					m.setId((rs.getAs("message_id")));
					m.setUserId(rs.getAs("author_id"));
					m.setUsername(rs.getAs("username"));
					m.setText(rs.getAs("text"));
					m.setPubDate(rs.getAs("pub_date"));
					m.setGravatar(GravatarUtil.gravatarURL(rs.getAs("email"), GRAVATAR_DEFAULT_IMAGE_TYPE, GRAVATAR_SIZE));
					String img = rs.getAs("img");
					if(img == null || img.isEmpty() || img.equals("null")) {
                        m.setImg(null);
					}else{
                        m.setImg(rs.getAs("img"));
					}
					return m;
				},
				Encoders.bean(Message.class));
		return res.collectAsList();
	}
	private List<Message> UserMapper( Dataset<Row> s) {

		Dataset<Message> res = s.map(
				(MapFunction<Row, Message>) rs -> {
					Message m = new Message();

					m.setId(rs.getAs("message_id"));
					m.setUserId(rs.getAs("author_id"));
					m.setUsername(rs.getAs("username"));
					m.setText(null);
					m.setPubDate(null);
					m.setGravatar(GravatarUtil.gravatarURL(rs.getAs("email"), GRAVATAR_DEFAULT_IMAGE_TYPE, GRAVATAR_SIZE));
					m.setImg(null);

					return m;
				},
				Encoders.bean(Message.class));
		return res.collectAsList();
	}
	private List<Message> HashtagMapper( Dataset<Row> s) {
		Dataset<Message> res = s.map(
				(MapFunction<Row, Message>) rs -> {
					Message m = new Message();
					m.setId(0);
					m.setUserId(0);
					int k = (int)(long)rs.getAs("c");
					if(k == 1) {
						m.setUsername( rs.getAs("a")+ ": Has been used " + rs.getAs("c") + " time.");
					} else {
						m.setUsername( rs.getAs("a")+ ": Has been used " + rs.getAs("c") + " times.");
					}
					m.setText(rs.getAs("tag"));
					m.setPubDate(null);
					m.setGravatar(null);
					m.setImg(null);

					return m;
				},
				Encoders.bean(Message.class));

		return res.collectAsList();
	}
}
