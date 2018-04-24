package com.minitwit.dao;

import com.minitwit.model.User;

public interface UserDao {
	void updateUser(User user0,User user1) ;

	User getUserbyUsername(String username);
	
	void insertFollower(User follower, User followee);
	
	void deleteFollower(User follower, User followee);
	
	boolean isUserFollower(User follower, User followee);
	
	void registerUser(User user);
}
