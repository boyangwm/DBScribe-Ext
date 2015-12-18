package com.riskIt.ui;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import com.riskIt.controller.UserManager;
import com.riskIt.db.DatabaseConnection;
import com.riskIt.controller.AgentManager;
import com.riskIt.data.Invitation;
import com.riskIt.util.*;


/**
 * This class provides all UI choices for agent and customer
 * and then calls appropriate method to continue calculations
 * @author Poornima Tantry
 *
 */
public class SubMenuChoices 
{
	private AgentManager agent;
	private UserManager user;
	private Scanner in;
	private AccessorMethods acc;
	private String agentName;
	private int agentId;
	private static boolean agentFlag = false;
	private ArrayList<Invitation> userInviteList;
	
	//Constructor
	public SubMenuChoices()
	{
		agent = new AgentManager();
		user = new UserManager();
		acc = new AccessorMethods();
		in = new Scanner(System.in);	
		userInviteList = new ArrayList<Invitation>();
	}
	
	/**
	 * This is the UI, so that agent can see number of people
	 * or customers and getting a count, by providing
	 * either zipcode or education, occupation, marital status
	 * or estimated income
	 */
	public void browseUserByChoice()
	{
		
		int choice = 0;
		String getChoice = "";
		
		do
		{
			System.out.println("1. Filter using ZIPCODE");
			System.out.println("2. Filter using EDUCATION");
			System.out.println("3. Filter using OCCUPATION");
			System.out.println("4. Filter using MARITAL_STATUS");
			System.out.println("5. Filter using ESTIMATED_INCOME");
			System.out.println("6. Exit");
			
			System.out.print("Enter your choice : ");
			getChoice = in.nextLine();	
			
			//check for invalid input
			choice = acc.checkLetterDigit(getChoice, RiskItConstants.SUB_INVALID);
			
			switch(choice)
			{
			case 1:
				//get customer names filtering through zipcode
					System.out.print("Enter zipcode : ");
					String zip = in.nextLine();
					System.out.println("------------------------------------------");
					user.filterZipcode(zip);
					System.out.println("------------------------------------------");
					break;
			case 2:
				//get customer names filtering through education 
					System.out.print("Enter education level : ");
					String edu = in.nextLine();
					System.out.println("------------------------------------------");
					user.filterEducation(edu);
					System.out.println("------------------------------------------");
					break;
			case 3:
				//get customer names filtering through Occupation
					System.out.print("Enter Occupation : ");
					String occupation = in.nextLine();
					System.out.println("------------------------------------------");
					user.filterOccupation(occupation);
					System.out.println("------------------------------------------");
					break;
			case 4:
				//get customer names filtering through Marital Status
					System.out.print("Enter Marital Status : ");
					String status = in.nextLine();
					System.out.println("------------------------------------------");
					user.filterMaritalStatus(status);
					System.out.println("------------------------------------------");
					break;
			case 5:
				//get customer names filtering through estimated income
					System.out.print("Enter Estimated income : ");
					String getIncome = in.nextLine();
					System.out.println("------------------------------------------");
					user.filterEstimatedIncome(getIncome);
					System.out.println("------------------------------------------");
					break;
			default:
					System.out.println("You chose invalid option. Please try again...");
				break;
					
					 
			
			}
		}while(choice > 0 && choice < 6);
	}
	
	/**
	 * This method accepts all user inputs for creating a new user and then 
	 * passes it be inserted into table
	 * @author Poornima Tantry
	 */
	public void inputForCreateUser()
	{
		String getInput, name, fName, mName, lName, zipcode, ssn, sex, marital;
		String race, taxStatus, taxDetails, houseHoldDetails, fatherOrigin, motherOrigin;
		String birthCountry, citizenship;
		String parents, unEmploymentReason, employmentStatus, education, eduEnroll;
		String workClass, unionMember;			
		int age, capitalGains, capitalLosses, stockDividend, industryCode;
		int employerSize, occupationCode, weekWage, workWeeks, selfEmployed = 0;
		Statement stat;
		ResultSet result;
		
		//first name cannot be a null value
		do{
			System.out.print("Customer's First-Name : ");
			fName = in.nextLine();
		}while(fName.length() <= 0);
		
		//middle name
		System.out.print("Customer's middle-Name : ");
		mName = in.nextLine();
		
		//last name
		do
		{
			System.out.print("Customer's Last-Name : ");
			lName = in.nextLine();
		}while(lName.length() <= 0);
		
		//form the complete name here
		if(mName.length() == 0)
			name = fName + " ," + lName;
		else
			name = fName + " ," + mName + " ," + lName;
		
		//customer's SSN
		do{
		System.out.print("Customer's SSN : ");
		ssn = in.nextLine();
		int tempssn = acc.checkLetterDigit(ssn, RiskItConstants.INVALID_VALUE);
		ssn = Integer.toString(tempssn);
		}while(ssn.length() <= 0 || ssn.equalsIgnoreCase("0"));
		
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			
			//check to see if customer already in database
			String cmd_search = "SELECT * from userrecord where ssn = " + ssn;
			result = stat.executeQuery(cmd_search);		
			
			//check to see if the customer already exists in our database
			boolean checkCustomerFlag = false;
			while(result.next())
			{	
				checkCustomerFlag = true;
			}
			
			
			if(checkCustomerFlag)
			{
				System.out.println("--------------------------------------------");
				System.out.println("This customer is already in our database");
				System.out.println("--------------------------------------------");
			}
			else
			{
				//get all data and insert into database
				do{
					System.out.print("Customer's age : ");
					getInput = in.nextLine();
				}while(getInput.length() <=0);
				age = Integer.parseInt(getInput);
			
			
				do{
					System.out.print("Customer's sex : ");
					sex = in.nextLine();
				}while(sex.length() <= 0);
			
				do{
					System.out.print("Customer's race : ");
					race = in.nextLine();
				}while(race.length() <= 0);
				
				do{
					System.out.print("Customer's marital-Status : ");
					marital = in.nextLine();
				}while(marital.length() <= 0);
			
				do{
					System.out.print("Customer's citizenship-status : ");
					citizenship = in.nextLine();
				}while(citizenship.length() <= 0);
			
				do{
				System.out.print("Customer's tax-filing-status : ");
				taxStatus = in.nextLine();
				}while(taxStatus.length() <= 0);
			
				do{
					System.out.print("Customer's details for given tax-filing-status : ");
					taxDetails = in.nextLine();
				}while(taxDetails.length() <= 0);
			
				do{
					System.out.print("Customer's house-hold-details : ");
					houseHoldDetails = in.nextLine();
				}while(houseHoldDetails.length() <= 0);
			
				do{
					System.out.print("Customer's father's origin : ");
					fatherOrigin = in.nextLine();
				}while(fatherOrigin.length() <= 0);
			
				do{
					System.out.print("Customer's mother's origin : ");
					motherOrigin = in.nextLine();
				}while(motherOrigin.length() <= 0);
			
				do{
					System.out.print("Customer's birth-country : ");
					birthCountry = in.nextLine();
				}while(birthCountry.length() <= 0);
			
				System.out.print("Customer's zipcode : ");
				zipcode = in.nextLine();
				if(zipcode.length() <= 0)
					zipcode = null;
			
				System.out.print("Does customer have any capital-gains(No COMMA) : ");
					getInput = in.nextLine();	
				capitalGains = acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE);
							
				System.out.print("Did customer have capital-losses(No COMMA) : ");
				getInput = in.nextLine();	
				capitalLosses = acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE);
			
				System.out.print("Does customer have stock-dividends(No COMMA) : ");
				getInput = in.nextLine();		
				stockDividend = acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE);
					
				do{
					System.out.print("Customer's parents information : ");
					parents = in.nextLine();
				}while(parents.length() <= 0);
				
				do{
					System.out.print("Customer's employment-Status : ");
					employmentStatus = in.nextLine();
					}while(employmentStatus.length() <= 0);
				
					System.out.print("Customer's un-employment reason : ");
					unEmploymentReason = in.nextLine();
					if(unEmploymentReason.length() <= 0)
						unEmploymentReason = null;
					
					do{
						System.out.print("Customer's education : ");
						education = in.nextLine();
					}while(education.length() <= 0);
			
					System.out.print("Is customer currently enrolled for further education : ");
					eduEnroll = in.nextLine();
					if(eduEnroll.length() <= 0)
						eduEnroll = null;
					
					do{
						System.out.print("Customer's working-class [public/Private]: ");
						workClass = in.nextLine();
					}while(workClass.length() <= 0);
				
					do{
						System.out.print("Customer's industry-code : ");
						getInput = in.nextLine();
					}while(getInput.length() <= 0);		
					industryCode = acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE);
					
					do{
						System.out.print("Customer's occupation-code : ");
						getInput = in.nextLine();
					}while(getInput.length() <= 0);		
					occupationCode = acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE);
				
					System.out.print("Is customer a union-member(Y/N) : ");
					unionMember = in.nextLine();
					
					//validate input
					if(unionMember.length() <= 0)
						unionMember = null;
					if(unionMember.equalsIgnoreCase("Y")||unionMember.equalsIgnoreCase("yes"))
						unionMember = "Yes";
					else 
						unionMember = "No" ;
				
					System.out.print("Size of customer's employer(No Comma) : ");
					getInput = in.nextLine();	
					employerSize = acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE);
					
					System.out.print("Is customer self-employed(Y/N) : ");
					getInput = in.nextLine();
					
					//validate input
					if(getInput.length() <= 0)
						selfEmployed = 0;
					if(getInput.equalsIgnoreCase("Y")||getInput.equalsIgnoreCase("yes"))
						selfEmployed = 1;
					else 
						selfEmployed = 0 ;
					
					
					System.out.print("Customer's week-wage (No Comma): ");
					getInput = in.nextLine();		
					weekWage = acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE);
			
					System.out.print("Customer's working hours in WEEKS (No Comma) : ");
					getInput = in.nextLine();	
					workWeeks = acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE);
					
					agent.createNewUser(name, zipcode, Long.parseLong(ssn), age, sex, marital, race, taxStatus,
										taxDetails, houseHoldDetails, fatherOrigin, motherOrigin, 
										birthCountry, citizenship, capitalGains, capitalLosses, 
										stockDividend, parents, unEmploymentReason, employmentStatus,
										education, eduEnroll, workClass, unionMember, industryCode, 
										occupationCode, employerSize, weekWage, selfEmployed, workWeeks);
			
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in inputForCreateUser");
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * This method is the input means for accessing user with different 
	 * combinations of data and then calls the method to print results
	 * @author Poornima Tantry
	 */
	public void inputForbrowseUserProperties()
	{
		boolean checkInputFlag  = false;
		ArrayList<String> prop = new ArrayList<String>();
		String getInput = "";
		
		
		System.out.println("--------------------------------------------------------------");
		System.out.println("PLEASE HIT ENTER KEY IF YOU DO NOT WISH TO ENTER VALUES");
		
		System.out.print("Do you wish to enter name of customer : ");
		getInput = in.nextLine();
		prop.add(getInput) ;
		
		
		System.out.print("Do you wish to enter SSN of customer : ");
		getInput = in.nextLine();
		
		if(getInput.length() <= 0)
			getInput = "";
		else 
		{
			getInput = Integer.toString(acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE));
			if(getInput.equals("0"))
				checkInputFlag = true;
		}
		
		//if everything is fine, then add input
		if(!checkInputFlag)
			prop.add(getInput) ;
	
		
		if(!checkInputFlag)
		{
			System.out.print("Do you wish to enter age of customer : ");
			getInput = in.nextLine();
			
			if(getInput.length() <= 0)
				getInput = "";
			else
			{
				getInput = Integer.toString(acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE));
				if(getInput.equals("0"))
					checkInputFlag = true;
			}
		}
		
		//if everything is fine, then add input
		if(!checkInputFlag)
			prop.add(getInput) ;
		
		if(!checkInputFlag)
		{
			System.out.print("Do you wish to enter sex of customer : ");
			getInput = in.nextLine();
			prop.add(getInput) ;
		
		
			System.out.print("Do you wish to enter marital_status of customer : ");
			getInput = in.nextLine();
			prop.add(getInput) ;
		
			System.out.print("Do you wish to enter race of customer : ");
			getInput = in.nextLine();
			prop.add(getInput) ;
		
			System.out.print("Do you wish to enter country of customer : ");
			getInput = in.nextLine();
			prop.add(getInput) ;
				
			user.browseUserProperties(prop);
		}
		else
			System.out.println("You have entered invalid input..");
		
		
	}
	
	/**
	 * The method provided a choice for either agent to get values or user to get
	 * their values
	 * @author Poornima Tantry
	 */
	public void AgentAndUser()
	{
		int choice = 0;
		String getChoice = "";
		do
		{
			System.out.print( "ENTER \n 1. Agent \n 2. User \n 3. Exit \n Please enter your choice : ");
			getChoice = in.nextLine();
			
			choice = acc.checkLetterDigit(getChoice, RiskItConstants.AGENT_INVALID);
			
			switch(choice)
			{
			case 1:
					//if agent has already given his id
					//then directly go to agent menu else
					//first accept the id from agent
					if(agentFlag)
						agentMenu();
					else
						agentFlag  = AgentLogin();
					break;
			case 2:
					//UI for customer to get a quote for themselves
					inputForUserQuotes();
					break;
			default:
					break;
			}
			
		}while(choice > 0 && choice < 3);
		
		
	}
	
	
	/**
	 * This method first creates a agent table and then helps agent 
	 * login to system
	 * @author Poornima Tantry
	 */
	
	public boolean AgentLogin()
	{
		Statement stat;
		ResultSet result;
		boolean checkFlag = false;
		
		agent.createAgentTable();
		
		//read data from user
		System.out.print("Enter your name : ");
		agentName = in.nextLine();
		
		System.out.print("Enter your id : ");
		String num = in.nextLine();
		
		if(num.length() > 0)
			agentId = Integer.parseInt(num);
		else
			agentId = 0;
		
		//compare the accepted input, to see if the agent is in
		//agent table, else try again
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			stat = conn.createStatement();
			
			String cmd_search = "Select * from AgentTable where name = '" + agentName + "'"
								+ " and id = '" + agentId + "'";
			result = stat.executeQuery(cmd_search);
			
			if(result.next())
			{
				checkFlag = true;
				agentMenu();
			}
			else
				System.out.println("You have not identified yourself. Try again...");
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in chooseAgentActivity");
			e.printStackTrace();
		}
		
		return checkFlag;
		
	}
	/**
	 * This is menu option for the agent to send out invitation
	 * to customer
	 * @author Poornima Tantry
	 */
	public void agentMenu()
	{
		String getChoice = "";
		int choice = 0;
		
		do
		{
			System.out.print( "ENTER \n 1. Send Invitation  \n 2. Exit \n Please enter your choice : ");
			getChoice = in.nextLine();
			
			choice = acc.checkLetterDigit(getChoice, 2);
			
			switch(choice)
			{
			case 1:
					//create UI for accepting input to send 
					//invitation to customer 
					sendInvitation();
					break;
			default:
					break;
			}
			
		}while(choice > 0 && choice < 2);
		
	}
	
	/**
	 * This method accepts all information for a user and stores it in a
	 * object and calculates the premium amount and helps agent
	 * send out invitation.
	 * @author Poornima Tantry
	 */
	
	public void sendInvitation()
	{
		long ssn = 0;
		double expo_value = 0;
		String getInput = "";
		String choice = "N"; 
		
		
		do
		{
			//create a new invitation object for each customer
			Invitation invite = new Invitation();
			
			//enter all relevant values
			System.out.print("Enter the customer's SSN : ");
			getInput = in.nextLine();
			
			//validate input
			getInput = Integer.toString(acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE));
		
			if(getInput.length() <= 0 || getInput.equals("0"))
			{
				System.out.println("You have entered a invalid SSN");
			}
			else
			{
				ssn = Long.parseLong(getInput);
				invite.setUserSSN(Long.parseLong(getInput));
			
				System.out.print("Enter total expense value (NO COMMA): $");
				getInput = in.nextLine();
				getInput = Integer.toString(acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE));
				
				if(getInput.length() > 0)
					invite.setPropertyValue(Double.parseDouble(getInput));
			
			
				System.out.print("Enter coverage amount that insurance company provides for full year(Exposure Unit) (NO COMMA): $");
				getInput = in.nextLine();
				getInput = Integer.toString(acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE));
			
				if(getInput.length() > 0)
					expo_value = Double.parseDouble(getInput);
					invite.setExpoUnit(expo_value);
				
				System.out.print("Enter loss amount(if needed in future) (NO COMMA): $");
				getInput = in.nextLine();
				getInput = Integer.toString(acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE));
			
				if(getInput.length() > 0)
					invite.setlossCoverage(Double.parseDouble(getInput));
				
				System.out.print("Enter the deductible amount (NO COMMA): $");
				getInput = in.nextLine();
				getInput = Integer.toString(acc.checkLetterDigit(getInput, RiskItConstants.INVALID_VALUE));
			
				if(getInput.length() > 0)
					invite.setDeductible(Double.parseDouble(getInput));
				
				invite.setPremium(0);
				invite.setAgentID(agentId);
				
				double tempScore = agent.calculateScore(ssn);
				invite.setScore(tempScore);
				
				userInviteList.add(invite);
				System.out.print("Do you wish to add another customer(Y/N) : ");
				choice = in.nextLine();
			
				double premium = agent.calculateTotalValue(invite);
				//precision to 2 digits
				premium = premium * 100;
				premium = Math.round(premium);
				premium = premium/100;
				
				System.out.println("The monthly premium for this customer is : $" + premium);
			}
				
		}while(choice.equals("Y")|| choice.equals("Yes") );
	}
	
	/**
	 * This method is used to accept input for customer and get a quote using
	 * deductible and property value
	 * @author Poornima Tantry
	 */
	public void inputForUserQuotes()
	{
		String  getInput = "";
		double deductible = 0, netWorth = 0;
		long ssn = 0;
		Invitation invite = new Invitation();
		
		System.out.print("Enter your SSN : ");
		getInput = in.nextLine();
		ssn = Long.parseLong(getInput);
		
		System.out.print("Enter all expenses your property (NO COMMA): $");
		getInput = in.nextLine();
		netWorth = Double.parseDouble(getInput);
		
		System.out.print("Enter the amount of deductible (NO COMMA): $");
		getInput = in.nextLine();
		deductible = Double.parseDouble(getInput);
		
		invite = user.calculateQuoteForCustomer(ssn, deductible, netWorth, userInviteList);
		
		System.out.print("Would you like to accpet this offer(Y/N) : ");
		String input = in.nextLine();
	
		//save the customer input so that agent can be compensated.
		if(input.equals("Y") || input.equals("y"))
			invite.setAcceptInvite(RiskItConstants.ACCEPT);
		else
			invite.setAcceptInvite(RiskItConstants.DENY);
		
	}
}
