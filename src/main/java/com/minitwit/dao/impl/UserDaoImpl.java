package com.minitwit.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.minitwit.config.SpSql;
import com.minitwit.model.Message;
import com.minitwit.util.GravatarUtil;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;


import com.minitwit.dao.UserDao;
import com.minitwit.model.User;


public class UserDaoImpl implements UserDao {

	private static SpSql spark;

	public UserDaoImpl(SpSql spark) {
		this.spark = spark;
	}


	public User getUserbyUsername(String username) {
		String sql = "SELECT * FROM user WHERE username='"+username+"'";
		Dataset<Row> s = spark.get().sql(sql);
		List<User> list = userMapper(spark.get().sql(sql));
		User result = null ;
        if(list != null && !list.isEmpty()) {
        	result = list.get(0);
        }
        
		return result;
	}


	public void insertFollower(User follower, User followee) {

        
		String sql = "insert into follower values ('"+follower.getId()+"', '"+followee.getId()+"')";

		spark.get().sql(sql);
	}


	public void deleteFollower(User follower, User followee) {

        
		String sql = "select * from follower where follower_id != '"+follower.getId()+"' or followee_id != '"+followee.getId()+"'";
		Dataset<Row> followerDS = spark.get().sql(sql);
		followerDS.createOrReplaceTempView("follower");
	}
	

	public boolean isUserFollower(User follower, User followee) {

        
		String sql = "select * from user where user.user_id in( select follower_id from follower where " +
            "follower.follower_id = '"+follower.getId()+"' and follower.followee_id = '"+followee.getId()+"')";
		
		List<User> list = userMapper(spark.get().sql(sql));
		
		return list != null && !list.isEmpty();
	}


	public void registerUser(User user) {

		int j = spark.UserID();
		String sql = "insert into user values ('" + j + "','" + user.getUsername() + "', '" + user.getEmail() + "', '" + user.getPassword() + "')";
		spark.get().sql(sql);
		Message m = new Message();
		int i = spark.MessageID();
		m.setId(i);
		m.setUserId(j);
		sql = "insert into message values ('" + i + "','" + m.getUserId() + "', '" + user.getUsername() + " has joined Berd."+"', '"+new Date()+"', '')";
		spark.get().sql(sql);
		sql = "insert into follower values ('"+j+"','"+j+"')";
		spark.get().sql(sql);
	}

	private List<User> userMapper(Dataset<Row> s) {

		Dataset<User> res = s.map(
				(MapFunction<Row, User>) rs -> {
					User u = new User();
					u.setId(rs.getAs("user_id"));
					u.setEmail(rs.getAs("email"));
					u.setUsername(rs.getAs("username"));
					u.setPassword(rs.getAs("pw"));

					return u;
				},
				Encoders.bean(User.class));
		System.out.print("ok uM");
		return res.collectAsList();
	}
}
