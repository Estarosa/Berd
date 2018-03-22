package com.minitwit.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class MessageDaoImpl implements MessageDao {
	
	private static final String GRAVATAR_DEFAULT_IMAGE_TYPE = "monsterid";
	private static final int GRAVATAR_SIZE = 48;
	private static SpSql spark;


	public MessageDaoImpl(SpSql spark) {
		this.spark = spark ;
	}

	public List<Message> getTrendingtags(String search){
		String sql = "select top" + search + " tag , COUNT(tag) as c from hashtag group by tag order by c desc";
		return HashtagMapper(spark.get().sql(sql));
	}


    public List<Message> getSearchUserFollowees(String search){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vr", search );
        String sql = "select message.*,user.* from message, user where " +
                "user.user_id = message.author_id "+
                "and user.user_id in (select followee_id from follower,user " +
                "where username = :vr and follower_id = user_id )" +
                "and message.message_id in (Select max(message_id) from message group by author_id)"+
                "order by message.pub_date desc";
        List<Message> result = template.query(sql, params, UserMapper);

        return result;
    }
    public List<Message> getSearchUserFollowers(String search){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vr", search );
        String sql = "select message.*,user.* from message, user where " +
                "user.user_id = message.author_id "+
                "and user.user_id in (select follower_id from follower,user " +
                "where username = :vr and followee_id = user_id )" +
                "and message.message_id in (Select max(message_id) from message group by author_id)"+
                "order by message.pub_date desc";
        List<Message> result = template.query(sql, params, UserMapper);

        return result;
    }
    // search by user
	public List<Message> getSearchUser(String search){
	    Map<String, Object> params = new HashMap<String, Object>();
		params.put("vr", search );
		String sql = "select message.*,user.* from message, user where " +
				"user.user_id = message.author_id and user.username like Concat('%',:vr,'%')"+
				"and message.message_id in (Select max(message_id) from message group by author_id)"+
				"order by message.pub_date desc";
		List<Message> result = template.query(sql, params, UserMapper);

		return result;
	}

	// search by message

	public List<Message> getSearchMessage(String search){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("vr", search );
		String sql = "select message.*,user.* from message, user where " +
				"user.user_id = message.author_id and message.text like Concat('%',:vr,'%')"+
				"order by message.pub_date desc";
		List<Message> result = template.query(sql, params, messageMapper);

		return result;
	}

	@Override
	public List<Message> getUserTimelineMessages(User user) {
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", user.getId());
        
		String sql = "select message.*, user.* from message, user where " +
				"user.user_id = message.author_id and user.user_id = :id " +
				"order by message.pub_date desc";
		List<Message> result = template.query(sql, params, messageMapper);
		
		return result;
	}
	@Override
	public List<Message> getUserFullTimelineMessages(User user) {
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", user.getId());
        
		String sql = "select message.*, user.* from message, user " +
				"where message.author_id = user.user_id and ( " +
				"user.user_id = :id or " +
				"user.user_id in (select followee_id from follower " +
                                    "where follower_id = :id))" +
                "order by message.pub_date desc";
		List<Message> result = template.query(sql, params, messageMapper);
		
		return result;
	}

	@Override
	public List<Message> getPublicTimelineMessages() {
		Map<String, Object> params = new HashMap<String, Object>();
        
		String sql = "select message.*, user.* from message, user " +
				"where message.author_id = user.user_id " +
				"order by message.pub_date desc";
		List<Message> result = template.query(sql, params, messageMapper);
		
		return result;
	}

	@Override
	public void insertMessage(Message m) {
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", m.getUserId());
        params.put("text", m.getText());
        params.put("pubDate", m.getPubDate());
        params.put("img",m.getImg());
        String sql = "insert into message (author_id, text, pub_date,img) values (:userId, :text, :pubDate, :img)";
        template.update(sql, params);

		String str = m.getText() ;
		Pattern MY_PATTERN = Pattern.compile("#(\\S+)");
		Matcher mat = MY_PATTERN.matcher(str);
		while (mat.find()) {

			params.put("tag", mat.group(1));
			sql = "insert into Hashtag (tag) values (:tag)";
			template.update(sql, params);

		}

	}
	
	private RowMapper<Message> messageMapper = (rs, rowNum) -> {
		Message m = new Message();
		
		m.setId(rs.getInt("message_id"));
		m.setUserId(rs.getInt("author_id"));
		m.setUsername(rs.getString("username"));
		m.setText(rs.getString("text"));
		m.setPubDate(rs.getTimestamp("pub_date"));
		m.setGravatar(GravatarUtil.gravatarURL(rs.getString("email"), GRAVATAR_DEFAULT_IMAGE_TYPE, GRAVATAR_SIZE));
		m.setImg(rs.getString("img"));

		return m;
	};
	private RowMapper<Message> UserMapper = (rs, rowNum) -> {
		Message m = new Message();

		m.setId(rs.getInt("message_id"));
		m.setUserId(rs.getInt("author_id"));
		m.setUsername(rs.getString("username"));
		m.setText(null);
		m.setPubDate(null);
		m.setGravatar(GravatarUtil.gravatarURL(rs.getString("email"), GRAVATAR_DEFAULT_IMAGE_TYPE, GRAVATAR_SIZE));
		m.setImg(null);

		return m;
	};
	private List<Message> HashtagMapper( Dataset<Row> s) {

		Dataset<Message> res = s.map(
				(MapFunction<Row, Message>) rs -> {
					int count = 0;
					count++;
					Message m = new Message();
					m.setId(0);
					m.setUserId(0);
					if (Integer.parseInt(rs.getAs("c") )== 1) {
						m.setUsername(count + ": Has been used " + rs.getAs("c") + " time.");
					} else {
						m.setUsername(count + ": Has been used " + rs.getAs("c") + " times.");
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
