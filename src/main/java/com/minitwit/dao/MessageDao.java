package com.minitwit.dao;

import java.util.List;

import com.minitwit.model.Message;
import com.minitwit.model.User;

public interface MessageDao {
	List<Message> getUserTimelineMessages(User user);

	List<Message> getUserFullTimelineMessages(User user);
	List<Message> getTrendingtags(String i);
	List<Message> getPublicTimelineMessages();

	List<Message> getSearchMessage(String search);
	List<Message> getSearchUserFollowers(String search);
	List<Message> getSearchUserFollowees(String search);
	List<Message> getSearchUser(String search);


	void insertMessage(Message m);
}
