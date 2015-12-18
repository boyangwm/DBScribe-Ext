package com.riskIt.ui;

import java.util.Scanner;
import com.riskIt.controller.*;
import com.riskIt.util.Factory;
import com.riskIt.util.*;

public class MenuChoices
{
	private UserManager user;
	private AgentManager agent;
	private Scanner in;
	private AccessorMethods acc;
	private SubMenuChoices subMenu;
    private SubMenuChoices19_24 subMenu19To24;

	public MenuChoices()
	{
		user = new UserManager();
		agent = new AgentManager();
		acc  = new AccessorMethods();
		in = new Scanner(System.in);
		subMenu = new SubMenuChoices();
        subMenu19To24 = new SubMenuChoices19_24();
	}

	/**
	 * This method creates the main menu and accepts the user input.
	 * Depending on the input choice, the particular method is called.
	 */
	public void mainMenu()
	{
		String getInput="";
		int choice = 0;
		int ind = 0;
		int occ = 0;
		String zip ="";

		//print the main menu and get user choice
		do
		{
			System.out.println("\t\t MAIN MENU");
			System.out.println("01. Select a user to calculate SCORE.");
			System.out.println("02. Functionality for browsing and selecting users.");
			System.out.println("03. Send invitations to users.");
			System.out.println("04. Create a new user.");
			System.out.println("05. Browse user properties.");
			System.out.println("06. Calculate unemployment rate for a given state.");
			System.out.println("07. Average weekly wage for a given state based on " +
									"occupationcode or industrycode " );
			System.out.println("08. Estimate a person’s income based on " +
									"his work weeks, weekly wage, and investment income.");
			System.out.println("09. Calculate average income for a given state.");
			System.out.println("10. Calculate average income for a given"
								+ " occupation or industry category.");
			System.out.println("11. Calculate average income for each race.");
			System.out.println("12. Calculate average income for each education level.");
			System.out.println("13. Update \'wage\' table, \'meanWeekWage\' field with "
									+ "average Weekly wage for a given occupation code"
									+ " and industry code.");
			System.out.println("14. Update user information according various "
								+ "life events, such as moving, changing, getting or "
								+ "losing jobs.");
			System.out.println("15. Update \'wage\' table when people change get or loose jobs");
			System.out.println("16. Update \'Industry\' and \'Occupation\' table stability value "
									+ " when too many people loose or get jobs in certain area "
									+ "in a short period of time");
			System.out.println("17. For given a state, list top five occupation category. ");
			System.out.println("18. For given a state, list top five industry category.");
			System.out.println("19. For given an occupation category or occupation code, "
									+ "list the top 5 states that have the most workers");
			System.out.println("20. For given an industry category or industrial code, "
									+ "list the top 5 states that have the most workers");
			System.out.println("21. Given a user record, recommend best state to work”.");
			System.out.println("22. Compute average education for a given occupation or industry");
			System.out.println("23. List occupations/industries with workers having highest (and lowest) education.");
			System.out.println("24. Calculate the \'likeliness to move factor\' for a person.");
			System.out.println("0. Exit");

			System.out.print("Enter your choice : ");
			getInput = in.nextLine();

			//check to see if input is right
			choice = acc.checkLetterDigit(getInput, RiskItConstants.MAIN_INVALID);

			//depending on the choice select the method
			switch(choice)
			{
			case 1:
					//get ssn of the customer and print the score value
					System.out.print("Enter SSN of the customer to calculate SCORE : ");
					getInput = in.nextLine();
					long userSSN = Integer.parseInt(getInput);
					double score = agent.calculateScore(userSSN);
					System.out.println("------------------------------------------");
					System.out.println("The score for customer with SSN : " + userSSN
											+ " is : " + score);
					System.out.println("------------------------------------------");
					break;
			case 2:
				//call method to print UI for the user-properties
					subMenu.browseUserByChoice();
					break;
			case 3:
				//print UI to choose either agent or customer
					subMenu.AgentAndUser();
					break;
			case 4:
				//print UI to accept inputs to create new customer
					subMenu.inputForCreateUser();
					break;
			case 5:
				//print UI to filter out user properties and print result
					subMenu.inputForbrowseUserProperties();
					break;
			case 6:
					//accept name of state and store it as upper case
					System.out.print("Enter the name of State : ");
					String stateName = in.nextLine();
					stateName = stateName.toUpperCase();
					System.out.println("------------------------------------------");
					user.calculateUnEmploymentRate(stateName);
					System.out.println("------------------------------------------");
					break;
			case 7:
					Factory.getWeeklyWageForCategories().start();
					break;
			case 8:
					Factory.getEstimateIncomeGUI().start();
					break;
			case 9:
					Factory.getIncomeByState().start();
					break;
			case 10:
					Factory.getIncomeByOccupationOrIndustryCode().start();
					break;
			case 11:
					Factory.getIncomeByRace().start();
					break;
			case 12:
					Factory.getIncomeByEducation().start();
					break;
			case 13:
				try{
				System.out.print("Enter industrycode ");
				getInput = in.nextLine();
				ind = Integer.parseInt(getInput);
				System.out.print("Enter occupationcode ");
				getInput = in.nextLine();
				occ = Integer.parseInt(getInput);
				user.updatewagetable(ind,occ);
				}catch(NumberFormatException e){
					System.out.println("Enter Right number");
				}
				break;
			case 14:
				try{
				System.out.print("Enter ssn number");
				getInput = in.nextLine();
				userSSN = Integer.parseInt(getInput);
				System.out.print("Enter industrycode ");
				getInput = in.nextLine();
				ind = Integer.parseInt(getInput);
				System.out.print("Enter occupationcode ");
				getInput = in.nextLine();
				occ = Integer.parseInt(getInput);
				user.userinformation(userSSN,ind,occ);
			}catch(NumberFormatException e){
				System.out.println("Enter Right number");
			}
				break;
			case 15:
				try{
				System.out.print("Enter ssn number");
				getInput = in.nextLine();
				userSSN = Integer.parseInt(getInput);
				System.out.print("Enter industrycode ");
				getInput = in.nextLine();
				ind = Integer.parseInt(getInput);
				System.out.print("Enter occupationcode ");
				getInput = in.nextLine();
				occ = Integer.parseInt(getInput);
				user.updatetable(userSSN,ind,occ);
				}catch(NumberFormatException e){
					System.out.println("Enter Right number");
				}
				break;
			case 16:
				System.out.print("Enter zip number");
				zip = in.nextLine();
				try{
				System.out.print("Enter industrycode ");
				getInput = in.nextLine();
				ind = Integer.parseInt(getInput);
				System.out.print("Enter occupationcode ");
				getInput = in.nextLine();
				occ = Integer.parseInt(getInput);
				user.updatestability(zip,ind,occ);
				}catch(NumberFormatException e){
					System.out.println("Enter Right number");
				}
				break;
			case 17:
				System.out.print("Enter the name of the state");
				String state = in.nextLine();
				state = state.toUpperCase();
				user. FindTopOccupationCode(state);
				break;
			case 18:
				System.out.print("Enter the name of the state");
				String statename = in.nextLine();
				statename = statename.toUpperCase();
				user. FindTopIndustryCode(statename);
				break;
			case 19:
				subMenu19To24.FindTopStatesMenu("occupation");
                		break;
			case 20:
				subMenu19To24.FindTopStatesMenu("industry");
                		break;
			case 21:
				subMenu19To24.RecommendBestStateToWorkMenu();
                		break;
			case 22:
				subMenu19To24.ComputeAverageEducationMenu();
                		break;
			case 23:
				subMenu19To24.FindOccIndustryWithHighestLowestEducationMenu();
                		break;
			case 24:
				subMenu19To24.CalculateLikelinessToMoveFactorMenu();
                		break;
			case 25:
				System.out.println("Invalid input...Try again!!");
				break;
			default :
					System.out.println("Thank you, visit us again. Good Bye");
					break;
			}
		}while(choice > 0 && choice < 26);
	}



}
