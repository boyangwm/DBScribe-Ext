package com.riskIt.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import tada.TaDaMethod;

import com.riskIt.data.*;
import com.riskIt.db.DatabaseConnection;
import com.riskIt.util.AccessorMethods;
import com.riskIt.util.RiskItConstants;


/**
 * This class has methods that is related to the agent queries
 * and help methods that helps create a new user, calculate the 
 * score and other helper methods
 * @author Poornima
 *
 */

public class AgentManager 
{
	
	private AccessorMethods acc;
	private ZipcodeManager zipcodeManager; 
	
	public AgentManager()
	{
		acc = new AccessorMethods();
		zipcodeManager = new ZipcodeManager();
	}
	
	public void setZipcodeManager(ZipcodeManager zm)
	{
		zipcodeManager = zm;
	}	
	/**
	 * This method accepts all information regarding a customer and 
	 * then writes it to the appropriate table in database.
	 * @param all parameters holds user data to be inserted into table
	 * @return success - return 1 if insert is success else 0, if data is already 
	 * 					 present in database
	 * @author Poornima Tantry
	 */
	public int createNewUser(String name, String zipcode, long ssn, int age, String sex,
							 String marital, String race, String taxStatus, String taxDetails,
							 String houseHoldDetails, String fatherOrigin, String motherOrigin,
							 String birthCountry, String citizenship, int capitalGains, 
							 int capitalLosses, int stockDividend, String parents, 
							 String unEmploymentReason, String employmentStatus,
							 String education,String eduEnroll,String workClass,
							 String unionMember, int industryCode, int occupationCode,
							 int employerSize, int weekWage, int selfEmployed, int workWeeks)
	{	
		int success = 0, count =0;
		Statement stat = null;
		ResultSet result = null;
		
			try
			{
				Connection conn = DatabaseConnection.getConnection();
				stat = conn.createStatement();
					
				System.out.println("Customer details you entered are .....");
				System.out.println(" Name                       : " + name);
				System.out.println(" SSN                        : " + ssn);
				System.out.println(" Age                        : " + age);
				System.out.println(" Sex                        : " + sex);
				System.out.println(" Marital Status             : " + marital);
				System.out.println(" Race                       : " + race);
				System.out.println(" Tax Status                 : " + taxStatus);
				System.out.println(" Tax Details                : " + taxDetails);
				System.out.println(" House hold information     : " + houseHoldDetails);
				System.out.println(" Father's origin            : " + fatherOrigin);
				System.out.println(" Mother's origin            : " + motherOrigin);
				System.out.println(" Birth Country              : " + birthCountry);
				System.out.println(" CitizenShip                : " + citizenship);
				System.out.println(" Capital Gains              : " + capitalGains);
				System.out.println(" Capital losses             : " + capitalLosses);
				System.out.println(" Stock dividends            : " + stockDividend);
				System.out.println(" Parents'Informarion        : " + parents);
				System.out.println(" Employment Status          : " + employmentStatus);
				System.out.println(" Un-employment reason       : " + unEmploymentReason);
				System.out.println(" Education                  : " + education);
				System.out.println(" Further education enrolled : " + eduEnroll);
				System.out.println(" Work Class                 : " + workClass);
				System.out.println(" Union member               : " + unionMember);
				System.out.println(" Industry code              : " + industryCode);
				System.out.println(" Occupation code            : " + occupationCode);
				System.out.println(" Employer Size              : " + employerSize);
				System.out.println(" Self-Employed              : " + selfEmployed);
				System.out.println(" Week wage                  : " + weekWage);
				System.out.println(" No. of weeks of work       : " + workWeeks);
				
				
				//Search if the data is already there in table
				String cmd_dataCheck = "Select * from userrecord where ssn = "
										+ ssn;
				
				//get connection to the table and insert all the data	
				String cmd_userRecord = "INSERT INTO userrecord VALUES ('" + name + "', '" 
										+ zipcode + "', " + ssn + ", "+ age +", '"+ sex +"', '" 
										+marital +"', '" + race +"', '" + taxStatus + "', '" 
										+ taxDetails +"', '" + houseHoldDetails +"', '" + fatherOrigin
										+ "', '" + motherOrigin + "', '" + birthCountry + "', '" 
										+ citizenship + "')";
				
				String cmd_investment = "INSERT INTO investment VALUES (" + ssn +", " 
										+ capitalGains +", " + capitalLosses +", " 
										+ stockDividend	+ ")";
				String cmd_youth = "INSERT INTO youth VALUES (" + ssn + ", '" 
									+ parents +"')";
				String cmd_employment = "INSERT INTO employmentstat VALUES (" + ssn + ", '"
										+ unEmploymentReason + "', '" + employmentStatus + "')";
				String cmd_education = "INSERT INTO education VALUES (" + ssn + ", '" 
										+ education + "', '" + eduEnroll + "')";
				String cmd_job = "INSERT INTO job VALUES (" + ssn +", '" + workClass + "', "
								+ industryCode + ", " + occupationCode + ", '" + unionMember 
								+ "', " + employerSize + ", " + weekWage + ", " + selfEmployed 
								+ ", " + workWeeks + ")";
				
				//first check before inserting the data into table				
				result = stat.executeQuery(cmd_dataCheck);
				
				while(result.next())
				{
					++count;
				}
				System.out.println("---------------------------------------------");
				//if no record is present, then isert the data
				if(count == 0)
				{		
					stat.execute(cmd_userRecord);
					stat.execute(cmd_investment);
					stat.execute(cmd_youth);
					stat.execute(cmd_employment);
					stat.execute(cmd_education);	
					stat.execute(cmd_job);
					success = 1;
					System.out.println("This customer is successfully added to database");
				}
				else
					System.out.println("This customer is already present in the database");
			
				result.close();
				stat.close();
				
				System.out.println("---------------------------------------------");
		}
		catch(Exception e)
		{
			System.out.println("Catch in createUser");
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * This method accepts customer's Social security number and 
	 * calculates the score
	 * @param userSSN - ssn of the customer
	 * @return score = score of the customer
	 * @author Poornima Tantry
	 */
	@TaDaMethod(variablesToTrack ={"occupationValue", "workweeks", "gains", "loss", "stocks"
			,"zipcode", "gender", "unionMember"
			,"edu", "noOfEmp", "selfEmp", "marital_status", "age"}, 
			correspondingDatabaseAttribute = {"occupation.occupation", "job.workweeks", 
			"investment.capitalgains", "investment.capitallosses", "investment.stockdividends",
			"userrecord.zip", "userrecord.sex", "job.unionmember",
			"education.education", "job.employersize", "job.selfemployed", "userrecord.marital", "userrecord.age"})
	public double calculateScore(long userSSN)
	{	
		int indus_stab = 0, occu_stab =0, gains = 0, workweeks = 0;
		int noOfEmp = 0, selfEmp = 0, age = 0, weekwage = 0, loss = 0, stocks = 0;
		double income = 0, score = 0;
		String zipcode = "", gender = "", unionMember = "", edu = "";
		String occupationValue = "", marital_status = "";
		ArrayList<ScoreData> sData = new ArrayList<ScoreData>();
		Statement stat = null;
		ResultSet result = null;
		
		String cmd_selectSSN = "SELECT userrecord.zip, userrecord.sex, userrecord.ssn, " 
								+ " userrecord.age, userrecord.marital, investment.capitalgains, "
								+ " investment.capitallosses, investment.stockdividends, " 
								+ " job.workweeks, job.weekwage, job.unionmember, job.employersize, "
								+ " job.selfemployed, industry.stability, industry.industrycode, "
								+ " occupation.stability, occupation.occupationcode, occupation.occupation, "
								+ " education.education  "			
								+ " FROM  industry, occupation, investment, education,"
								+ " userrecord, job WHERE industry.industrycode = job.industrycode "
								+ " and occupation.occupationcode = job.occupationcode "
								+ " and education.ssn = userrecord.ssn and "
								+ " investment.ssn = userrecord.ssn and "
								+ " job.ssn = userrecord.ssn and "
								+ " userrecord.ssn = " + userSSN ;		
		
		try
		{
			System.out.println("flow:1");
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			
			result = stat.executeQuery(cmd_selectSSN);
			
			//calculate the income for given person	
			boolean flag = result.next();
			
			if(flag)
			{ 
				System.out.println("flow:2");
				occupationValue = result.getString("occupation");
				
				if(occupationValue.equalsIgnoreCase("Federal government")){
					System.out.println("flow:3");
					score = 100;
				}
				else
				{
					System.out.println("flow:4");
					workweeks = result.getInt("workweeks");
					weekwage = result.getInt("weekwage");
					gains = result.getInt("capitalGains");
					loss = result.getInt("capitallosses");
					stocks = result.getInt("stockdividends");
					
					//calculate income
					income = acc.calculateIncome(workweeks, weekwage, gains, loss, stocks);
												
					//get all data to local variables
					zipcode = result.getString("zip");
					gender = result.getString("sex");
					unionMember = result.getString("unionmember");
					indus_stab = result.getInt(14);
					occu_stab = result.getInt(16);
					edu = result.getString("education");
					gains = result.getInt("capitalGains");					
					noOfEmp = result.getInt("employersize");
					selfEmp = result.getInt("selfemployed");
					marital_status = result.getString("marital");
					age = result.getInt("age");
					
					//get all zipcode and calculate range
					sData = zipcodeManager.getAllZipcodes(zipcode);
					
					//calculate the percentile
					int percentile = acc.calculatePercentile(income, sData);											
					score = 100 * percentile;
										
					//check if person is female
					if(gender.trim().equals("Female"))
					{
						System.out.println("flow:5");
						score = score + (score/100 * 5);
					}
					
					//calculate score based on marital status
					score = acc.checkScoreRange(unionMember, indus_stab, occu_stab, edu, score);
					
					//if major income from capital gains reduce score by 20%
					double majorIncome = income * 50/100;					
					if(majorIncome >= gains){
						System.out.println("flow:6");
						score = score - (score * RiskItConstants.INCOME_GAIN_PERCENTAGE_REDUCTION/100);			
					}
					//calculate score based on age, employee status
					score = acc.checkScoreForStatus(marital_status, age, selfEmp, noOfEmp, score);
					if(score > 100.0){
						System.out.println("flow:7");
						score = 100;
					}
				}	
			} 
			else{
				System.out.println("flow:8");
				System.out.println("There is no record for this customer");
			}
			result.close();
		}
		catch(Exception e)
		{
			System.out.println("flow:9");
			System.out.println("Exception in calculateScore");
			e.printStackTrace();
		}
		
		return score;
	}
	
	/**
	 * Method to delete the test record, and then re-insert into table
	 * @param ssn - SSN of person
	 * @return - 1 for success or 0 for fail 
	 * @author Poornima Tantry
	 */
	public int deleteUser(String ssn)
	{
		int count = 0, success = 0;
		Statement stat = null;
		ResultSet result = null;
		
		try
		{
			String del_user = "Delete from userrecord where ssn = " + Long.parseLong(ssn);
			String del_job = "Delete from job where ssn = " + Long.parseLong(ssn);
			String del_edu = "Delete from education where ssn = " + Long.parseLong(ssn);
			String del_emp = "Delete from employmentstat where ssn = " + Long.parseLong(ssn);
			String del_inv = "Delete from investment where ssn = " + Long.parseLong(ssn);
			String del_mig = "Delete from migration where ssn = " + Long.parseLong(ssn);
			String del_youth = "Delete from youth where ssn = " + Long.parseLong(ssn);
			
			String cmd_checkData = "Select * from userrecord where ssn = " + Long.parseLong(ssn);
			
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			
			result = stat.executeQuery(cmd_checkData);
			while(result.next())
				++count;
			//check if records are there and then delete it
			if(count > 0)
			{
				stat.execute(del_youth);
				stat.execute(del_mig);
				stat.execute(del_job);
				stat.execute(del_edu);
				stat.execute(del_emp);
				stat.execute(del_inv);	
				stat.execute(del_user);
				success = 1;
			}
			
			//re do the checking to see if the data is deleted 
			result = stat.executeQuery(cmd_checkData);
			while(result.next())
				++count;
			if(count > 0)
			{
				stat.execute(del_youth);
				stat.execute(del_mig);
				stat.execute(del_job);
				stat.execute(del_edu);
				stat.execute(del_emp);
				stat.execute(del_inv);	
				stat.execute(del_user);
				success = 1;
			}
			else
				System.out.println("The data is already deleted");
			
			result.close();
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in delete");
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * This method creates a agent table with their names and ids.
	 * @author Poornima Tantry
	 */
	public void createAgentTable()
	{
		Statement stat;
		
		try
		{
			String create_agent = "Create table AgentTable ( Name char(20), id char(5))" ;
			String insert_agent1 = "Insert into AgentTable values ( 'Jim Cohen', '12345')";
			String insert_agent2 = "Insert into AgentTable values ( 'Lisa Coburn', '23456')";
			String insert_agent3 = "Insert into AgentTable values ( 'Big John', '34567')";
			String insert_agent4 = "Insert into AgentTable values ( 'Chris Flake', '98765')";
			String insert_agent5 = "Insert into AgentTable values ( 'Mike Jackman', '85642')";
			String del_Table = " Drop table AgentTable";
			
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			
			try
			{
				//if the database already contains the table
				//first delete it and then create it again
				stat.execute(del_Table);
			}
			catch(Exception e)
			{
				//e.printStackTrace();
			}
			//create agenttable
			stat.execute(create_agent);
			stat.execute(insert_agent1);
			stat.execute(insert_agent2);
			stat.execute(insert_agent3);
			stat.execute(insert_agent4);
			stat.execute(insert_agent5);
		
		}
		catch(Exception e)
		{
			System.out.println("Exception in createAgentTable");
			e.printStackTrace();
			
		}
	}
	
	
	/**
	 * This method calculates the pure premium value using the total property 
	 * value and the exposure unit that insurance covers for one full year
	 * @param inv - it is a object holding all data of customer needed to 
	 * calculate the premium
	 * @return premium - the pure premium value calculated for the given criteria
	 * @author Poornima Tantry 
	 */
	public double calculatePurePremium(Invitation inv)
	{
		double premium = 0;
		
		//calculate pure premium using the loss coverage for given property
		if(inv.getlossCoverage() > 0 && inv.getExpoUnit() > 0)
			premium = inv.getlossCoverage()/inv.getExpoUnit();
		
		return premium;
	}
	
	/** 
	 * THis method calculates the expense for every exposure unit. This value 
	 * is given by the company for the amount of possible loss coverage 
	 * @param inv - object holding the information of customer
	 * @return expo - expense for every unit of exposure of property
	 * @author Poornima Tantry
	 */
	
	public double calculateExpenseForExpo(Invitation inv)
	{
		double expo = 0;
		
		//calculate expense per every exposure unit using property value
		if(inv.getPropertyValue() > 0 && inv.getExpoUnit() > 0)
			expo = inv.getPropertyValue()/inv.getExpoUnit();
		
		return expo;
	}
	
	/**
	 * This methods calculates actual premium considering 
	 * the profit and contingency factor, the expense of exposure unit
	 * and pure premium values
	 * @param inv - Object holding all of customer's information
	 * @return tempPremium - the actual premium value given to customer
	 * @author Poornima Tantry
	 */
	public double calculateActualPremium(Invitation inv, double premium, double expo)
	{
		double tempPremium = 0;
		
		//calculate the actual premium
		tempPremium = (premium + expo)/(1 - RiskItConstants.PROFIT_CONTINGENCY_FACTOR/100);
		
		//get the state name
		inv = zipcodeManager.getOneZipcode(inv);
		
		//balance the premium considering the age and deductible factor
		tempPremium = compareCustomerZones(inv, tempPremium);
		
		return tempPremium;
	}
	
	
	/**
	 * This method accepts the customer object and premium value 
	 * calculated till half point and then compares the customer's zipcode
	 * to see where he stays, his age and his score and then calculates
	 * the final premium
	 * @param inv - customer object
	 * @param premium - the temporary premium value
	 * @return premium value for each month
	 * @author Poornima Tantry
	 */
	public double compareCustomerZones(Invitation inv, double premium)
	{
		double tempPremium = premium ;
		
		String state = inv.getStateName();
		
		//check if customer is in the risk zone
		if(state.equals("CA") || state.equals("AZ") || state.equals("NM") ||
		   state.equals("TX") || state.equals("LA") || state.equals("OK") ||
		   state.equals("MS") || state.equals("AL") || state.equals("FL") ||
		   state.equals("GA") || state.equals("SC") || state.equals("NC") ||
		   state.equals("VA") || state.equals("NJ") || state.equals("DE") ||
		   state.equals("RI") || state.equals("CT") || state.equals("MA") )
		{
			//if customer is in the risk zone and is about 60 years
			// then premium goes up by 30% else it goes up by 20%
			if(inv.getAge() >= RiskItConstants.SIXTY_AGE)
				tempPremium = tempPremium + tempPremium * RiskItConstants.THIRTY_PERCENT/100;
			else
				tempPremium = tempPremium + tempPremium * RiskItConstants.TWENTY_PERCENT/100;
		}
		
		//calculate  total premium per year
		double totalPremium = tempPremium * 12;
		
		//compare the deductible based on  property values
		if(inv.getDeductible() < inv.getPropertyValue() * RiskItConstants.TEN_PERCENT/100)
			totalPremium = totalPremium + totalPremium * RiskItConstants.TEN_PERCENT/100;
		else if(inv.getDeductible() < inv.getPropertyValue() * RiskItConstants.TWENTY_PERCENT/100)
			totalPremium = totalPremium + totalPremium * RiskItConstants.TWENTY_PERCENT/100;
		else if(inv.getDeductible() < inv.getPropertyValue() * RiskItConstants.THIRTY_PERCENT/100)
			totalPremium = totalPremium + totalPremium * RiskItConstants.THIRTY_PERCENT/100;
		
		//compare based on score too
		if(inv.getScore() <= 80.0)
			totalPremium = totalPremium + totalPremium * RiskItConstants.TEN_PERCENT/100;
		else if(inv.getScore() <= 50.0)
			totalPremium = totalPremium + totalPremium * RiskItConstants.TWENTY_PERCENT/100;
		else if(inv.getScore() <= 10.0)
			totalPremium = totalPremium + totalPremium * RiskItConstants.THIRTY_PERCENT/100;
		
		return totalPremium/12;
	}
	
	
	/**
	 * This method puts all methods to calculate the premium together as 
	 * one call
	 * @param inv - customer data object
	 * @return - the monthly premium
	 */
	public double calculateTotalValue(Invitation inv)
	{
		System.out.println("flow:14");
		//get first pure premium
		double purePremium = calculatePurePremium(inv);
		
		//calculate expense per every exposure unit
		double expense_expo = calculateExpenseForExpo(inv);
		inv.setExpenseExpo(expense_expo);
		
		//now calculate actual premium
		double premium = calculateActualPremium(inv, purePremium, expense_expo);
		inv.setPremium(premium);
		
		return premium;
	}
	

}
