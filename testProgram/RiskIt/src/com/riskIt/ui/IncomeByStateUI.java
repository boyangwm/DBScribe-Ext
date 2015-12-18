package com.riskIt.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

import com.riskIt.interfaces.CalculateByStateInterface;
import com.riskIt.interfaces.StartInterface;
import com.riskIt.util.Factory;


/**
 * IncomeByStateGUI.java
 * Purpose: GUI component for CalculatebyState class / requirement
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class IncomeByStateUI implements StartInterface {
	String type;
	String fillData;
	
	public IncomeByStateUI(String type){
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
	
	private ArrayList<String> stateList = new ArrayList<String>();

	public void start() {
		
		Scanner in = new Scanner(System.in);
        double returnValue;
        String stateSelection;
        CalculateByStateInterface calc = Factory.getCalculateByState();
        stateList = calc.getStateList();
        boolean found = false;
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###.00");

        // Get Selection from User
        do {
            System.out.println("\nPlease enter a state to show the average " + fillData + " for that state.");
        	System.out.println("Enter a two digit state code or 'all' for the average from all states.");
        	System.out.println("For a list of all state codes type 'list', or type 'exit' to return to the main menu.");
        	stateSelection = in.nextLine();
        	stateSelection = stateSelection.trim().toUpperCase();
        	
        	if(stateSelection.equalsIgnoreCase("exit")){
        		break;
        	}
        	
        	if(stateSelection.equalsIgnoreCase("list")){
        		for(String i : stateList){
        			System.out.println(i);
        		}
        	}
        	else {
            	for(String i : stateList){
            		if(i.equalsIgnoreCase(stateSelection) || stateSelection.equalsIgnoreCase("all")) {
            			found = true;
            		}
            	}
        	}
        	
        	if(found == false && !(stateSelection.equalsIgnoreCase("list"))){
        		System.out.println(stateSelection + " is not a valid state code contained in the data base.");
        	}
        	
            if(found == true){
            	
            	if(type.equalsIgnoreCase("income")){
            		returnValue = calc.calculateIncomeByState(stateSelection);
            	} else {
            		returnValue = calc.calculateWeeklyWageByState(stateSelection);
            	}
            	
            	if(stateSelection.equalsIgnoreCase("all")){
            		stateSelection = "all states";
            	}
                
                
            	if(returnValue <= 0){
            		System.out.println("\nAverage " + fillData + " for " +  stateSelection + " is not available. Actual value = " + myFormatter.format(returnValue) + "\n");
            	} else {
            		System.out.println("\nAverage " + fillData + " for " + stateSelection + " is " + myFormatter.format(returnValue) + ".");
            	}
                
                
                
                found = false;
            }
        	
        } while (true);
	}
}
