package com.riskIt.ui;

import java.text.DecimalFormat;
import java.util.Scanner;

import com.riskIt.interfaces.EstimateIncomeDTOInterface;
import com.riskIt.interfaces.EstimateIncomeInterface;
import com.riskIt.interfaces.StartInterface;
import com.riskIt.util.Factory;



/**
 * EstimateIncomeGUI.java
 * Purpose: GUI Component for EstiamteIncome Class / Requirement.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class EstimateIncomeUI implements StartInterface {

	Scanner in = new Scanner(System.in);
	EstimateIncomeInterface calc = Factory.getEstimateIncome();
	DecimalFormat myFormatter = new DecimalFormat("$###,###,###.00");
	
	double valueToCalculate;
	double resultAll;
	double slopeAll;
	double interceptAll;
	double resultMiddleTwoThirds;
	double slopeMiddleTwoThirds;
	double interceptMiddleTwoThirds;
	
	public void start() {
		do {
			
			int intChoice = 0;

			displayMainMenu();
			do {
				if(in.hasNextInt()){
					intChoice = in.nextInt();	
	        		if(intChoice < 1 || intChoice >  5){
	        			displayMainMenu();
	        	        System.out.println(intChoice + " is an invalid selection.");
	        	        System.out.println("Please enter a selection 1 ~ 5.");
	        		}
				} else {
					displayMainMenu();
					String wrong = in.next();
	        		System.out.println(wrong + " is an invalid selection.");
	                System.out.println("Please enter a selection 1 ~ 5.");
	                intChoice = 0;
				}
			} while (intChoice < 1 || intChoice > 5);
			
			if(intChoice == 5){
				return;
			}
			
			if(intChoice == 1){
				intChoice = estimateBasedOnWorkWeeks();
			} else if(intChoice == 2){
				intChoice = estimateBasedOnWeeklyWage();
			} else if(intChoice == 3){
				intChoice = estimateBasedOnInvestmentIncome();
			} else if(intChoice == 4){
				intChoice = estimateBasedOnSSN();
			}
			
			if(intChoice == 5){
				return;
			}
			
		} while (true);

	}
	
	public int estimateBasedOnInvestmentIncome(){
		do {
			double InvestmentIncome = -1.0;
			System.out.println("Please enter an investment income value to estimate income based on that value.");
			System.out.println("Enter 'return' to return to the previous menu, or 'exit' to exit.");
				if(in.hasNextInt()){
					InvestmentIncome = in.nextDouble();	
	        		if(InvestmentIncome < 0){
	        	        System.out.println(InvestmentIncome + " is an invalid selection.");
	        	        System.out.println("Please enter an investment income value of 0 or more.");
	        		}
				} else {
					String stringAnswer = in.next();
					stringAnswer = stringAnswer.trim();
					if(stringAnswer.equalsIgnoreCase("return")){
						return 0;
					} else if(stringAnswer.equalsIgnoreCase("exit")){
						return 5;
					} else {
		        		System.out.println(stringAnswer + " is an invalid selection.");
		                System.out.println("Please enter a numeric investment income value.");
		                InvestmentIncome = -1.0;
					}
				}
				
				if(InvestmentIncome >= 0){
					resultAll = calc.givenInvestmentIncomeEstimateIncomeAll(InvestmentIncome);
					slopeAll = calc.givenInvestmentIncomeEstimateIncomeGetSlopeAll();
					interceptAll = calc.givenInvestmentIncomeEstimateIncomeGetInterceptAll();
					
					resultMiddleTwoThirds = calc.givenInvestmentIncomeEstimateIncomeMiddleTwoThirds(InvestmentIncome);
					slopeMiddleTwoThirds = calc.givenInvestmentIncomeEstimateIncomeGetSlopeMiddleTwoThirds();
					interceptMiddleTwoThirds = calc.givenInvestmentIncomeEstimateIncomeGetInterceptMiddleTwoThirds();
					
					printAnswerAll("investment income", InvestmentIncome, resultAll, slopeAll, interceptAll);
					printAnswerMiddleTwoThirds("investment income", InvestmentIncome, resultMiddleTwoThirds, slopeMiddleTwoThirds, interceptMiddleTwoThirds);
				}

		} while (true);
		
	}
	
	public int estimateBasedOnWeeklyWage(){
		do {
			
			double weeklyWageChoice = 0.0;
			System.out.println("Please enter a weekly wage value to estimate income based on that weekly wage.");
			System.out.println("Enter 'return' to return to the previous menu, or 'exit' to exit.");
				if(in.hasNextInt()){
					weeklyWageChoice = in.nextDouble();	
	        		if(weeklyWageChoice <= 0){
	        	        System.out.println(weeklyWageChoice + " is an invalid selection.");
	        	        System.out.println("Please enter a weekly wage value greater than 0.");
	        		}
				} else {
					String stringAnswer = in.next();
					stringAnswer = stringAnswer.trim();
					if(stringAnswer.equalsIgnoreCase("return")){
						return 0;
					} else if(stringAnswer.equalsIgnoreCase("exit")){
						return 5;
					} else {
		        		System.out.println(stringAnswer + " is an invalid selection.");
		                System.out.println("Please enter a numeric weekly wage value greater than 0.");
		                weeklyWageChoice = 0.0;
					}
				}
				
				if(weeklyWageChoice> 0){
					resultAll = calc.givenWeeklyWageEstimateIncomeAll(weeklyWageChoice);
					slopeAll = calc.givenWeeklyWageEstimateIncomeGetSlopeAll();
					interceptAll = calc.givenWeeklyWageEstimateIncomeGetInterceptAll();
					
					resultMiddleTwoThirds = calc.givenWeeklyWageEstimateIncomeMiddleTwoThirds(weeklyWageChoice);
					slopeMiddleTwoThirds = calc.givenWeeklyWageEstimateIncomeGetSlopeMiddleTwoThirds();
					interceptMiddleTwoThirds = calc.givenWeeklyWageEstimateIncomeGetInterceptMiddleTwoThirds();
					
					printAnswerAll("weekly wage", weeklyWageChoice, resultAll, slopeAll, interceptAll);
					printAnswerMiddleTwoThirds("weekly wage", weeklyWageChoice, resultMiddleTwoThirds, slopeMiddleTwoThirds, interceptMiddleTwoThirds);
				}

		} while (true);
		
	}
	
	public int estimateBasedOnWorkWeeks(){
		do {
			
			int workWeekChoice = 0;
			System.out.println("Please enter the number of work weeks (1 ~ 52) to estimate income based on that number of work weeks.");
			System.out.println("Enter 'return' to return to the previous menu, or 'exit' to exit.");
				if(in.hasNextInt()){
					workWeekChoice = in.nextInt();	
	        		if(workWeekChoice < 1 || workWeekChoice >  52){
	        	        System.out.println(workWeekChoice + " is an invalid selection.");
	        	        System.out.println("Please enter a selection 1 ~ 52 weeks.");
	        		}
				} else {
					String stringAnswer = in.next();
					stringAnswer = stringAnswer.trim();
					if(stringAnswer.equalsIgnoreCase("return")){
						return 0;
					} else if(stringAnswer.equalsIgnoreCase("exit")){
						return 5;
					} else {
		        		System.out.println(stringAnswer  + " is an invalid selection.");
		                System.out.println("Please enter a numeric selection 1 ~ 52 weeks.");
		                workWeekChoice = 0;
					}
				}
				
				if(workWeekChoice > 0 && workWeekChoice <= 52){
					resultAll = calc.givenWorkWeeksEstimateIncomeAll(workWeekChoice);
					slopeAll = calc.givenWorkWeeksEstimateIncomeGetSlopeAll();
					interceptAll = calc.givenWorkWeeksEstimateIncomeGetInterceptAll();
					
					resultMiddleTwoThirds = calc.givenWorkWeeksEstimateIncomeMiddleTwoThirds(workWeekChoice);
					slopeMiddleTwoThirds = calc.givenWorkWeeksEstimateIncomeGetSlopeMiddleTwoThirds();
					interceptMiddleTwoThirds = calc.givenWorkWeeksEstimateIncomeGetInterceptMiddleTwoThirds();
					
					printAnswerAll("work week", workWeekChoice, resultAll, slopeAll, interceptAll);
					printAnswerMiddleTwoThirds("work week", workWeekChoice, resultMiddleTwoThirds, slopeMiddleTwoThirds, interceptMiddleTwoThirds);
				}

		} while (true);
		
	}
	
	public int estimateBasedOnSSN(){
		do {
			
			int SSNChoice = 0;
			System.out.println("Please enter a SSN to estimate income based on values from the database.");
			System.out.println(" Suggested good values = 101894625 or 101894739 etc..");
			System.out.println("Enter 'return' to return to the previous menu, or 'exit' to exit.");
				if(in.hasNextInt()){
					SSNChoice = in.nextInt();	
	        		if(SSNChoice < 100000000 || SSNChoice > 999999999){
	        	        System.out.println(SSNChoice + " is an invalid selection.");
	        	        System.out.println("Please enter a valid SSN.");
	        		}
				} else {
					String stringAnswer = in.next();
					stringAnswer = stringAnswer.trim();
					if(stringAnswer.equalsIgnoreCase("return")){
						return 0;
					} else if(stringAnswer.equalsIgnoreCase("exit")){
						return 5;
					} else {
		        		System.out.println(stringAnswer + " is an invalid selection.");
		                System.out.println("Please enter a valid SSN.");
		                System.out.println();
		                SSNChoice = 0;
					}
				}
				
				if(SSNChoice >= 100000000 && SSNChoice <= 999999999){
					// Handle if no record is returned
					EstimateIncomeDTOInterface data = calc.getValues(SSNChoice);
					
					if(data.getName() == null || data.getName().trim().equalsIgnoreCase("None Entered")){
						System.out.println("No data found in the database for SSN of " + SSNChoice);
					} else{
						
						System.out.println();
						System.out.println(data.toString());
						System.out.println();
						
						if(data.getWorkWeeks() > 0){
							resultAll = calc.givenWorkWeeksEstimateIncomeAll(data.getWorkWeeks());
							slopeAll = calc.givenWorkWeeksEstimateIncomeGetSlopeAll();
							interceptAll = calc.givenWorkWeeksEstimateIncomeGetInterceptAll();
							System.out.println("Based on " + data.getWorkWeeks() + " work weeks the estimated income is:");
							System.out.println(myFormatter.format(resultAll) + " based on all the data in the database.");
							System.out.println("Using a regression formula of Income = work weeks * " + slopeAll + " + " + interceptAll);

							System.out.println();
							
							resultMiddleTwoThirds = calc.givenWorkWeeksEstimateIncomeMiddleTwoThirds(data.getWorkWeeks());
							slopeMiddleTwoThirds = calc.givenWorkWeeksEstimateIncomeGetSlopeMiddleTwoThirds();
							interceptMiddleTwoThirds = calc.givenWorkWeeksEstimateIncomeGetInterceptMiddleTwoThirds();
							System.out.println("Based on the middle two thirds of data in the database the estiamte income is " + myFormatter.format(resultMiddleTwoThirds));
							System.out.println("Using a regression formula of Income = work weeks * " + slopeMiddleTwoThirds + " + " + interceptMiddleTwoThirds);
							System.out.println();

						} else {
							System.out.println("No work week data available for this record.\n");
						}
						
						if(data.getWeeklyWage() > 0){
							resultAll = calc.givenWeeklyWageEstimateIncomeAll(data.getWeeklyWage());
							slopeAll = calc.givenWeeklyWageEstimateIncomeGetSlopeAll();
							interceptAll = calc.givenWeeklyWageEstimateIncomeGetInterceptAll();
							System.out.println("Based on weekly wage of " + myFormatter.format(data.getWeeklyWage()) + " the estimated income is:");
							System.out.println(myFormatter.format(resultAll) + " based on all the data in the database.");
							System.out.println("Using a regression formula of Income = weekly wage * " + slopeAll + " + " + interceptAll);

							System.out.println();
							
							resultMiddleTwoThirds = calc.givenWeeklyWageEstimateIncomeMiddleTwoThirds(data.getWeeklyWage());
							slopeMiddleTwoThirds = calc.givenWeeklyWageEstimateIncomeGetSlopeMiddleTwoThirds();
							interceptMiddleTwoThirds = calc.givenWeeklyWageEstimateIncomeGetInterceptMiddleTwoThirds();
							System.out.println("Based on the middle two thirds of data in the database the estimated income is " + myFormatter.format(resultMiddleTwoThirds));
							System.out.println("Using a regression formula of Income = weekly wage * " + slopeMiddleTwoThirds + " + " + interceptMiddleTwoThirds);
							System.out.println();

						} else {
							System.out.println("No weekly wage data available for this record.\n");
						}
						
						if(data.getInvestmentIncome() > 0){
							resultAll = calc.givenInvestmentIncomeEstimateIncomeAll(data.getInvestmentIncome());
							slopeAll = calc.givenInvestmentIncomeEstimateIncomeGetSlopeAll();
							interceptAll = calc.givenInvestmentIncomeEstimateIncomeGetInterceptAll();
							System.out.println("Based on investment income of " + myFormatter.format(data.getInvestmentIncome()) + " the estimated income is:");
							System.out.println(myFormatter.format(resultAll) + " based on all the data in the database.");
							System.out.println("Using a regression formula of Income = investment income * " + slopeAll + " + " + interceptAll);

							System.out.println();
							
							resultMiddleTwoThirds = calc.givenInvestmentIncomeEstimateIncomeMiddleTwoThirds(data.getInvestmentIncome());
							slopeMiddleTwoThirds = calc.givenInvestmentIncomeEstimateIncomeGetSlopeMiddleTwoThirds();
							interceptMiddleTwoThirds = calc.givenInvestmentIncomeEstimateIncomeGetInterceptMiddleTwoThirds();
							System.out.println("Based on the middle two thirds of data in the database the estimated income is " + myFormatter.format(resultMiddleTwoThirds));
							System.out.println("Using a regression formula of Income = investment income * " + slopeMiddleTwoThirds + " + " + interceptMiddleTwoThirds);
							System.out.println();

						} else {
							System.out.println("No investment income data available for this record.\n");
						}
						
						if(data.getWeeklyWage() > 0 && data.getWorkWeeks() > 0){
							System.out.println("Actual income = " + myFormatter.format(data.getWeeklyWage() * data.getWorkWeeks()));
						} else {
							System.out.println("Database does not contain enough information to calculate actual income value.");
						}
						System.out.println();	
					}
				}

		} while (true);
		
	}
	
	public void printAnswerAll(String type, double value, double resultAll, double slopeAll, double intercpetAll){
		System.out.println();
		System.out.println("Simple Linear Regression based on all data in the database:");
		System.out.println("Based on " + type + " value of " + myFormatter.format(value) + " the estimated income is " + myFormatter.format(resultAll) );
		System.out.println("This estimate is based on simple linear regression based on all the " + type + " and income data");
		System.out.println("contained in the database using a regression formula of:");
		System.out.println("Income = " + type + " * " + slopeAll + " + " + intercpetAll);
		System.out.println();
	}
	
	public void printAnswerAll(String type, int value, double resultAll, double slopeAll, double intercpetAll){
		System.out.println();
		System.out.println("Simple Linear Regression based on all data in the database:");
		System.out.println("Based on " + type + " value of " + value + " the estimated income is " + myFormatter.format(resultAll) );
		System.out.println("This estimate is based on simple linear regression based on all the " + type + " and income data");
		System.out.println("contained in the database using a regression formula of:");
		System.out.println("Income = " + type + " * " + slopeAll + " + " + intercpetAll);
		System.out.println();
	}
	
	public void printAnswerMiddleTwoThirds(String type, int value, double resultMiddleTwoThirds, double slopeMiddleTwoThirds, double intercpetMiddleTwoThirds){
		System.out.println();
		System.out.println("Simple Linear Regression based on the middle two thrids of data in the database sorted based on " + type + ":");
		System.out.println("Based on " + type + " value of " + value + " the estimated income is " + myFormatter.format(resultMiddleTwoThirds) );
		System.out.println("This estimate is based on simple linear regression based on the middle two thirds of the " + type + " and");
		System.out.println("income data contained in the database (sorted based on " + type + " ) using a regression formula of:");
		System.out.println("Income = " + type + " * " + slopeMiddleTwoThirds + " + " + intercpetMiddleTwoThirds);
		System.out.println();
	}
	
	public void printAnswerMiddleTwoThirds(String type, double value, double resultMiddleTwoThirds, double slopeMiddleTwoThirds, double intercpetMiddleTwoThirds){
		System.out.println();
		System.out.println("Simple Linear Regression based on the middle two thrids of data in the database sorted based on " + type + ":");
		System.out.println("Based on " + type + " value of " + myFormatter.format(value) + " the estimated income is " + myFormatter.format(resultMiddleTwoThirds) );
		System.out.println("This estimate is based on simple linear regression based on the middle two thirds of the " + type + " and");
		System.out.println("income data contained in the database (sorted based on " + type + " ) using a regression formula of:");
		System.out.println("Income = " + type + " * " + slopeMiddleTwoThirds + " + " + intercpetMiddleTwoThirds);
		System.out.println();
	}
	
	public void displayMainMenu(){
		System.out.println("Please select a catigory to estimate a persons Income based on:");
		System.out.println("[1] Work Weeks");
		System.out.println("[2] Weekly Wage");
		System.out.println("[3] Investment Income");
		System.out.println("[4] Enter a SSN to calculate based on a record in the database");
		System.out.println("[5] Exit");
	}

}
	
	

