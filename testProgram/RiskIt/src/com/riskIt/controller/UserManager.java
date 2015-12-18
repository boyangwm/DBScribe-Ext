package com.riskIt.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import tada.TaDaMethod;

import com.riskIt.db.DatabaseConnection;
import com.riskIt.util.AccessorMethods;
import com.riskIt.util.RiskItConstants;
import com.riskIt.data.Invitation;
import com.riskIt.controller.AgentManager;

public class UserManager 
{
	private AccessorMethods acc;
	private AgentManager agent;
	
	public UserManager()
	{
		acc = new AccessorMethods();
		agent = new AgentManager();
	}
	
	/**
	 * This method filters data from "user-record" table that matches same 
	 * zipcode and displays the data.
	 * @param zip - holds the value of zip-code to be compared with and filtered
	 * @return count - number of people living in this zip-code
	 * @author Poornima Tantry
	 */
	public int filterZipcode(String zip)
	{
		int count = 0;
		Statement stat = null;
		ResultSet result = null;
		
		if(zip.length() == 0 || zip.equals("null") )
			zip = null;
		
		String cmd_zipSearch = "SELECT * from userrecord where zip = '" + zip + "'";
		
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
					
			result = stat.executeQuery(cmd_zipSearch);
			
			System.out.println("List of customers for zipcode : " + zip);
			System.out.printf("%20s |%20s |" ,"NAME", "SSN");
			System.out.println("");
			
			//display data from result set
			while(result.next())
			{
				++count;
				System.out.printf("%s |%s |", result.getString("name"), result.getString("ssn"));
				System.out.println("");
			}
			
			if(count == 0)
				System.out.println("There are no customers enrolled in this zipcode");
			else
				System.out.println("No. of customers in zipcode : " + zip + " is " + count);
			result.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception in filterZipcode");
			e.printStackTrace();
		}
		return count;
		
	}
	
	/**
	 * This method accepts name of the state and calculates number of 
	 * people employment within the state as well as number of people unemployed 
	 * within the state and gives the percentage of unemployment rate..
	 * @param stateName - holds value of the state
	 * @return ratePercent - return the unemployment rate for the given state
	 * @author Poornima Tantry
	 */
	
	@TaDaMethod(variablesToTrack= {"countOfAllEmployees", "countOfAllEmployees",
			"countOfUnemployed", "countOfUnemployed", "countOfUnemployed", "countOfUnemployed", "countOfUnemployed"}
	, correspondingDatabaseAttribute = {"ziptable.zip", "userrecord.zip",
			"employmentStat.ssn", "userrecord.ssn", "ziptable.statename", "ziptable.zip", "userrecord.zip"})
			
	public double calculateUnEmploymentRate(String stateName)
	{
		int countOfAllEmployees = 0;
		int countOfUnemployed = 0;
		double ratePercent = 0.0;
		
		Statement stat = null;
		ResultSet result = null;
		
		//validate input
		stateName = acc.checkLetter(stateName);
		
		if(stateName.equals(""))
			System.out.println("You have entered a incorrect value.");
		else
		{
			try
			{
				Connection conn = DatabaseConnection.getConnection();
				stat = conn.createStatement();
					
				String cmd_getAllUnemployed = "SELECT count(*) as totalUnemploy "
											+ " FROM ziptable, employmentStat, userrecord "
											+ " WHERE employmentStat.ssn = userrecord.ssn " 
											+ "and employmentstat.unemploymentreason <> 'null' "
											+ "and ziptable.statename = '" + stateName + "' "
											+ "and ziptable.zip = userrecord.zip";
				
				String cmd_getTotalCount = "SELECT count(*) as totalCount FROM  ziptable, userrecord "
											+ " WHERE ziptable.statename = '" + stateName + "' and ziptable.zip = userrecord.zip";
				
				result = stat.executeQuery(cmd_getTotalCount);		
				
				if(result.next())
					countOfAllEmployees = result.getInt("totalCount");			
				result.close();
			
				System.out.println("Total number of people working for the state of "
									+ stateName + " is " + countOfAllEmployees);
				
				//check if total count is 0, 
				//then set it to 1, to avoid division by zero
				if(countOfAllEmployees == 0)
					countOfAllEmployees = 1;
				
				result = stat.executeQuery(cmd_getAllUnemployed);		
				
				if(result.next())
					countOfUnemployed = result.getInt("totalUnemploy");		
				result.close();		
				
				System.out.println("Total number of unemployed people for the state of "
						+ stateName + " is " + countOfUnemployed);	
				
				//calculate the unemployment rate
				ratePercent = ((double)countOfUnemployed/countOfAllEmployees)*100;
				
				//get the precision to 2 digits
				ratePercent = ratePercent * 100;
				ratePercent = Math.round(ratePercent);
				ratePercent = ratePercent/100;
			
				System.out.printf("The unemployment rate for the state of %s is %3.2f", stateName,ratePercent);
				System.out.println();
				stat.close();
			}
			catch(Exception e)
			{
				System.out.println("Catch in calculateUnemployment");
				e.printStackTrace();
			}
		}
		return ratePercent;
	}
	
	/**
	 * This method is used to obtain set of results that matches the 
	 * given education level and display the customer's list 
	 * @param edu - holds the value of education to be compared with
	 * @return count - number of people holding this education
	 * @author Poornima Tantry
	 */
	public int filterEducation(String edu)
	{
		int count = 0;
		Statement stat = null;
		ResultSet result = null;
		
		if(edu.length() == 0)
			edu = null;
		if(edu.equals("null"))
			edu = null;
		
		String cmd_eduSearch = "SELECT userrecord.ssn, userrecord.name, " 
								+ " education.education "
								+ " from userrecord, education where "
								+ " userrecord.ssn = education.ssn and "
								+ " education.education = ' " + edu + "'";
		
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			result = stat.executeQuery(cmd_eduSearch);
			
			System.out.println("List of customers for given education : " + edu);
			System.out.printf("%20s |%20s |" ,"NAME", "SSN");
			System.out.println("");
			
			//display data from result set
			while(result.next())
			{
				++count;
				System.out.printf("%s |%s |", result.getString("name"), result.getString("ssn"));
				System.out.println("");
			}
			
			if(count == 0)
				System.out.println("There are no customers enrolled with this education");
			else
				System.out.println("No. of customers with education : " + edu + " is " + count);
			result.close();
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in filterEducation");
			e.printStackTrace();
		}
		
		return count;
	}
	
	
	/**
	 * This method displays customer's list that is a match with resepct to the 
	 * specified occupation
	 * @param occupation - holds the value of the occupation name to be compared with
	 * @return count - count of number of people with the given occupation
	 * @author Poornima Tantry
	 */
	
	public int filterOccupation(String occupation)
	{
		
		int count = 0;
		Statement stat = null;
		ResultSet result = null;
		
		if(occupation.length() == 0)
			occupation = null;
		if(occupation.equals("null"))
			occupation = null;
		
		String cmd_OccupationSearch = "SELECT userrecord.ssn, userrecord.name, " 
									+ " job.occupationcode "
									+ " from userrecord, job, occupation where "
									+ " userrecord.ssn = job.ssn and "
									+ " job.occupationcode = occupation.occupationcode and "
									+ " occupation.occupation = ' " + occupation + "'";
		
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			
			result = stat.executeQuery(cmd_OccupationSearch);
			
			System.out.println("List of customers for given Occupation : " + occupation);
			System.out.printf("%20s |%20s |" ,"NAME", "SSN");
			System.out.println("");
			
			//display data from result set
			while(result.next())
			{
				++count;
				System.out.printf("%s -- %s ", result.getString("name"), result.getString("ssn"));
				System.out.println("");
			}
			
			if(count == 0)
				System.out.println("There are no customers enrolled with this occupation");
			else
				System.out.println("No. of customers with occupation : " + occupation + " is " + count);
			result.close();
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in filterOccupation");
			e.printStackTrace();
		}
		
		return count;
	}
	
	
	/**
	 * This method filters customers based on the marital status and prints the results.
	 * @param status - holds value of the marital status to be compared with
	 * @return count - number of people with the given marital status
	 * @author Poornima Tantry
	 */
	
	public int filterMaritalStatus(String status)
	{
		int count = 0;
		Statement stat = null;
		ResultSet result = null;
		
		if(status.length() == 0)
			status = null;
		if(status.equals("null"))
			status = null;
		
		String cmd_statusSearch = "SELECT * from userrecord where marital = ' " + status + "'";
		
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			result = stat.executeQuery(cmd_statusSearch);
			
			System.out.println("List of customers for given Marital Status : " + status);
			System.out.printf("%20s |%20s |" ,"NAME", "SSN");
			System.out.println("");
			
			//display data from result set
			while(result.next())
			{
				++count;
				System.out.printf("%s -- %s ", result.getString("name"), result.getString("ssn"));
				System.out.println("");
			}
			
			if(count == 0)
				System.out.println("There are no customers enrolled with this status");
			else
				System.out.println("No. of customers with marital status : " + status + " is " + count);
			result.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception in filterMaritalStatus");
			e.printStackTrace();
		}
		
		return count;
	}
	
	/**
	 * This method accepts a estimated range of income and then finds how
	 * many people are there in this range of income and lists them
	 * @param getIncome - holds estimated income value to be compared with
	 * @return count - number of people in this income range
	 * @author Poornima Tantry
	 */
	
	@TaDaMethod(variablesToTrack = {"dataIncome", "dataIncome", "dataIncome", "dataIncome", "dataIncome"},
			correspondingDatabaseAttribute = {"job.workweeks", "job.weekwage", "investment.capitalgains", 
			"investment.capitallosses", "investment.stockdividends"})
	public int filterEstimatedIncome(String getIncome)
	{
		double income = 0, dataIncome = 0;	
		int count = 0;
		Statement stat = null;
		ResultSet result = null;		
		
		
		income = acc.checkLetterDigit(getIncome, RiskItConstants.INVALID_VALUE);
		
		String cmd_filterIncome = "SELECT  userrecord.name, userrecord.ssn, investment.capitalgains, " 
								+ "investment.capitallosses, investment.stockdividends, " 
								+ "job.workweeks, job.weekwage " 
								+ "FROM  investment, userrecord, job WHERE "
								+ " investment.ssn = userrecord.ssn and "
								+ " job.ssn = userrecord.ssn";
			
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			result = stat.executeQuery(cmd_filterIncome);
			
			System.out.println("List of customers for given Estimated Income : " + income);
			System.out.printf("%20s |%20s |" ,"NAME", "SSN");
			System.out.println("");
			
			//display data from result set
			while(result.next())
			{		
				dataIncome = acc.calculateIncome(result.getInt("workweeks"),
												 result.getInt("weekwage"),
												 result.getInt("capitalGains"),
												 result.getInt("capitallosses"),
												 result.getInt("stockdividends"));
				if(Math.floor(dataIncome) == Math.floor(income))
				{
					++count;
					System.out.printf("%s -- %s ", result.getString("name"), result.getString("ssn"));
					System.out.println("");
				}
			}
			
			if(count == 0)
				System.out.println("There are no customers enrolled with this estimated income");
			else
				System.out.println("No. of customers with estimated income : " + income + " is " + count);
			result.close();
		}
		catch(Exception e)
		{
			System.out.println("Exception in filterEstimatedIncome");
			e.printStackTrace();
		}
		return count;
	}	
	
	/**
	 * This method accepts any number of inputs and produces a filtered result of customers
	 * @param prop - arraylist containing properties of user input that will used as filter 
	 * @return count - number of customers present that match the given criteria
	 * @author Poornima Tantry
	 */
	public int browseUserProperties(ArrayList<String> prop)
	{
		int count = 0;
		Statement stat = null;
		ResultSet result = null;
		
		String cmd_generalSearch = "Select * from userrecord where ";
		String cmd_userSQL = "";
		
		boolean checkDataFlag = false;
		for(int i=0; i < prop.size(); ++i)
		{
			if(prop.get(i).length() > 0)
				checkDataFlag = true;
		}
		int index =0;
		
		//check to see which data are available to build the SQL query
		
		if(checkDataFlag)
		{
			//name
			if(prop.get(index).length() > 0)
				cmd_userSQL = cmd_userSQL + "name = '" + prop.get(index) + "'";
			++index;
			
			
			//ssn
			if(prop.get(index).length() > 0)
			{
				String andJoin = "";
				if(cmd_userSQL.length() > 0)
					andJoin = " and ";
				cmd_userSQL = cmd_userSQL + andJoin + " ssn = " + prop.get(index) ;
				
			}
			++index;
			
			//age
			if(prop.get(index).length() > 0)
			{
				String andJoin = "";
				if(cmd_userSQL.length() > 0)
					andJoin = " and ";
				cmd_userSQL = cmd_userSQL + andJoin + " age = " + prop.get(index) ;
				
			}
			++index;
			
			//sex
			if(prop.get(index).length() > 0)
			{
				String andJoin = "";
				if(cmd_userSQL.length() > 0)
					andJoin = " and ";
				
				cmd_userSQL = cmd_userSQL + andJoin + " sex = ' " + prop.get(index) + "'";
				
			}
			++index;
			
			//marital-status
			if(prop.get(index).length() > 0)
			{
				String andJoin = "";
				if(cmd_userSQL.length() > 0)
					andJoin = " and ";
				
				cmd_userSQL = cmd_userSQL + andJoin + " marital = ' " + prop.get(index) + "'";
				
			}
			++index;
			
			//race
			if(prop.get(index).length() > 0)
			{
				String andJoin = "";
				if(cmd_userSQL.length() > 0)
					andJoin = " and ";
				
				cmd_userSQL = cmd_userSQL + andJoin + " race = ' " + prop.get(index) + "'";			
			}
			++index;
			
			//country
			if(prop.get(index).length() > 0)
			{
				String andJoin = "";
				if(cmd_userSQL.length() > 0)
					andJoin = " and ";
				cmd_userSQL = cmd_userSQL + andJoin + " birthcountry = ' " + prop.get(index) + "'";
				
			}
			++index;	
			
			cmd_generalSearch = cmd_generalSearch +  cmd_userSQL;
				
			try
			{
				Connection conn = DatabaseConnection.getConnection();
				stat = conn.createStatement();
				result = stat.executeQuery(cmd_generalSearch);						
				
				while(result.next())
				{
					++count;
					
					System.out.println(   result.getString("name")+ "->" 
										+ result.getInt("ssn") + "->" 
										+ result.getString("age") + "->"
										+ result.getString("sex") + "->"
										+ result.getString("marital") + "->"
										+ result.getString("race") + "->"
										+ result.getString("birthcountry"));				
				}
				result.close();
			}					
			catch(Exception e)
			{
				System.out.println("Exception in browseUserProperties");
				e.printStackTrace();
			}
		}
		else
			System.out.println("There is no data to filter out customers");
		
		return count;
	}
	
	/**
	 * This method calculates a quote for a customer, when they choose to find
	 * quotes by themselves. This method will accept the net worth of the property
	 *  with all expenses and and deductibles and gives a value.
	 * @param ssn - SSN of the customer
	 * @param deductible - deductible that customer wishes to pay
	 * @param netWorth - worth of all of property
	 * @param userList - list of all customers, if available , to match data
	 * @return calc_invite - user object with all calculated values
	 * @author Poornima Tantry
	 */
	
	public Invitation calculateQuoteForCustomer(long ssn, double deductible, 
											double netWorth, 
											ArrayList<Invitation> userList)
	{
		double quote = 0;
		Invitation inv = new Invitation();
		Invitation calc_invite = new Invitation();
		int deduct_list[] = {100, 250, 500,1000, 1500, 2000, 2500, 5000, 10000};
		boolean checkFlag = false;
		
		//check to see if customer is already present in lsit
		for(int i=0; i < userList.size(); ++i)
		{
			System.out.println("flow:10");
			inv = userList.get(i);
			if(inv.getUserSSN() == ssn)
			{
				System.out.println("flow:11");
				checkFlag = true;
				break;
			}
		}
		
		if(checkFlag)
		{
			System.out.println("flow:12");
			System.out.println("You have received $" + inv.getPremium() +
					" for the property value of " + inv.getPropertyValue() +
					" and deductible of $" + inv.getDeductible() + "\n\n");
		}
		System.out.println("For the expenses that you have provided, the quote will be ...");
			
		for(int i=0; i < deduct_list.length; ++i)
		{
			System.out.println("flow:13");
			//set all the values
			calc_invite.setUserSSN(ssn);
			calc_invite.setDeductible(deduct_list[i]);
			calc_invite.setPropertyValue(netWorth);
			
			//assume the property exposure unit is 30% of the property value
			calc_invite.setExpoUnit(netWorth * RiskItConstants.THIRTY_PERCENT/100);
			
			//assume loss coverage is 50% of the total expense and property value
			calc_invite.setlossCoverage(netWorth * RiskItConstants.FIFTY_PERCENT/100);
			
			//calculate the score of customer
			double score = agent.calculateScore(ssn);
			calc_invite.setScore(score);
			
			//calculate the final quote
			quote = agent.calculateTotalValue(calc_invite);
			calc_invite.setPremium(quote);
			
			System.out.println("The quote for deductible of $" + deduct_list[i] + " is : $" + quote + " per month.");
		}
		System.out.println("---------------------------------------------------");
		return calc_invite;
	}
	
	public double updatewagetable(int industrycode,int occupcode){
		int sum = 0;		
		int count_industrycode = 0;
		int count = 0;
		double average = 0;
		int meanweekwage = 0;
		Statement stat = null;
		ResultSet result = null;
		
		try
        {
        	
            Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
            stat=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
          
            String select_occupcode = "select  wage.occupationcode,wage.industrycode,weekwage from wage,job where wage.occupationcode = job.occupationcode and wage.industrycode = job.industrycode and wage.occupationcode = "  + occupcode + " and wage.industrycode = " + industrycode+ " group by wage.occupationcode,wage.industrycode,weekwage";
            String select_industrycode = "select  industrycode from wage where industrycode = " + industrycode +"";         
            String query = "select meanweekwage,weekwage from job,wage where wage.occupationcode = job.occupationcode and wage.industrycode = job.industrycode and wage.industrycode = " + industrycode +" and wage.occupationcode =  " + occupcode +" group by wage.occupationcode,wage.industrycode,weekwage,meanweekwage";    
            String updatetable = "select industrycode, occupationcode,meanweekwage from wage where industrycode = " + industrycode +" and occupationcode = " + occupcode+"";
            
            
           // Total number of occupation code
            result = stat.executeQuery(select_occupcode);
            while(result.next())
        	      count++;
                  //System.out.println(count);
                            
           result = stat.executeQuery(select_industrycode);
          //Total number of industry code
           while(result.next())
        	    count_industrycode++;
                       
          result = stat.executeQuery(query);
          while(result.next())
               sum = sum + (result.getInt("weekwage"));            
               average = (double)sum/(double)count;             
               System.out.println("Industrycode" + "  OccupationCode" +"     Sum" +"      Average");
               System.out.println( industrycode + "              " + "  " + occupcode + "           " + sum +"       " +  average);
       
          if (result.getConcurrency() == ResultSet.CONCUR_UPDATABLE) {
              System.out.println("ResultSet non-updatable.");
          } else {
             System.out.println("ResultSet updatable.");
          }
          result = stat.executeQuery(updatetable);
          result.beforeFirst();         
         while( result.next()){      
               result.updateDouble("meanweekwage",average);    	
               result.updateRow();       
        }
        
      acc.printRs(result);
       // result.close();
      
        System.out.println("-------------------------------------------------------------------");               
         System.out.println("meanweekwage is updated for industry code: " + industrycode +  " and  Occupation code: " + occupcode + " Old value for meanweekwage is:  " + meanweekwage + " and new value for meanweekwage is:  " + (int)average );   
        stat.close();
           
        }
        catch(Exception e){
         
            e.printStackTrace();
        }
    
		return average;
	}
	
	public double updatetable(long ssn,int industrycode,int occupationcode){
		int sum = 0;		
		int count_industrycode = 0;
		int count = 0;
		int countwage = 0;
		double average = 0;
		Statement stat = null;
		ResultSet result = null;
		try
        {
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
            stat=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                      ResultSet.CONCUR_UPDATABLE);
          
            String select_occupcode = "select  wage.occupationcode,wage.industrycode,weekwage from wage,job where wage.occupationcode = job.occupationcode and wage.industrycode = job.industrycode and wage.industrycode = " + industrycode+" and wage.occupationcode = " + occupationcode+"  group by wage.occupationcode,wage.industrycode,weekwage";
            String select_industrycode = "select  industrycode from wage ";         
            String query = "select meanweekwage,weekwage from job,wage where wage.occupationcode = job.occupationcode and wage.industrycode = job.industrycode and wage.industrycode = " + industrycode+" and wage.occupationcode = " + occupationcode+"   group by wage.occupationcode,wage.industrycode,weekwage,meanweekwage";    
            String updatetable = "select industrycode, occupationcode,meanweekwage from wage where industrycode = " + industrycode+" and occupationcode = " + occupationcode+" ";
            String wage = "select wage.industrycode,wage.occupationcode,job.ssn,weekwage,unemploymentreason from job,wage,employmentstat where job.ssn = employmentstat.ssn and wage.industrycode = job.industrycode and wage.occupationcode = job.occupationcode  and job.ssn = " +ssn+""; //group by job.industrycode,job.occupationcode,job.ssn,weekwage,unemploymentreason";
            
           // Total number of occupation code
            result = stat.executeQuery(select_occupcode);
            while(result.next())
        	   count++;
               System.out.println( "Total number of occupation code and industrycode " + count);
               result.close();             
           result = stat.executeQuery(select_industrycode);
         //Total number of industry code
           while(result.next())
        	    count_industrycode++;
                      
            result = stat.executeQuery(query);
            while(result.next())
        	  sum = sum + (result.getInt("weekwage"));     
          double oldaverage = (double)sum/(double)(count-1);
              average = (double)sum/(double)(count);    
              
              System.out.println("ssn "+  ssn + "  Sum  " + sum +" Average " +  average  ) ;            
              result = stat.executeQuery(wage);
            
            while(result.next()){
               countwage++;
               int weekwage = result.getInt("weekwage");
               String reason = result.getString("unemploymentreason");
               //int ssn = result.getInt(3);
               System.out.println("Number of row  "  + countwage + "  ssn  "  +  ssn + "  weekwage  " +  weekwage+ "   unemploymentreason  " + reason);
            }
            	 
              
             if (result.getConcurrency() == ResultSet.CONCUR_UPDATABLE) {
                 System.out.println("ResultSet non-updatable.");
             } else {
               System.out.println("ResultSet updatable.");
             }
             
            result = stat.executeQuery(updatetable);
            result.beforeFirst();         
            while( result.next()){      
             result.updateDouble("meanweekwage",average);   	
             result.updateRow();       
           }
        
        acc.printRs(result);
      
        System.out.println("-------------------------------------------------------------------");               
        System.out.println("meanweekwage is updated for  ssn  = " + ssn + " industry code: " + industrycode +  " and  Occupation code: " + occupationcode + " Old value for meanweekwage is:  " + (int)oldaverage + " and new value for meanweekwage is:  " + (int)average );   
        stat.close();
      
       }
        catch(Exception e){
         
            e.printStackTrace();
        }
		
		return average;
	}
	
	public double userinformation(long ssn,int industrycode,int occupationcode){
		int sum = 0;		
		int count_industrycode = 0;
		int count = 0;
		int countwage = 0;
		double average = 0;
		Statement stat = null;
		ResultSet result = null;
		 try
	        {
			    Connection conn = DatabaseConnection.getConnection();				
	            stat = conn.createStatement();
	            stat=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
	                                      ResultSet.CONCUR_UPDATABLE);
	          
	            String select_occupcode = "select  wage.occupationcode,wage.industrycode,weekwage from wage,job where wage.occupationcode = job.occupationcode and wage.industrycode = job.industrycode and wage.industrycode = " + industrycode+" and wage.occupationcode = " + occupationcode+"  group by wage.occupationcode,wage.industrycode,weekwage";
	            String select_industrycode = "select  industrycode from wage ";         
	            String query = "select meanweekwage,weekwage from job,wage where wage.occupationcode = job.occupationcode and wage.industrycode = job.industrycode and wage.industrycode = " + industrycode+" and wage.occupationcode = " + occupationcode+"   group by wage.occupationcode,wage.industrycode,weekwage,meanweekwage";    
	            String updatetable = "select ssn,workclass,industrycode, occupationcode,unionmember,employersize,weekwage,selfemployed,workweeks from job where industrycode = " + industrycode+" and occupationcode = " + occupationcode+" and job.ssn = " + ssn+ "";
	            String wage = "select job.industrycode,job.occupationcode,job.ssn,weekwage,unemploymentreason from job,wage,employmentstat where job.ssn = employmentstat.ssn and wage.industrycode = job.industrycode and wage.occupationcode = job.occupationcode  and job.ssn = " +ssn+""; //group by job.industrycode,job.occupationcode,job.ssn,weekwage,unemploymentreason";
	            
	           // Total number of occupation code
	            result = stat.executeQuery(select_occupcode);
	            while(result.next())
	        	      count++;
	                  System.out.println( "Total number of occupation code and industrycode " + count);
	                              
	           result = stat.executeQuery(select_industrycode);
	          //Total number of industry code
	           while(result.next())
	        	     count_industrycode++;
	                          
	           result = stat.executeQuery(query);
	           while(result.next())
	        	    sum = sum + (result.getInt("weekwage"));            
	                average = (double)sum/(double)count;             
	                System.out.println("ssn "+  ssn + "  Sum  " + sum +" Average " +  average  ) ;           
	           result = stat.executeQuery(wage);
	           while(result.next()){
	            	 countwage++;
	            	 int weekwage = result.getInt("weekwage");
	            	 String reason = result.getString("unemploymentreason");
	            	 //int ssn = result.getInt(3);
	            	 System.out.println("Number of row  "  + countwage + "  ssn  "  +  ssn + "  weekwage  " +  weekwage+ "   unemploymentreason  " + reason);
	          }
	            	               
	          if (result.getConcurrency() == ResultSet.CONCUR_UPDATABLE) {
	              System.out.println("ResultSet non-updatable.");
	          } else {
	              System.out.println("ResultSet updatable.");
	          }
	          result = stat.executeQuery(updatetable);
	          result.beforeFirst();  
	          while(result.next()){       	  
	        	  int weekwage =( result.getInt("weekwage"));
	        	  // System.out.println( " ," + weekwage);
	        	  result.updateInt("weekwage",weekwage);         	
	              result.updateRow();
	          }
	               
	        acc.printResult(result);
	        
	        System.out.println("-------------------------------------------------------------------");               
	        stat.close();
	       
	        }
	        catch(Exception e){
	        
	            e.printStackTrace();
	        }
			
		return average;
	}
	
	
	public int updatestability(String zipcode,int industrycode,int occupationcode){
		int count = 0;
		int numcount = 0;
		int stability = 0;
		Statement stat = null;
		ResultSet result = null;
		try{
			Connection conn = DatabaseConnection.getConnection();
            stat = conn.createStatement();
            stat=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                      ResultSet.CONCUR_UPDATABLE);
               
            String table = "SELECT  distinct userrecord.ssn,ziptable.zip  FROM  ziptable, userrecord,job "
				+ " WHERE ziptable.zip = '" + zipcode + "' and industrycode = " + industrycode + " and occupationcode = " + occupationcode + " and  ziptable.zip = userrecord.zip and job.ssn = userrecord.ssn";
            String cmd_getAllUnemployed = "SELECT distinct employmentStat.ssn,ziptable.zip, employmentStat.unemploymentreason "
				+ " FROM ziptable, employmentStat, userrecord ,job"
				+ " WHERE job.ssn = employmentstat.ssn " 
				+ "and employmentstat.unemploymentreason <> 'null' "
				+ "and ziptable.zip = '" + zipcode + "' "
				+ "and industrycode = " + industrycode + ""
				+ "and occupationcode = " + occupationcode + ""
				+ "and ziptable.zip = userrecord.zip";
           
			String industry = "select industrycode,industry,stability from industry where industrycode = " + industrycode +"";		
            String occupation = "select occupationcode ,occupation,stability from occupation where occupationcode = " + occupationcode + "";
            
            result = stat.executeQuery(table);
            while(result.next()){			
			     int ssn = result.getInt(1);						
			     //System.out.println(ssn + "," + zip  );			
		}
            result = stat.executeQuery(table);
            while(result.next())
            	numcount++;
                System.out.println("Total number of people working in zipcode " + zipcode + " is " + numcount );    
		
		
		    result = stat.executeQuery(cmd_getAllUnemployed);
		    while(result.next()){			
			      String reason = result.getString(3);
			      int ssn = result.getInt(1);
			      //System.out.println( ssn + "," + zip  + "," + reason );
			      //System.out.println("=============");
	   	}
		    result = stat.executeQuery(cmd_getAllUnemployed);
		    while(result.next())
			     count++;
		         System.out.println("Total number of unemployed  people in zipcode " + zipcode +  " is " + count);
		         System.out.println("=======================================");
		         int  sum = count + numcount;
		
		  if (result.getConcurrency() == ResultSet.CONCUR_UPDATABLE) {
            System.out.println("ResultSet non-updatable.");
          } else {
            System.out.println("ResultSet updatable.");
          }
			System.out.println("=======================================");
			
		   result = stat.executeQuery(industry);
		   result.beforeFirst();
		   while(result.next()){
			   if(sum == 0){
					  stability = 0;
				   }else{
				   
		             stability = (numcount*10/(sum));
				   }
	            
	             // int indu_code = result.getInt(1);
	             // String indus = result.getString(2);
	             //System.out.println(indu_code + "," + indus + "," + stability);
	             result.updateInt("stability",stability);   	
                 result.updateRow();
		}
		acc.printIndustry(result);
		System.out.println("=======================================");
		  result = stat.executeQuery(occupation);
		  result.beforeFirst();
		  while(result.next()){
			  if(sum == 0){
				  stability = 0;
			   }else{
			   
	             stability = (numcount*10/(sum));
			   }
	             //stability = (numcount*10/(count+numcount));
	           //int occupcode = result.getInt(1);
	           //String occup = result.getString(2);
	           // System.out.println(occupcode + "," +  occup +  "," + stability);
	           result.updateInt("stability",stability);   	
               result.updateRow();
		}
		acc.printOccupation(result);
		System.out.println("=======================================");
		
		stat.close();
       
		}catch(SQLException e){
			e.printStackTrace();
		
		}
		return stability;
	}
	 public int FindTopOccupationCode( String statename) {
		 Statement stat = null;
		 ResultSet result = null;
	     

	      try{
	    	  Connection conn = DatabaseConnection.getConnection();
	     	    stat = conn.createStatement();
	        result = stat.executeQuery(     
	             
	        "select occupationcode from job,userrecord,ziptable where userrecord.zip = ziptable.zip and userrecord.ssn = job.ssn and statename = '" + statename +"' group by occupationcode,employersize,statename");
	       if (!result.next())
	        {
	                //reportFailure("No rows in ResultSet");
	                //return 1;
	        }
	        
	        FindTop5Occupationcode(result);
	      } catch (SQLException e)
	        {
	           e.printStackTrace();
	      } 
	      return 5;
	   }

	 @TaDaMethod(variablesToTrack = {"code"},
			 correspondingDatabaseAttribute = {"job.occupationcode"})
	   private void FindTop5Occupationcode(ResultSet rs){
	      
	     int[] resultCount = new int[50];
	     int[] resultStates= new int[50];
	     int resultIndex = 0;
	     int stateIndex =0;
	     try{
	       do{	          
	    	  int code = rs.getInt(1);
	          stateIndex = acc.getOccupation(resultStates, code);
	          if( stateIndex < 50){	            
	            resultCount[stateIndex]++;	            
	          }
	          else{	            
	            resultCount[resultIndex]++;
	            resultStates[resultIndex] = code;
	            resultIndex++;
	          }         
	          
	       }while(rs.next());
	       FindBestoccupationcode(5, resultStates, resultCount, resultIndex, " Number of workers: ");
	       
	      } catch (SQLException e)
	        {
		   e.printStackTrace();     
	      }     
	      
	    }

	   public void FindBestoccupationcode(int numResults, int[] resultStates, int[] resultValues, int resultIndex, String text){

	       int maxValue =0;
	       int maxIndex =0;
	       int j=0;
	       for(int i=0; i<numResults; i++){
	          for(j=0; j<resultIndex; j++){
	             if(maxValue < resultValues[j]){
	                maxValue = resultValues[j];
	                maxIndex=j;
	           }
	       }

	          if(maxValue != 0){
	            System.out.println("OccupationCode: " + resultStates[maxIndex] + text + maxValue);
	            System.out.println("");
	            maxValue = 0;
	            resultValues[maxIndex] = 0;
	          }
	       }
	      
	    }
	    public int FindTopIndustryCode(String statename) {
	    	Statement stat = null;
			 ResultSet result = null;
		      try{
		    	  Connection conn = DatabaseConnection.getConnection();
		     	  stat = conn.createStatement();
		          result = stat.executeQuery(     
		               "select industrycode from job,userrecord,ziptable where userrecord.zip = ziptable.zip and userrecord.ssn = job.ssn and statename = '" + statename +"' group by industrycode,employersize,statename");
		       if (!result.next())
		        {
		                //reportFailure("No rows in ResultSet");
		                //return;
		        }
		        
		        FindTop5Industrycode(result);
		      } catch (SQLException e)
		        {
		           e.printStackTrace();
		      } 
		      return 5;
		   }

	    @TaDaMethod(variablesToTrack = {"code"},
				 correspondingDatabaseAttribute = {"job.industrycode"})
		   private void FindTop5Industrycode(ResultSet rs){
		      
		     int[] resultCount = new int[50];
		     int[] resultStates= new int[50];
		     int resultIndex = 0;
		     int stateIndex =0;
		     try{
		       do{		          
		    	  int code = rs.getInt(1); 
		          stateIndex = acc.getIndustryCode(resultStates, code);
		          if( stateIndex < 50){	            
		            resultCount[stateIndex]++;		            
		          }
		          else{		            
		            resultCount[resultIndex]++;
		            resultStates[resultIndex] = code;
		            resultIndex++;
		          }         
		          
		       }while(rs.next());
		       FindBestIndustrycode(5, resultStates, resultCount, resultIndex, " Number of workers: ");
		       
		      } catch (SQLException e)
		        {
			   e.printStackTrace();     
		      }     
		      
		    }
    private void FindBestIndustrycode(int numResults, int[] resultStates, int[] resultValues, int resultIndex, String text){

		       int maxValue =0;
		       int maxIndex =0;
		       int j=0;
		        for(int i=0; i<numResults; i++){
		          for(j=0; j<resultIndex; j++){
		             if(maxValue < resultValues[j]){
		                maxValue = resultValues[j];
		                maxIndex=j;
		          }
		       }

		          if(maxValue != 0){
		            System.out.println("IndustryCode: " + resultStates[maxIndex] + text + maxValue);
		            System.out.println("");
		            maxValue = 0;
		            resultValues[maxIndex] = 0;
		          }
		       }
		    }    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
}

