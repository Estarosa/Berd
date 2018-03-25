package com.minitwit.service.impl;

import java.util.List;


import com.minitwit.config.SpSql;
import com.minitwit.dao.MessageDao;
import com.minitwit.dao.UserDao;
import com.minitwit.dao.impl.MessageDaoImpl;
import com.minitwit.dao.impl.UserDaoImpl;
import com.minitwit.model.LoginResult;
import com.minitwit.model.Message;
import com.minitwit.model.User;
import com.minitwit.util.PasswordUtil;


public class MiniTwitService {
	

	private UserDao userDao ;
	
	private MessageDao messageDao ;

	public MiniTwitService(){
		SpSql spark = new SpSql();
		userDao = new UserDaoImpl(spark);
		messageDao = new MessageDaoImpl(spark);
		}

	public List<Message> getSearchUser(String search) {
		return messageDao.getSearchUser(search);
	}
	public List<Message> getSearchMessage(String search) {
		return messageDao.getSearchMessage(search);
	}
	public List<Message> getSearchUserFollowers(String search){return messageDao.getSearchUserFollowers(search);}
	public List<Message> getSearchUserFollowees(String search){return messageDao.getSearchUserFollowees(search);}
	public List<Message> getTrendingtags(String i){return messageDao.getTrendingtags(i);}

	public List<Message> getUserFullTimelineMessages(User user) {
		return messageDao.getUserFullTimelineMessages(user);
	}
	
	public List<Message> getUserTimelineMessages(User user) {
		return messageDao.getUserTimelineMessages(user);
	}
	
	public List<Message> getPublicTimelineMessages() {
		return messageDao.getPublicTimelineMessages();
	}
	
	public User getUserbyUsername(String username) {
		return userDao.getUserbyUsername(username);
	}
	
	public void followUser(User follower, User followee) {
		userDao.insertFollower(follower, followee);
	}
	
	public void unfollowUser(User follower, User followee) {
		userDao.deleteFollower(follower, followee);
	}
	
	public boolean isUserFollower(User follower, User followee) {
		return userDao.isUserFollower(follower, followee);
	}
	
	public void addMessage(Message message) {
		messageDao.insertMessage(message);
	}
	
	public LoginResult checkUser(User user) {
		LoginResult result = new LoginResult();
		User userFound = userDao.getUserbyUsername(user.getUsername());
		if(userFound == null) {
			result.setError("Invalid username");
		} else if(!PasswordUtil.verifyPassword(user.getPassword(), userFound.getPassword())) {
			result.setError("Invalid password");
		} else {
			result.setUser(userFound);
		}
		
		return result;
	}
	
	public void registerUser(User user) {
		user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
		userDao.registerUser(user);
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}
}
