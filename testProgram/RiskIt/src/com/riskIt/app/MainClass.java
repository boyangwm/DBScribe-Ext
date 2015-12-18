package com.riskIt.app;

import com.riskIt.db.DatabaseConnection;
import com.riskIt.ui.MenuChoices;

public class MainClass 
{
	
	/**
	 * This is the main method that calls for the display of the
	 * over-all menu of the project and finally closes the 
	 * database connection
	 * @param args - parameters to be passes using command line
	 * 
	 */
  	public static void main(String[] args) 
	{
  			//call for all menu display
  			MenuChoices mc = new MenuChoices();
  			mc.mainMenu();	
  			
  			//close connection to database
  			DatabaseConnection.closeConnection();
	}

}

