package com.citi.dao;

import com.citi.domain.Forum;

public interface ForumDAO {

	public void insertForum(Forum forum);
	public Forum selectForum(int forumId);
	
}
