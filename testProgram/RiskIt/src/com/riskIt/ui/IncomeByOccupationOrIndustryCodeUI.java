package com.riskIt.ui;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Scanner;

import com.riskIt.interfaces.CalculateByOccupationOrIndustryCodeInterface;
import com.riskIt.interfaces.StartInterface;
import com.riskIt.util.Factory;


/**
 * IncomeByOccupationOrIndustryCodeGUI.java
 * Purpose: GUI component for CalculateByOccupationOrIndustryCode
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class IncomeByOccupationOrIndustryCodeUI implements StartInterface {

	Scanner in = new Scanner(System.in);
	HashMap<Integer,String> occupationMap = null;
	HashMap<Integer, String> industryMap = null;
	CalculateByOccupationOrIndustryCodeInterface calc = null;
	double result;
	DecimalFormat myFormatter = new DecimalFormat("$###,###,###.00");
	String type;
	String fillData;
	
	public IncomeByOccupationOrIndustryCodeUI(String type){
		if(type != null){
			this.type = type.trim();
		}
		else {
			type = "income";
		}
		
		if(type.equalsIgnoreCase("income")){
			fillData = "income";
		} else {
			fillData = "weekly wage";
		}
	}
	
	public void start() {
		
		int mainChoice = 0;
		int occupationChoice = 0;
		int industryChoice = 0;

		do{
			displayMainMenu();
			System.out.println("Please enter a selection 1 ~ 5");
			mainChoice = mainMenu();
			
			// Exit
	        if(mainChoice == 5){
	        	mainChoice = 0;
	        	return;
	        }

	        // Get Occupation Map Once each time class is run.
	        if (occupationMap == null){
	        	occupationMap = Factory.getCalculateByOccupationOrIndustryCode().getOccupationCodeAndNameMap();
	        }
	           
	        // Get Industry Map Once each time class is run.
	        if (industryMap == null){
	        	industryMap = Factory.getCalculateByOccupationOrIndustryCode().getIndustryCodeAndNameMap();
	        }
	           
	        // Occupation Code Sub menu choice
	        if(mainChoice == 1){
	        	do {
		        	occupationChoice = OccupationCodeMenu();
		        	if(occupationChoice == -2){
		        		mainChoice = 0;
		        		return;
		        	}
		        	else if(occupationChoice == -1){
		        		mainChoice = 0;
		        	}
		        	else {
		        		calc = Factory.getCalculateByOccupationOrIndustryCode();
		        		if(type.equalsIgnoreCase("income")){
		        			result = calc.calculateIncomeByOccupationCode(occupationChoice);
		        		} else {
		        			result = calc.calculateWeeklyWageByOccupationCode(occupationChoice);
		        		}
		        		
		                
		                
		            	if(result <= 0){
		            		System.out.println("\nAverage " + fillData + " for occupation code " +  occupationChoice + " : " +occupationMap.get(occupationChoice) + " is not available.  Actual value is " + myFormatter.format(result) + ".\n");
		            	} else {
		            		System.out.println("\nAverage " + fillData + " for occupation code " +  occupationChoice + " : " +occupationMap.get(occupationChoice) + " is "+ myFormatter.format(result) + ".\n");
		            	}
		                
		                
		                
		                mainChoice = 1;
		        	}
	        	}
	        	while(mainChoice == 1);
	        }
	        
	        // Industry Code Sub menu choice
	        if(mainChoice == 2){
	        	do {
		        	industryChoice = IndustryCodeMenu();
		        	if(industryChoice == -2){
		        		mainChoice = 0;
		        		return;
		        	}
		        	else if(industryChoice == -1){
		        		mainChoice = 0;
		        	}
		        	else {
		        		calc = Factory.getCalculateByOccupationOrIndustryCode();
		        		if(type.equalsIgnoreCase("income")){
		        			result = calc.calculateIncomeByIndustryCode(industryChoice);
		        		} else {
		        			result = calc.calculateWeeklyWageByIndustryCode(industryChoice);
		        		}

		                
		                
		            	if(result <= 0){
		            		System.out.println("\nAverage " + fillData + " for industry code " +  industryChoice+ " : " +industryMap.get(industryChoice) + " is not available.  Actual value is " + myFormatter.format(result) + ".\n");
		            	} else {
		            		System.out.println("\nAverage " + fillData + " for industry code " +  industryChoice + " : " + industryMap.get(industryChoice) + " is "+ myFormatter.format(result) + ".\n");
		            	}
		                
		                
		                mainChoice = 2;
		        	}
	        	}
	        	while(mainChoice == 2);
	        }
	           
	        // List Occupation Codes and Names
	        if(mainChoice == 3){
	        	displayOccupationMap();
	        	mainChoice = 0;
	        }
	           
	        // List Industry Codes and Names
	        if(mainChoice == 4){
	        	displayIndustryMap();
	        	mainChoice = 0;
	        }

		} while (true);
	}
	
	private int mainMenu(){
		int returnValue = 0;
		do{
			if (in.hasNextInt()) {
				returnValue = in.nextInt();
        		if(returnValue < 1 || returnValue >  5){
        	        displayMainMenu();
        	        System.out.println(returnValue + " is an invalid selection.");
        	        System.out.println("Please enter a selection 1 ~ 5: ");
        		}
        	}
        	else {
                displayMainMenu();
        		System.out.println(in.next() + " is an invalid selection.");
                System.out.println("Please enter a selection 1 ~ 5");
        	}

		} while (returnValue < 1 || returnValue > 5);
		
		return returnValue;
	}
	
	private int OccupationCodeMenu(){
		int returnValue = 0;
		do{
			displayOccupationMenu();
			if (in.hasNextInt()) {
				returnValue = in.nextInt();
				if(returnValue < 0 || returnValue >  occupationMap.size()){
					System.out.println(returnValue + " is an invalid selection.");
				}
			} 
			else {
				String occupationString = in.next();
				if(occupationString.trim().equalsIgnoreCase("list")){
					displayOccupationMap();
					returnValue = -1;
				} 
				else if(occupationString.trim().equalsIgnoreCase("return")){
					return -1;
				}
				else if(occupationString.trim().equalsIgnoreCase("exit")){
					return -2;
				}
				else {
					System.out.println(occupationString + " is an invalid selection.");
					returnValue = -1;
				}
			}
		} while (returnValue < 0 || returnValue > occupationMap.size());	
			
		return returnValue;
	}
	
	private int IndustryCodeMenu(){
		int returnValue = 0;
		do{
			displayIndustryMenu();
			if (in.hasNextInt()) {
				returnValue = in.nextInt();
				if(returnValue < 0 || returnValue >  industryMap.size()){
					System.out.println(returnValue + " is an invalid selection.");
				}
			} 
			else {
				String industryString = in.next();
				if(industryString.trim().equalsIgnoreCase("list")){
					displayIndustryMap();
					returnValue = -1;
				} 
				else if(industryString.trim().equalsIgnoreCase("return")){
					return -1;
				}
				else if(industryString.trim().equalsIgnoreCase("exit")){
					return -2;
				}
				else {
					System.out.println(industryString + " is an invalid selection.");
					returnValue = -1;
				}
			}
		} while (returnValue < 0 || returnValue > industryMap.size());	
			
		return returnValue;
	}
	
	private void displayMainMenu(){
		System.out.println("[1] Select an Occupation Code to display average " + fillData + " for that occupation code");
		System.out.println("[2] Select an Industry Code to display average " + fillData + " for that industry code.");
		System.out.println("[3] Display all Occupation codes associated occupation names.");
		System.out.println("[4] Display all Industry codes and associated industry names.");
		System.out.println("[5] Exit");
	}
	
	private void displayOccupationMenu(){
		System.out.println("Enter an Occupation code, or 'list' for the list of Occupation codes and descriptions.");
		System.out.println("Enter 'return' to return to the Occupation and Industry Code menu, or 'exit' to return to the main menu.");
	}
	
	private void displayIndustryMenu(){
		System.out.println("Enter a Industry code, or 'list' for the list of Industry codes and descriptions.");
		System.out.println("Enter 'return' to return to the Occupation and Industry Code menu, or 'exit' to return to the main menu.");
	}
	
	private void displayOccupationMap(){
 	   for(int i = 0; i < occupationMap.size(); i ++){
		   String stringToInsert = " : ";
		   if(i < 10){
			   stringToInsert = " " + stringToInsert;
		   }
		   System.out.println("Occupation code " + i + stringToInsert + occupationMap.get(i));
	   }
 	   System.out.println();
	}
	
	private void displayIndustryMap(){
 	   for(int i = 0; i < industryMap.size(); i ++){
		   String stringToInsert = " : ";
		   if(i < 10){
			   stringToInsert = " " + stringToInsert;
		   }
		   System.out.println("Industry code " + i + stringToInsert + industryMap.get(i));
	   }
 	   System.out.println();
	}
	
}
