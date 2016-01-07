package com.citi.dao;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import com.citi.domain.Forum;

public class Main {

	public static void main(String[] args) {
		 ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		System.out.println("Hello");
		
		//SimpleJdbcTemplate sp=(SimpleJdbcTemplate)	context.getBean("jdbcT");
		 //int a=sp.update("delete from citi_SCHEMA.Employee where Empid=?",1);
		 //System.out.println(a);
		ForumDAO forumDAO = (ForumDAO) context.getBean("forumDAO");
		//Forum springForum = new Forum(8,"SpringGlodeop", "Di Spring");
		//forumDAO.insertForum(springForum);		
		System.out.println(forumDAO.selectForum(8).getForumName());
		
	}
	
}
