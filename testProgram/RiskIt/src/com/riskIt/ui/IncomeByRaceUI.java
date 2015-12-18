package com.riskIt.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

import com.riskIt.interfaces.CalculateByRaceInterface;
import com.riskIt.interfaces.StartInterface;
import com.riskIt.util.Factory;


/**
 * IncomeByRaceGUI.java
 * Purpose: GUI component for CalculateByRace Class / Features
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class IncomeByRaceUI implements StartInterface {
	
	private int listCount;
	private ArrayList<String> raceList = new ArrayList<String>();
    DecimalFormat myFormatter = new DecimalFormat("$###,###,###.00");
    String type;
    String fillData;
    
    public IncomeByRaceUI(String type){
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
        String raceSelection;
        CalculateByRaceInterface calc = Factory.getCalculateByRace();
        raceList = calc.getRaceList();
        
    	System.out.println("\nEnter a race catigory to show the average " + fillData + " for that race.");
    	displayMenu();
    	System.out.println("Please enter a selection 1 ~" + (listCount-1));
    	
        // Get Selection from User
        int intChoice = 0;
        do {
            do{
            	if (in.hasNextInt()) {
            		intChoice = in.nextInt();
            		if(intChoice < 1 || intChoice >  listCount-1){
            	        displayMenu();
            	        System.out.println(intChoice + " is an invalid selection.");
            	        System.out.println("Please enter a selection 1 ~" + (listCount-1));
            		}
            	}
            	else {
                    displayMenu();
            		System.out.println(in.next() + " is an invalid selection.");
                    System.out.println("Please enter a selection 1 ~" + (listCount-1));
                    intChoice = 0;
            	}
            } while (intChoice < 1 || intChoice > listCount-1);
            
            // Exit
            if(intChoice == raceList.size()+2){
            	break;
            }
            
            //*** Calculate Average for All Races
            if(intChoice == raceList.size()+1){
            	raceSelection = "all races";
            	
            	if(type.equalsIgnoreCase("income")){
            		returnValue = calc.calculateIncomeByRace("All");
            	} else {
            		returnValue = calc.calculateWeeklyWageByRace("All");
            	}
            }

            // Calculate the average for the specific race
            else{
            	raceSelection = raceList.get(intChoice - 1);
            	
            	if(type.equalsIgnoreCase("income")){
            		returnValue = calc.calculateIncomeByRace(raceSelection);
            	} else {
            		returnValue = calc.calculateWeeklyWageByRace(raceSelection);
            	}
            }
            
        	System.out.println("Please enter a race catigory to show the average " + fillData + " for that race.");
        	displayMenu();
        	
        	if(returnValue <= 0){
        		System.out.println("\nAverage " + fillData + " for " +  raceSelection + " is not available. Actual value = " + myFormatter.format(returnValue) + "\n");
        	} else {
        		System.out.println("\nAverage " + fillData + " for " +  raceSelection + " is " + myFormatter.format(returnValue) + ".\n");
        	}
        	
        	System.out.println("Please enter a selection 1 ~" + (listCount-1));

        } while (true);
	}
	
	private void displayMenu(){
    	listCount = 1;
        for(String i : raceList){
        	System.out.println("[" + listCount++ + "] " + i);
        }       
        System.out.println("[" + listCount++ + "] " + "All Races");
        System.out.println("[" + listCount++ + "] " + "Return");
	}
}

/*	Testing through ij
 * Average income for Other 957.98 - confirmed throgh ij
 * Average income for all races race is $976.56 Confirmed through IJ
 * Average income for White race is $986.97  confirmed through IJ
 * 
 */

 