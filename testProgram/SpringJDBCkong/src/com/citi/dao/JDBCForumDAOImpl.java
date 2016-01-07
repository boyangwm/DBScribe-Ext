package com.citi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.citi.domain.Forum;

public class JDBCForumDAOImpl implements ForumDAO {

	private DataSource dataSource;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void insertForum(Forum forum) {
		/**
		 * Specify the statement
		 */
		String query = "INSERT INTO FORUMS (FORUM_ID, FORUM_NAME, FORUM_DESC) VALUES (?,?,?)";
		/**
		 * Define the connection and preparedStatement parameters
		 */
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			/**
			 * Open the connection
			 */
			connection = dataSource.getConnection();
			/**
			 * Prepare the statement
			 */
			preparedStatement = connection.prepareStatement(query);
			/**
			 * Bind the parameters to the PreparedStatement
			 */
			preparedStatement.setInt(1, forum.getForumId());
			preparedStatement.setString(2, forum.getForumName());
			preparedStatement.setString(3, forum.getForumDesc());
			/**
			 * Execute the statement
			 */
			preparedStatement.execute();
		} catch (SQLException e) {
			/**
			 * Handle any exception
			 */
			e.printStackTrace();
		} finally {
			try {
				/**
				 * Close the preparedStatement
				 */
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				/**
				 * Close the connection
				 */
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				/**
				 * Handle any exception
				 */
				e.printStackTrace();
			}
		}

	}
	
	public Forum selectForum(int forumId) {
		/**
		 * Specify the statement
		 */
		String query = "SELECT * FROM FORUMS WHERE FORUM_ID=?";
		/**
		 * Define the connection, preparedStatement and resultSet parameters
		 */
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			/**
			 * Open the connection
			 */
			connection = dataSource.getConnection();
			/**
			 * Prepare the statement
			 */
			preparedStatement = connection.prepareStatement(query);
			/**
			 * Bind the parameters to the PreparedStatement
			 */
			preparedStatement.setInt(1, forumId);
			/**
			 * Execute the statement
			 */
			resultSet = preparedStatement.executeQuery();
			Forum forum = null;
			/**
			 * Extract data from the result set
			 */
			if(resultSet.next())
			{
				forum = new Forum(resultSet.getInt("FORUM_ID"),resultSet.getString("FORUM_NAME"),resultSet.getString("FORUM_DESC"));
			}
			return forum;
		} catch (SQLException e) {
			/**
			 * Handle any exception
			 */
			e.printStackTrace();
		} finally {
			try {
				/**
				 * Close the resultSet
				 */
				if (resultSet != null) {
					resultSet.close();
				}
				/**
				 * Close the preparedStatement
				 */
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				/**
				 * Close the connection
				 */
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				/**
				 * Handle any exception
				 */
				e.printStackTrace();
			}
		}
		return null;
	}

}
