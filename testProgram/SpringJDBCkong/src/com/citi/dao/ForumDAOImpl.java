package com.citi.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.support.DaoSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.citi.domain.Forum;

public class ForumDAOImpl  implements ForumDAO {

private SimpleJdbcTemplate jdbctemp;

		
	public SimpleJdbcTemplate getJdbctemp() {
	return jdbctemp;
}

public void setJdbctemp(SimpleJdbcTemplate jdbctemp) {
	this.jdbctemp = jdbctemp;
}

	public void insertForum(Forum forum) {
		/**
		 * Specify the statement
		 */
		
		String query = "INSERT INTO citius_SCHEMA.FORUMS (FORUM_ID, FORUM_NAME, FORUM_DESC) VALUES (?,?,?)";
		/**
		 * Specify the values 
		 */
		/*jdbcTemplate.update(query, new Object[] { Integer.valueOf(forum.getForumId()),
				forum.getForumName(), forum.getForumDesc() });*/
		
		jdbctemp.update(query, new Object[] { Integer.valueOf(forum.getForumId()),
				forum.getForumName(), forum.getForumDesc() });
	}
	
	public Forum selectForum(int forumId) {
		/**
		 * Specify the statement
		 */
		String query = "SELECT * FROM citius_SCHEMA.FORUMS WHERE FORUM_ID=?";
		/**
		 * Implement the RowMapper callback interface
		 */
		/*return (Forum) jdbcTemplate.queryForObject(query, new Object[] { Integer.valueOf(forumId) }, 
 				new RowMapper() {
					public Object mapRow(ResultSet resultSet, int rowNum) throws SQLException {
						return new Forum(resultSet.getInt("FORUM_ID"), resultSet.getString("FORUM_NAME"), 
								resultSet.getString("FORUM_DESC"));
					}
				});*/
	
		
		return (Forum) jdbctemp.queryForObject(query, 
 				new RowMapper<Forum>() {
					public Forum mapRow(ResultSet resultSet, int rowNum) throws SQLException {
						return new Forum(resultSet.getInt("FORUM_ID"), resultSet.getString("FORUM_NAME"), 
								resultSet.getString("FORUM_DESC"));
					}
				},new Object[] { Integer.valueOf(forumId) });
		
	}

}
