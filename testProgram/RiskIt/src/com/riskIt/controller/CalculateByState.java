package com.riskIt.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import tada.TaDaMethod;

import com.riskIt.interfaces.CalculateByStateInterface;
import com.riskIt.interfaces.CalcImplInterface;
import com.riskIt.interfaces.TypeWageDataStructureInterface;
import com.riskIt.util.Factory;


/**
 * CalculateByState.java
 * Purpose: Calculation Class to calculate average weekly wage and average
 * 	income by State.  Used for requirements 4 and 6.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

/**
 * Object to perform all calculations necessary to calculate
 * 	average income and average weekly wage by all or by a given
 * 	race state contained in the database. Uses Hash Maps and
 *  offloads processing to the JVM to overcome inherent
 *  inefficiencies in the Derby database thus significantly improving
 *  performance.
 */
public class CalculateByState implements CalculateByStateInterface {

	ResultSet results;
	Statement statement;

	/**
	 * Returns a list with all the state codes contained in the database.
	 * <p>
	 * @return				An ArrayList<String> of all state codes.
	 */
	@TaDaMethod(variablesToTrack ={"stateList"},
			correspondingDatabaseAttribute = {"ziptable.statename"})
	public ArrayList<String> getStateList() {
		ArrayList<String> stateList = new ArrayList<String>();

		try {
			statement = Factory.getConnection().createStatement();
			results = statement.executeQuery("SELECT DISTINCT STATENAME FROM ziptable");
			while (results.next()) {
				if(results.getString("STATENAME") != null){
					stateList.add(results.getString(1).trim());
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

		Collections.sort(stateList);
		return stateList;
	}

	/**
	 * Checks if a state code is contained in the database
	 * <p>
	 * @param 				A String of a state code to test
	 * @return				Boolean true / false answer
	 */
	public boolean isInStateList(String state) {
		ArrayList<String> stateList = getStateList();
		
		if(state == null){
			state = "";
		} else {
			state = state.trim();
		}
		
		for (String i : stateList) {
			if (i.trim().equalsIgnoreCase(state)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates average income for a state code
	 * <p>
	 * @param 				A String of a state code to request
	 * 						Warning: This method does not check if the 
	 * 							state string submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the string is contained
	 * 							in the db use the method :
	 * 							isInStateList(String)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 */
	public double calculateIncomeByState(String state) {
		return calculateIncome(state, Factory.getYearlyImpl());
	}

	/**
	 * Calculates average weekly wage for a state code
	 * <p>
	 * @param 				A String of a state code to request
	 * 						Warning: This method does not check if the 
	 * 							state string submitted is contained in
	 * 							the database as this is an expensive call to
	 * 							the db.  To check if the string is contained
	 * 							in the db use the method :
	 * 							isInStateList(String)
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 */
	public double calculateWeeklyWageByState(String state) {
		return calculateIncome(state, Factory.getWeeklyImpl());
	}
	
	/**
	 * Calculates average income for all states in the database
	 * <p>
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 */
	public double calculateIncomeByAllStates() {;
		return calculateIncome("all", Factory.getYearlyImpl());
	}

	/**
	 * Calculates average weekly wage for all states in the database
	 * <p>
	 * @return				A Double rounded to two decimal places using the
	 * 							ROUND_HALF_EVEN method of the BigDecimal class.
	 */
	public double calculagteWeeklyWageByAllState() {
		return calculateIncome("all", Factory.getWeeklyImpl());
	}

	@TaDaMethod(variablesToTrack ={"ssnZipHM", "ssnZipHM", "zipStateHM", "zipStateHM",  "ssnWageHM", "ssnWageHM",
			"ssnWorkWeeks", "ssnWorkWeeks"},
			correspondingDatabaseAttribute = {"userrecord,zip", "userrecord.ssn", 
			"ziptable.zip", "ziptable.statename",
			"job.ssn", "job.WEEKWAGE",
			"job.ssn", "job.WORKWEEKS"})
	private double calculateIncome(String stateIn, CalcImplInterface type) {

		double returnValue = 0;
		HashMap<Integer, String> ssnZipHM = new HashMap<Integer, String>();
		HashMap<String, String> zipStateHM = new HashMap<String, String>();
		HashMap<Integer, Integer> ssnWageHM = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> ssnWorkWeeks = new HashMap<Integer, Integer>();
		String stateCode = null;
		int weekWage = 0;
		int workWeeks = 0;
		ArrayList<TypeWageDataStructureInterface> stateWageAL = new ArrayList<TypeWageDataStructureInterface>();
		double sum = 0;
		double count = 0;
		
		if(stateIn == null){
			stateIn = "bad_value_return_0";
		}else {
			stateIn = stateIn.trim();
		}

		try {
			statement = Factory.getConnection().createStatement();

			// Get the SSN and Zip from user record and put them in a hashMap  
			results = statement.executeQuery("SELECT SSN, ZIP from userrecord WHERE ZIP IS NOT NULL");
			while (results.next()) {
				ssnZipHM.put(results.getInt("SSN"), results.getString("ZIP"));
			}

			// Get the ZIP and State Code from Zip Table and put them in a hash
			// map
			results = statement.executeQuery("SELECT ZIP, STATENAME from ziptable");
			while (results.next()) {
				if(results.getString("ZIP") == null || results.getString("STATENAME") == null){
					continue;
				}
				zipStateHM.put(results.getString("ZIP"), results.getString("STATENAME"));
			}
			
			// Get the SSN and Wage from job table and put in a hash map
			results = statement.executeQuery("SELECT SSN, WEEKWAGE, WORKWEEKS from job");
			while (results.next()) {
				if(results.getInt("WEEKWAGE") > 0){
					ssnWageHM.put(results.getInt("SSN"), results.getInt("WEEKWAGE"));
				}
				if(results.getInt("WORKWEEKS") > 0){
					ssnWorkWeeks.put(results.getInt("SSN"), results.getInt("WORKWEEKS"));
				}
			}

			// Iterate through all the SSN's from main user record
			// First find the zip for that SSN
			for (Iterator<Integer> i = ssnZipHM.keySet().iterator(); i.hasNext();) {
				int keySSN = (Integer) i.next();
				String zip = (String) ssnZipHM.get(keySSN);
				// Then find the state code for that zip
				if (zip != null && !(zip.equals(""))) {
					stateCode = (String) zipStateHM.get(zip);
					if (stateCode != null) {
						stateCode = stateCode.trim();
					}
				} else {
					stateCode = null;
				}
				
				// Then find weekly wage from job table based on ssn
				// If Null or can not part to an int then Week Wage = 0;
				if (ssnWageHM.get(keySSN) == null) {
					weekWage = 0;
				} else {
					weekWage = (Integer) ssnWageHM.get(keySSN);
				}

				if (ssnWorkWeeks.get(keySSN) == null) {
					workWeeks = 0;
				} else {
					workWeeks = (Integer) ssnWorkWeeks.get(keySSN);
				}
				
				// Then build an new Array List inserting new StateWageClass
				// object
				
				type.addToArrayList(stateWageAL, stateCode, weekWage, workWeeks);
			}

			// Now go through the stateWageAL Array List and look for the state
			// selected or all states for ALL
			
			for (TypeWageDataStructureInterface i : stateWageAL) {
				double result = type.calculateAverage(stateIn, i);
				if(result > 0){
					sum = sum + result;
					count++;
				}
			}
			
			// Handle Divide by 0 for invalid state codes or states with no wage
			// data
			if (count == 0) {
				returnValue = 0;
			} else {
				returnValue = sum / count;
			}

		} catch (SQLException e) {
			{
				while (e != null) {
					System.err.println("\n----- SQLException -----");
					System.err.println("  SQL State:  " + e.getSQLState());
					System.err.println("  Error Code: " + e.getErrorCode());
					System.err.println("  Message:    " + e.getMessage());
					// for stack traces, refer to derby.log or uncomment this:
					// e.printStackTrace(System.err);
					e = e.getNextException();
				}
				throw new IllegalArgumentException();
			}
		}

		return Factory.getRoundMethod(returnValue);
	}
}
