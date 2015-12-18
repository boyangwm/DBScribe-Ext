package com.riskIt.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import tada.TaDaMethod;

import com.riskIt.interfaces.CalcImplWithDescriptionInterface;
import com.riskIt.interfaces.CalculateByOccupationOrIndustryCodeInterface;
import com.riskIt.interfaces.TypeWageDataStructureInterface;
import com.riskIt.util.Factory;


/**
 * CalculateByOccupationOrIndustryCode.java
 * Purpose: Calculation class for calculate average income and average
 * 	wage for requirements 4 and 7.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

/**
 * Object to perform all calculations necessary to calculate
 * 	average income and average weekly wage by all or by a given
 * 	industry or occupation code contained in the database.  Uses Hash
 *  Maps and offloads processing to the JVM to overcome inherent
 *  inefficiencies in the Derby database thus significantly improving
 *  performance.
 */
public class CalculateByOccupationOrIndustryCode implements CalculateByOccupationOrIndustryCodeInterface{

    Statement statement;
	ResultSet results;

	/**
	 * Returns a list with all the occupation codes contained in the database.
	 * <p>
	 * @return				An ArrayList<Integer> of all occupation codes.
	 */
	@TaDaMethod(variablesToTrack ={"occupationCodeList"},
			correspondingDatabaseAttribute = {"occupation.OCCUPATIONCODE"})
	public ArrayList<Integer> getOccupationCodeList() {
	    ArrayList<Integer> occupationCodeList = new ArrayList<Integer>();
		try {
			statement = Factory.getConnection().createStatement();
	        results = statement.executeQuery("SELECT DISTINCT OCCUPATIONCODE from occupation");
	        while(results.next()){
	        	if(results.getString("OCCUPATIONCODE") != null){
	        		occupationCodeList.add(results.getInt("OCCUPATIONCODE"));
	        	}
	        	
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
		
        Collections.sort(occupationCodeList);
		return occupationCodeList;
	}
	
	/**
	 * Checks if a occupation code is contained in the database
	 * <p>
	 * @param 				A integer of a occupation code to test
	 * @return				Boolean true / false answer
	 */
	public boolean isValidOccupationCode(int code) {

		ArrayList<Integer> occupationCodeList = getOccupationCodeList();
		
		for(int i: occupationCodeList){
			if (i == code){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a hash map matching the occupation codes with their string descriptions
	 * <p>
	 * @return				A hashMap <Integer, String> of occupation codes and their
	 * 							associated string descriptions.
	 */
	@TaDaMethod(variablesToTrack ={"occupation", "occupationCodeAndNameMap"},
			correspondingDatabaseAttribute = {"occupation.occupation", "occupation.OCCUPATIONCODE"})
	public HashMap<Integer, String> getOccupationCodeAndNameMap() {
		HashMap<Integer, String> occupationCodeAndNameMap = new HashMap<Integer, String>();
		
		try {
			statement = Factory.getConnection().createStatement();
	        results = statement.executeQuery("SELECT OCCUPATIONCODE, OCCUPATION from occupation"); 
	        while(results.next()){
	        	if(results.getString("OCCUPATIONCODE") == null){
	        		continue;
	        	}
	        	
	        	String occupation = results.getString("OCCUPATION");
	        	
	        	if(occupation != null){
	        		occupation = occupation.trim();
	        	} else {
	        		occupation = "None Entered";
	        	}
	        	
	        	if(results.getString("OCCUPATIONCODE") != null){
	        		occupationCodeAndNameMap.put(results.getInt("OCCUPATIONCODE"), occupation);
	        	}
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
		
		return occupationCodeAndNameMap;
	}
	
	/**
	 * Returns a list with all the industry codes contained in the database.
	 * <p>
	 * @return				An ArrayList<Integer> of all industry codes.
	 */
	@TaDaMethod(variablesToTrack ={"industryCodeList"},
			correspondingDatabaseAttribute = {"industry.INDUSTRYCODE"})
	public ArrayList<Integer> getIndustryCodeList() {
		ArrayList<Integer> industryCodeList = new ArrayList<Integer>();
		try {
			statement = Factory.getConnection().createStatement();  
	        results = statement.executeQuery("SELECT DISTINCT INDUSTRYCODE from industry");
	        while(results.next()){
	        	if(results.getString("INDUSTRYCODE") != null){
	        		industryCodeList.add(results.getInt("INDUSTRYCODE"));
	        	}
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
		
        Collections.sort(industryCodeList);
		return industryCodeList;
	}
	
	/**
	 * Checks if an industry code is contained in the database
	 * <p>
	 * @param 				A integer of an industry code to test
	 * @return				Boolean true / false answer
	 */
	public boolean isValidIndustryCode(int code) {
		ArrayList<Integer> industryCodeList = getIndustryCodeList();
		
		for(int i: industryCodeList){
			if (i == code){
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * Returns a hash map matching the industry codes with their string descriptions
	 * <p>
	 * @return				A hashMap <Integer, String> of industry codes and their
	 * 							associated string descriptions.
	 */
	@TaDaMethod(variablesToTrack ={"IndustryCodeAndNameMap", "industry"},
			correspondingDatabaseAttribute = {"industry.INDUSTRYCODE", "industry.industry"})
	public HashMap<Integer, String> getIndustryCodeAndNameMap() {
		HashMap<Integer, String> IndustryCodeAndNameMap = new HashMap<Integer, String>();
		
		try {
			statement = Factory.getConnection().createStatement();
	        results = statement.executeQuery("SELECT INDUSTRYCODE, INDUSTRY from industry"); 
	        while(results.next()){
	        	if(results.getString("INDUSTRYCODE") == null){
	        		continue;
	        	}
	        	
	        	String industry = results.getString("INDUSTRY");
	        	if(industry == null){
	        		industry = "None Entered";
	        	} else {
	        		industry = industry.trim();
	        	}
	        	
	        	if(results.getString("INDUSTRYCODE") != null){
	        		IndustryCodeAndNameMap.put(results.getInt("INDUSTRYCODE"), industry);
	        	}
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
		
		return IndustryCodeAndNameMap;
	}

	/**
	 * Calculates average income for a given occupation code.
	 * <p>
	 * @param 				An integer of an occupation level to request
	 * 						Warning: This method does not check if the 
	 * 							occupation integer submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the integer is contained
	 * 							in the db use the method :
	 * 							isValidOccupationCode(int)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 */
	public double calculateIncomeByOccupationCode(int code){
		return calculateResult(code, Factory.getYearlyImplWithDescription("OCCUPATIONCODE"));
	}
	
	/**
	 * Calculates average income for a given industry code
	 * <p>
	 * @param 				An integer of an industry code to request
	 * 						Warning: This method does not check if the 
	 * 							industry integer submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the integer is contained
	 * 							in the db use the method :
	 * 							isValidIndustryCode(int)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 */
	public double calculateIncomeByIndustryCode(int code) {
		return calculateResult(code, Factory.getYearlyImplWithDescription("INDUSTRYCODE"));
	}
	
	/**
	 * Calculates weekly wage for a given occupation code.
	 * <p>
	 * @param 				An integer of an occupation level to request
	 * 						Warning: This method does not check if the 
	 * 							occupation integer submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the integer is contained
	 * 							in the db use the method :
	 * 							isValidOccupationCode(int)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 * 
	 */
	public double calculateWeeklyWageByOccupationCode(int code) {
		return calculateResult(code, Factory.getWeeklyImplWithDescription("OCCUPATIONCODE"));
	}
	
	/**
	 * Calculates average weekly wage for a given industry code
	 * <p>
	 * @param 				An integer of an industry code to request
	 * 						Warning: This method does not check if the 
	 * 							industry integer submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the integer is contained
	 * 							in the db use the method :
	 * 							isValidIndustryCode(int)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 */
	public double calculateWeeklyWageByIndustryCode(int code) {
		return calculateResult(code, Factory.getWeeklyImplWithDescription("INDUSTRYCODE"));
	}
	
	@TaDaMethod(variablesToTrack ={"type", "type"},
			correspondingDatabaseAttribute = {"job.WEEKWAGE", "job.WORKWEEKS"})
	private double calculateResult(int code, CalcImplWithDescriptionInterface type) {
		ArrayList<TypeWageDataStructureInterface> dataAL = new ArrayList<TypeWageDataStructureInterface>();
		double sum = 0;
        double count = 0;
        double returnValue = 0;
		try {
			statement = Factory.getConnection().createStatement();
	        results = statement.executeQuery("SELECT " + type.getDescription() + ", WEEKWAGE, WORKWEEKS from job");
            while(results.next()){
            	type.addToArrayList(dataAL, results.getString(type.getDescription()), results.getInt("WEEKWAGE"), results.getInt("WORKWEEKS"));
            }
	        statement.close();
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
		
		for(TypeWageDataStructureInterface i : dataAL){
    		double result = type.calculateAverage(Integer.toString(code), i);
			if(result > 0){
				sum = sum + result;
				count++;
			}
		}
		
        if (count == 0) {
        	returnValue = 0;
        }
        else {
        	returnValue = sum / count;
        }

        return Factory.getRoundMethod(returnValue);
	}

}
