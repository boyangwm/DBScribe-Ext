package com.riskIt.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import tada.TaDaMethod;

import com.riskIt.interfaces.CalcImplInterface;
import com.riskIt.interfaces.CalculateByEducationInterface;
import com.riskIt.interfaces.TypeWageDataStructureInterface;
import com.riskIt.util.Factory;


/**
 * CalculateByEducation.java
 * Purpose: Calculates average income and average weekly wage for
 * 	requirements 4 & 9
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

/**
 * Object to perform all calculations necessary to calculate
 * 	average income and average weekly wage by all or by a given
 * 	education level contained in the database.  Uses Hash
 *  Maps and offloads processing to the JVM to overcome inherent
 *  inefficiencies in the Derby database thus significantly improving
 *  performance.
 */
public class CalculateByEducation implements  CalculateByEducationInterface{
	ResultSet results;
	Statement statement;
	
	/**
	 * Returns a list with all the Education Levels contained in the database.
	 * <p>
	 * @return				An ArrayList<String> of all education levels.
	 */
	@TaDaMethod(variablesToTrack = {"education"}, correspondingDatabaseAttribute ={"education.education"})
	public ArrayList<String> getEducationLevelList() {
		ArrayList<String> educationList = new ArrayList<String>();
		String education;
		
		try{
			statement = Factory.getConnection().createStatement();
	    	results = statement.executeQuery("SELECT DISTINCT EDUCATION from education");

	    	while(results.next()){
	    		education = results.getString("EDUCATION");
		    	if(education != null){
		    		education = education.trim();
		    		if(!educationList.contains(education)){
		    			educationList.add(education);
		    		}
		    	}
	    	}
	    	
	    	
			
		} catch (SQLException e) {
			while (e != null) {
				System.err.println("\n----- SQLException -----");
				System.err.println("  SQL State:  " + e.getSQLState());
				System.err.println("  Error Code: " + e.getErrorCode());
				System.err.println("  Message:    " + e.getMessage());
				// for stack traces, refer to derby.log or uncomment this:
				// e.printStackTrace(System.err);
				e = e.getNextException();
			}
		}
		
		Collections.sort(educationList);
		return educationList;
	}

	/**
	 * Checks if an education level is contained in the database
	 * <p>
	 * @param 				A String of an education level to test
	 * @return				Boolean true / false answer
	 */
	public boolean isInEducationList(String education) {
		ArrayList<String> educationList = getEducationLevelList();
		for(String i : educationList){
			if(education != null && i.equalsIgnoreCase(education.trim())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Calculates average income for a given education level
	 * <p>
	 * @param 				A String of an education level to request
	 * 						Warning: This method does not check if the 
	 * 							education string submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the string is contained
	 * 							in the db use the method :
	 * 							isInEducationList(String)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 */
	public double calculateIncomeByEducationLevel(String education){
		return calculateValue(education, Factory.getYearlyImpl());
	}
	
	/**
	 * Calculates average weekly wage for a given education level
	 * <p>
	 * @param 				A String of an education level to request
	 * 						Warning: This method does not check if the 
	 * 							education string submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the string is contained
	 * 							in the db use the method :
	 * 							isInEducationList(String)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 */
	public double calculateWeeklyWageByEducationLevel(String education){
		return calculateValue(education, Factory.getWeeklyImpl());
	}
	
	/**
	 * Calculates average income for all education levels
	 * <p>
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 * 
	 */
	public double calculateIncomeByAllEducationLevels(){
		return calculateValue("all", Factory.getYearlyImpl());
	}
	
	/**
	 * Calculates average weekly wage for all education levels
	 * <p>
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 * 
	 */
	public double calculateWeeklyWageByAllEducationLevels(){
		return calculateValue("all", Factory.getWeeklyImpl());
	}
	
	@TaDaMethod(variablesToTrack ={"ssn1", "education1", "weekWage", "workWeeks", "ssn2"}, 
			correspondingDatabaseAttribute = {"education.ssn", "education.education", "job.weekwage", "job.workweeks", "job.ssn"})
	private double calculateValue(String educationIn, CalcImplInterface type){
		HashMap<Integer, String> ssnEducation = new HashMap<Integer, String>();
		ArrayList<TypeWageDataStructureInterface> educationWageAL = new ArrayList<TypeWageDataStructureInterface>();
		
		int ssn1, ssn2;
		String education1, education2;
		int weekWage;
		int workWeeks;
		double sum = 0;
		double count = 0;
		double returnValue = 0;
		
		if(educationIn == null){
			educationIn = "";
		} else {
			educationIn = educationIn.trim();
		}
		
		
		try{
			statement = Factory.getConnection().createStatement();
	    	results = statement.executeQuery("SELECT SSN, EDUCATION from education");
	    	while(results.next()){
	    		if(results.getString("SSN") != null && results.getString("EDUCATION") != null){
	    			ssn1 = results.getInt("SSN");
		    		education1 = results.getString("EDUCATION").trim();
		    		ssnEducation.put(ssn1, education1);
	    		}
	    	}
	    	
	    	results = statement.executeQuery("SELECT SSN, WEEKWAGE, WORKWEEKS from job");
	    	while(results.next()){
	    		
	    		weekWage = results.getInt("WEEKWAGE");
	    		workWeeks = results.getInt("WORKWEEKS");
	    		ssn2 = results.getInt("SSN");
	    		education2 = (String) ssnEducation.get(ssn2);
	    		
	    		type.addToArrayList(educationWageAL, education2, weekWage, workWeeks);
	    		
	    	}
	    	
	    	for(TypeWageDataStructureInterface i : educationWageAL){
	    		double result = type.calculateAverage(educationIn, i);
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

		} catch(SQLException e) {
			while (e != null) {
				System.err.println("\n----- SQLException -----");
				System.err.println("  SQL State:  " + e.getSQLState());
				System.err.println("  Error Code: " + e.getErrorCode());
				System.err.println("  Message:    " + e.getMessage());
				// for stack traces, refer to derby.log or uncomment this:
				// e.printStackTrace(System.err);
				e = e.getNextException();
			}
		}

	    return Factory.getRoundMethod(returnValue);
	}
	
}
