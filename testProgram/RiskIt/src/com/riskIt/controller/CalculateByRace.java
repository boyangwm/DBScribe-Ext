package com.riskIt.controller;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import tada.TaDaMethod;

import com.riskIt.interfaces.CalcImplInterface;
import com.riskIt.interfaces.CalculateByRaceInterface;
import com.riskIt.interfaces.TypeWageDataStructureInterface;
import com.riskIt.util.Factory;


/**
 * CalculateByRace.java
 * Purpose: Calculates average income and average weekly wage for
 * 	requirements 4 and 8.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

/**
 * Object to perform all calculations necessary to calculate
 * 	average income and average weekly wage by all or by a given
 * 	race category contained in the database. Uses Hash
 *  Maps and offloads processing to the JVM to overcome inherent
 *  inefficiencies in the Derby database thus significantly improving
 *  performance.
 */
public class CalculateByRace implements CalculateByRaceInterface {
	
	ResultSet results;
    Statement statement;

	/**
	 * Returns a list with all the race categories contained in the database.
	 * <p>
	 * @return				An ArrayList<String> of all race categories.
	 */
    @TaDaMethod(variablesToTrack ={"raceList"},
			correspondingDatabaseAttribute = {"userrecord.race"})
	public ArrayList<String> getRaceList(){
	    ArrayList<String> raceList = new ArrayList<String>();
	    
		try {
			statement = Factory.getConnection().createStatement();
	        results = statement.executeQuery("SELECT DISTINCT RACE FROM userrecord");
	        while(results.next()){
	        	if(results.getString("RACE") == null || raceList.contains(results.getString("RACE"))){
	        		continue;
	        	}
	        	raceList.add(results.getString("RACE").trim());
	        }
		} catch (SQLException e) {
	        while (e != null)
	        {
	            System.err.println("\n----- SQLException -----");
	            System.err.println("  SQL State:  " + e.getSQLState());
	            System.err.println("  Error Code: " + e.getErrorCode());
	            System.err.println("  Message:    " + e.getMessage());
	            // for stack traces, refer to derby.log or uncomment this:
	            //e.printStackTrace(System.err);
	            e = e.getNextException();
	        }
		}
		
        Collections.sort(raceList);
		return raceList;
	}
	
	/**
	 * Checks if a race categories is contained in the database
	 * <p>
	 * @param 				A String of a race categories to test
	 * @return				Boolean true / false answer
	 */
	public boolean isInRaceList(String race) {
		ArrayList<String> raceList = getRaceList();
		if(race == null){
			race = "";
		} else {
			race = race.trim();
		}
		
		for(String i: raceList){
			if (i.trim().equalsIgnoreCase(race)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates average weekly wage for a race category
	 * <p>
	 * @param 				A String of a race category to request
	 * 						Warning: This method does not check if the 
	 * 							race string submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the string is contained
	 * 							in the db use the method :
	 * 							isInRaceList(String)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 */
	public double calculateWeeklyWageByRace(String race) {
		return calculateValue(race, Factory.getWeeklyImpl());
	}

	/**
	 * Calculates average weekly wage for all race categories
	 * <p>
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 */
	public double calculateWeelkyWageByAllRaces() {
		return calculateValue("all", Factory.getWeeklyImpl());
	}

	/**
	 * Calculates average income for a race category
	 * <p>
	 * @param 				A String of a race category to request
	 * 						Warning: This method does not check if the 
	 * 							race string submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the string is contained
	 * 							in the db use the method :
	 * 							isInRaceList(String)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 */
	public double calculateIncomeByRace(String race) {
		return calculateValue(race, Factory.getYearlyImpl());
	}
	
	/**
	 * Calculates average income for all race categories
	 * <p>
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 */
	public double calculateIncomeByAllRaces() {
		return calculateValue("all", Factory.getYearlyImpl());
	}
	
	
	 @TaDaMethod(variablesToTrack ={"tempSSN", "weekWage", "worksWeek", "tempSSN2", "ssnRaceHM"},
				correspondingDatabaseAttribute = {"job.ssn", "job.WEEKWAGE", "job.WORKWEEKS", "userrecord.ssn", "userrecord.race"})
	public double calculateValue(String raceIn, CalcImplInterface type) {
		double returnValue = 0;
		double sum = 0;
		double count = 0;

		HashMap<Integer,String> ssnRaceHM = new HashMap<Integer,String>();
		HashMap<Integer, Integer> ssnWeekWageHM = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> ssnWorkWeeksHM = new HashMap<Integer, Integer>();
        ArrayList<TypeWageDataStructureInterface> raceWageAL = new ArrayList<TypeWageDataStructureInterface>();
      
		if(raceIn == null){
			raceIn = "";
		} else {
			raceIn = raceIn.trim();
		}
		
		raceIn = raceIn.trim();
        
		try {
			statement = Factory.getConnection().createStatement();
		
	        results = statement.executeQuery("SELECT SSN, WEEKWAGE, WORKWEEKS from job");
	        while(results.next()){
	        	int tempSSN = results.getInt("SSN");
	        	int weekWage= results.getInt("WEEKWAGE");
	        	int worksWeek = results.getInt("WORKWEEKS");
	        	
	        	if(weekWage > 0) {	
	        		ssnWeekWageHM.put(tempSSN, weekWage);
	        	}
	        	
	        	if(worksWeek > 0){
	        		ssnWorkWeeksHM.put(tempSSN, worksWeek);
	        	}
	        }
			
	        results = statement.executeQuery("SELECT SSN, RACE from userrecord");
	        while(results.next()){
	        	
	        	int tempSSN2 = results.getInt("SSN");
	        	if(ssnWeekWageHM.containsKey(tempSSN2)){
	        		ssnRaceHM.put(tempSSN2, results.getString("RACE"));
	        	}
	        }

	        for (Iterator<Integer> i = ssnRaceHM.keySet().iterator(); i.hasNext();) { 
	        	int keySSN = (Integer) i.next(); 
	        	int weeklyWage;
	        	if(ssnWeekWageHM.get(keySSN) != null){
	        		weeklyWage = ssnWeekWageHM.get(keySSN);
	        	} else{
	        		weeklyWage = 0;
	        	}
	        	int workWeek;
	        	if(ssnWorkWeeksHM.get(keySSN) != null){
	        		workWeek = ssnWorkWeeksHM.get(keySSN);
	        	} else {
	        		workWeek = 0;
	        	}
	        	String raceFromDB = ssnRaceHM.get(keySSN).trim();
	        	
	        	type.addToArrayList(raceWageAL, raceFromDB, weeklyWage, workWeek);
	        }
	        
	    	for(TypeWageDataStructureInterface i : raceWageAL){
	    		double result = type.calculateAverage(raceIn, i);
				if(result > 0){
					sum = sum + result;
					count++;
				}
	    	}
	        
	        if(count == 0){
	        	returnValue = 0;
	        }
	        else {
	        	returnValue = sum / count;
	        }

		}
		catch (SQLException e){
	        while (e != null)
	        {
	            System.err.println("\n----- SQLException -----");
	            System.err.println("  SQL State:  " + e.getSQLState());
	            System.err.println("  Error Code: " + e.getErrorCode());
	            System.err.println("  Message:    " + e.getMessage());
	            // for stack traces, refer to derby.log or uncomment this:
	            //e.printStackTrace(System.err);
	            e = e.getNextException();
	        }
			throw new IllegalArgumentException();
		}
		
		return Factory.getRoundMethod(returnValue);
	}
	
}
