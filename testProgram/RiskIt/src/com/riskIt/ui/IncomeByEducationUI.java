package com.riskIt.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

import com.riskIt.interfaces.CalculateByEducationInterface;
import com.riskIt.interfaces.StartInterface;
import com.riskIt.util.Factory;


/**
 * IncomeByEducationGUI.java
 * Purpose: GUI component for CalculatebyEducation class / features
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class IncomeByEducationUI implements StartInterface {
	
	private int listCount;
	private ArrayList<String> educationList = new ArrayList<String>();
    DecimalFormat myFormatter = new DecimalFormat("$###,###,###.00");
	String type;
	String fillData;
    
    public IncomeByEducationUI(String type){
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
		
		Scanner in = new Scanner(System.in);
        double returnValue;
        String educationSelection;
        CalculateByEducationInterface calc =  Factory.getCalculateByEducation();
        educationList = calc.getEducationLevelList();
        
		displayMenu();
    	
        // Get Selection from User
        int intChoice = 0;
        do {
            do{
            	if (in.hasNextInt()) {
            		intChoice = in.nextInt();
            		if(intChoice < 1 || intChoice >  listCount-1){
            	        displayMenu();
            	        System.out.println(intChoice + " is an invalid selection.");
            		}
            	}
            	else {
                    displayMenu();
            		System.out.println(in.next() + " is an invalid selection.");
            		intChoice = 0;
            	}
            } while (intChoice < 1 || intChoice > listCount-1);
            
            // Exit
            if(intChoice == educationList.size()+2){
            	break;
            }
            
            //*** Calculate Average for All Education Levels
            if(intChoice == educationList.size()+1){
            	educationSelection = "all education levels";
            	if(type.equalsIgnoreCase("income")){
            		returnValue = calc.calculateIncomeByEducationLevel("all");
            	} else {
            		returnValue = calc.calculateWeeklyWageByEducationLevel("all");
            	}
            }

            // Calculate the average for the specific education level
            else{
            	educationSelection = educationList.get(intChoice - 1);
            	if(type.equalsIgnoreCase("income")){
            		returnValue = calc.calculateIncomeByEducationLevel(educationSelection);
            	} else {
            		returnValue = calc.calculateWeeklyWageByEducationLevel(educationSelection);
            	}
            }
            
        	displayMenu();
        	
        	if(returnValue <= 0){
        		System.out.println("\nAverage " + fillData + " for " +  educationSelection + " is not available. Actual value = " + myFormatter.format(returnValue) + "\n");
        	} else {
        		System.out.println("\nAverage " + fillData + " for " +  educationSelection + " is " + myFormatter.format(returnValue) + ".\n");
        	}
        	
            
            
        } while (true);
	}
	
	private void displayMenu(){
    	listCount = 1;
		System.out.println("Enter an education catigory to show the average " + fillData + " for that education level.");
        for(String i : educationList){
        	System.out.println("[" + listCount++ + "] " + i);
        }       
        System.out.println("[" + listCount++ + "] " + "All education levels");
        System.out.println("[" + listCount++ + "] " + "Return");	
        System.out.println("Please enter a selection 1 ~" + (listCount-1));
	}

}
