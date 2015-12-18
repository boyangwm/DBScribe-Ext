package com.riskIt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * This class is used to establish a database connection as well as 
 * close the connection
 * @author Poornima Tantry
 *
 */
public class DatabaseConnection 
{
	private static final String driver = "org.apache.derby.jdbc.ClientDriver";
	 
	private static final String URL = "jdbc:mysql://honeypot:33061/preist_riskinsurance";	
	private static Connection conn;
	private static boolean created = false;
	
	private DatabaseConnection()
	{	
		
	}
	
	public static Connection  getConnection() throws SQLException
	 {	    
		if(created)
		{
			return conn;
		}
		else
		{
			try 
				{
				    Class.forName(driver).newInstance();
				    conn = DriverManager.getConnection(URL,"root","--password--");
				    created = true;
				    System.out.println("connected using " + URL);
				}
				catch(Exception e)
				{
					System.out.println("Exception in CreateConnection");
					e.printStackTrace();
				}			
		        return conn;
		}
	 }	
	
	public static void closeConnection()
	{
		try
		{
			if(created)
			{
				created = false;
				conn.close();
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in closeCOnnection");
			e.printStackTrace();
		}
		
	}
	

}
