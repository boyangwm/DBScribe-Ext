package com.riskIt.ui;

import java.util.ArrayList;
import java.util.Scanner;

import com.riskIt.interfaces.StartInterface;
import com.riskIt.util.Factory;


/**
 * CalculateWeeklyWageGUI.java
 * Purpose: GUI component for calculating average income and average
 * 	weekly wage for requirement 4.
 * 
 * Actual calculations for weekly values are delegated (via factory) to :
 * 
 * CalculateByState Class
 * CalculatebyOccupationOrIndustryCode Class
 * CalculateByEducation Class
 * CalculateByRace Class
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class CalculateWeeklyWageUI implements StartInterface{
	private Scanner in = new Scanner(System.in);
	private int listCount;
	
	ArrayList<String> optionList = new ArrayList<String>();

	public void start() {
		
		optionList.add("State"); 						// 1
		optionList.add("Occupation or Industry Code"); 	// 2
		optionList.add("Education Level"); 				// 3
		optionList.add("Race"); 						// 4

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
            if(intChoice == optionList.size()+1){
            	break;
            }
            
            if(intChoice == 1){
            	Factory.getWeeklyWageByState().start();
            	intChoice = 0;
            }
            else if(intChoice == 2){
            	Factory.getWeeklyWageByOccupationOrIndustryCode().start();
            	intChoice = 0;
            }
            else if(intChoice == 3){
            	Factory.getWeeklyWageByEducation().start();
            	intChoice = 0;
            }
            else if(intChoice == 4) {
            	Factory.getWeeklyWageByRace().start();
            	intChoice = 0;
            }

        	displayMenu();
        	
        } while (true);	
	}
	
	private void displayMenu(){
    	listCount = 1;
		System.out.println("Enter a catigory to display average weekly wage for: ");
        for(String i : optionList){
        	System.out.println("[" + listCount++ + "] " + i);
        }       
        System.out.println("[" + listCount++ + "] " + "Return");	
        System.out.println("Please enter a selection 1 ~" + (listCount-1));
	}
}
