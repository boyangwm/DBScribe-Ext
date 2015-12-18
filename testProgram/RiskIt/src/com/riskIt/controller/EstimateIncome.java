package com.riskIt.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import tada.TaDaMethod;

import com.riskIt.data.CalculateRegressionDataStructure;
import com.riskIt.interfaces.EstimateIncomeDTOInterface;
import com.riskIt.interfaces.EstimateIncomeInterface;
import com.riskIt.util.Factory;



/**
 * EstimateIncome.java
 * Purpose: Calculation class to estimate income based on weekly wage, work 
 * 	weeks or investment income for requirement 5.
 * 
 * 	Uses CalculateRegression class to calculate the linear regression values.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

/**
 * Object to perform all data gathering functions necessary to obtain
 * 	the data to be passed to the CalculateRegression object that will
 * 	compute the linear regression formula in the form of y = m(x) + b
 *  based on the data.
 * 
 * Linear regression is performed once when the class is first instantiated,
 * 	then the computed slope and intercept values for the entire data set, along
 * 	with the middle 2/3 of the data set is held in CalculateRegressionDataStructure
 * 	data transfer objects for quick calculations.
 */
public class EstimateIncome implements EstimateIncomeInterface{

	private Statement statement;
	private ResultSet results;

	private CalculateRegressionDataStructure workWeeksFindIncome;
	private CalculateRegressionDataStructure weeklyWageFindIncome;
	private CalculateRegressionDataStructure investmentIncomeFindIncome;
	
	/**
	 * Public constructor that starts the regression analysis that will be
	 * 	performed only once per instantiation.
	 */
	public EstimateIncome(){
		calculateSlopesAndIntercepts();
	}
	
	// Gathers datat from the database and passes the formatted data to the
	//	CalculateRegression object that will compute the regression formulas.
	@TaDaMethod(variablesToTrack = {"ssn", "weekWage", "workWeeks", "ssn2",
			"capitalGains", "capitalLosses", "stockDividends"}, 
			correspondingDatabaseAttribute = {"job.ssn", "job.WEEKWAGE", "job.WORKWEEKS", "investment.ssn",
			"investment.CAPITALGAINS", "investment.CAPITALLOSSES", "investment.STOCKDIVIDENDS"})
	private void calculateSlopesAndIntercepts(){
		HashMap<Integer, Integer> ssnWeekWage = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> ssnWorkWeeks = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> ssnInvestmentIncome = new HashMap<Integer, Integer>();
		
		try {
			statement = Factory.getConnection().createStatement();
	        results = statement.executeQuery("SELECT SSN, WEEKWAGE, WORKWEEKS from job"); 
	        while(results.next()){
	        	if(results.getString("SSN") == null || results.getString("WEEKWAGE") == null || results.getString("WORKWEEKS") == null){
	        		continue;
	        	}
	        	int ssn = results.getInt("SSN");
	        	int weekWage = results.getInt("WEEKWAGE");
	        	int workWeeks = results.getInt("WORKWEEKS");
	        	
	        	ssnWeekWage.put(ssn, weekWage);
	        	ssnWorkWeeks.put(ssn, workWeeks);
	        }
	        
	        results = statement.executeQuery("SELECT SSN, CAPITALGAINS, CAPITALLOSSES, STOCKDIVIDENDS from investment");
	        while(results.next()){
	        	if(results.getString("SSN") == null || results.getString("CAPITALGAINS") == null || results.getString("CAPITALLOSSES") == null || results.getString("STOCKDIVIDENDS") == null){
	        		continue;
	        	}
	        	int ssn2 = results.getInt("SSN");
	        	int capitalGains = results.getInt("CAPITALGAINS");
	        	int capitalLosses = results.getInt("CAPITALLOSSES");
	        	int stockDividends = results.getInt("STOCKDIVIDENDS");
	        	int investmentIncome = capitalGains + stockDividends + capitalLosses;
	        	
	        	ssnInvestmentIncome.put(ssn2, investmentIncome);
	        }
	        
	        // Calculate Cofficients
	        // Build an array list of Work Weeks and an array list of Income;
	        ArrayList<Integer> workWeeksList = new ArrayList<Integer>();
	        ArrayList<Integer> incomeList = new ArrayList<Integer>();
	        ArrayList<Integer> weeklyWageList = new ArrayList<Integer>();
	        ArrayList<Integer> investmentList = new ArrayList<Integer>();
	        ArrayList<Integer> incomeListForInvestment = new ArrayList<Integer>();
	        
	        for (Iterator<Integer> i = ssnWorkWeeks.keySet().iterator(); i.hasNext();){
	        	int SSNkey = (Integer) i.next();
	        	if(ssnWorkWeeks.get(SSNkey) != null && ssnWeekWage.get(SSNkey) != null){
		        	int workWeeks = (Integer) ssnWorkWeeks.get(SSNkey);
		        	int weekWage = (Integer) ssnWeekWage.get(SSNkey);
		        	if(workWeeks > 0 && weekWage > 0){
		        		workWeeksList.add(workWeeks);
			        	incomeList.add(workWeeks * weekWage);
			        	weeklyWageList.add(weekWage);
			        	if(ssnInvestmentIncome.get(SSNkey) != null && ssnInvestmentIncome.get(SSNkey) != null){
			        		int investmentIncome = (Integer) ssnInvestmentIncome.get(SSNkey);
			        		investmentList.add(investmentIncome);
			        		incomeListForInvestment.add(workWeeks * weekWage);
			        	}
		        	}
	        	}
	        }
	        
	        // Set the member variable for the cofficients for given Work Weeks find Income
	        workWeeksFindIncome = new CalculateRegression().calculateRegressionNumbers(workWeeksList,incomeList);
	        
	     // Set the member variable for the cofficients for given Weekly Wage find Income
	        weeklyWageFindIncome = new CalculateRegression().calculateRegressionNumbers(weeklyWageList,incomeList);
	        
	     // Set the member variable for the cofficients for given InvestmentIncome find Income  
	        investmentIncomeFindIncome = new CalculateRegression().calculateRegressionNumbers(investmentList,incomeListForInvestment);
	        
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
		
	}
	
	/**
	 * Gathers information from the database based on a SSN and places that data in a data transfer
	 * 	object EstimateIncomeDTO to bu used by the estimate income requirement / function.
	 * <p>
	 * @param 				A int SSN number
	 * @return				A EstimateIncomeDTO that contains values to be used in the estimate 
	 * 						 income requirement.
	 */	
	@TaDaMethod(variablesToTrack = {"name", "race", "ssn2", "ssn3",
			"investmentIncome", "investmentIncome", "investmentIncome",
			"education", "ssn4",  "occupationCode",
			"industryCode", "weeklyWage", "workWeeks"}, 
			correspondingDatabaseAttribute = {"userrecord.name", "userrecord.race", "education.ssn", "investment.ssn",
			"investment.CAPITALGAINS", "investment.CAPITALLOSSES", "investment.STOCKDIVIDENDS",
			"education.education", "job.ssn", "job.INDUSTRYCODE", 
			"job.OCCUPATIONCODE", "job.WEEKWAGE", "job.workweeks"})
	public EstimateIncomeDTOInterface getValues(int ssn){
		
		ResultSet results;
		Statement statement;
		
		String ssnString = Integer.toString(ssn);
		
		String name = null;
		String race = null;
		String education = null;
		int occupationCode = 0;
		int industryCode = 0;
		int weeklyWage = 0;
		int workWeeks = 0;
		int investmentIncome = 0;
		
		try{
			statement = Factory.getConnection().createStatement();
	    	results = statement.executeQuery("SELECT SSN, NAME, RACE from userrecord WHERE SSN = " + ssnString +"");
	    	while(results.next()){
	    		if(results.getInt("SSN") == 0){
	    			continue;
	    		} else {
	    			name = results.getString("NAME");
	    			race = results.getString("RACE");
	    		}
	    	}
	    	
	    	results = statement.executeQuery("SELECT SSN, EDUCATION from education WHERE SSN = " + ssnString +"");
	    	while(results.next()){
	    		int ssn2 = results.getInt("SSN");
	    		if(ssn2 == 0){
	    			continue;
	    		} else {
	    			education = results.getString("EDUCATION");
	    		}
	    	}
	    	
	    	results = statement.executeQuery("SELECT SSN, CAPITALGAINS, CAPITALLOSSES, STOCKDIVIDENDS from investment WHERE SSN = " + ssnString +"");
	    	while(results.next()){
	    		int ssn3 = results.getInt("SSN");
	    		if(ssn3 == 0){
	    			continue;
	    		} else {
	    			investmentIncome = results.getInt("CAPITALGAINS") - results.getInt("CAPITALLOSSES") + results.getInt("STOCKDIVIDENDS");
	    		}
	    	}
	    	
	    	results = statement.executeQuery("SELECT SSN, INDUSTRYCODE, OCCUPATIONCODE, WEEKWAGE, WORKWEEKS from job WHERE SSN = " + ssnString +"");
	    	while(results.next()){
	    		int ssn4 = results.getInt("SSN");
	    		if(ssn4 == 0){
	    			continue;
	    		} else {
	    		occupationCode = results.getInt("INDUSTRYCODE");
	    		industryCode = results.getInt("OCCUPATIONCODE");
	    		weeklyWage = results.getInt("WEEKWAGE");
	    		workWeeks = results.getInt("WORKWEEKS");
	    		}
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
			
    	return Factory.getEstimateIncomeDTO(name, ssn, race, education,
    			occupationCode, industryCode, weeklyWage, workWeeks, investmentIncome);
    	
	}
	
	/**
	 * Estimates annual income based on a given work week value using all the
	 * 	data contained in the database.
	 * <p>
	 * @param 				An integer for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenWorkWeeksEstimateIncomeAll(int workWeeks){
	    return formatReturnValue((workWeeks * givenWorkWeeksEstimateIncomeGetSlopeAll()) + givenWorkWeeksEstimateIncomeGetInterceptAll());
	}
	
	/**
	 * Estimates annual income based on a given work week value using all the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A double for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenWorkWeeksEstimateIncomeAll(double workWeeks){
	    return formatReturnValue((workWeeks * givenWorkWeeksEstimateIncomeGetSlopeAll()) + givenWorkWeeksEstimateIncomeGetInterceptAll());
	}
	
	/**
	 * Estimates annual income based on a given work week value using the middle 2/3 the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A integer for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenWorkWeeksEstimateIncomeMiddleTwoThirds(int workWeeks){
	    return formatReturnValue((workWeeks * givenWorkWeeksEstimateIncomeGetSlopeMiddleTwoThirds()) + givenWorkWeeksEstimateIncomeGetInterceptMiddleTwoThirds());
	}
	
	/**
	 * Estimates annual income based on a given work week value using the middle 2/3 the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A double for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenWorkWeeksEstimateIncomeMiddleTwoThirds(double workWeeks){
		return formatReturnValue((workWeeks * givenWorkWeeksEstimateIncomeGetSlopeMiddleTwoThirds()) + givenWorkWeeksEstimateIncomeGetInterceptMiddleTwoThirds());
	}
	
	/**
	 * For work week entry [x] returns the slope [m] for linear equation y= m(x) + b
	 *  using all the data contained in the database.
	 * <p>
	 * @return				Double - Slope [m] of linear equation
	 */
	public double givenWorkWeeksEstimateIncomeGetSlopeAll(){
	    return formatReturnValue(workWeeksFindIncome.getSlopeAll());
	}
	
	/**
	 * For work week entry [x] returns the intercept [b] for linear equation y= m(x) + b
	 *  using all the data contained in the database.
	 * <p>
	 * @return				Double - Intercept [b] of linear equation
	 */
	public double givenWorkWeeksEstimateIncomeGetInterceptAll(){
	    return formatReturnValue(workWeeksFindIncome.getInterceptAll());
	}
	
	/**
	 * For work week entry [x] returns the slope [m] for linear equation y= m(x) + b
	 *  using the middle 2/3 of the data contained in the database.
	 * <p>
	 * @return				Double - Slope [m] of linear equation
	 */
	public double givenWorkWeeksEstimateIncomeGetSlopeMiddleTwoThirds(){
	    return formatReturnValue(workWeeksFindIncome.getSlopeMiddleTwoThirds());
	}

	/**
	 * For work week entry [x] returns the intercept [b] for linear equation y= m(x) + b
	 *  using the middle 2/3 of the data contained in the database.
	 * <p>
	 * @return				Double - Intercept [b] of linear equation
	 */
	public double givenWorkWeeksEstimateIncomeGetInterceptMiddleTwoThirds(){
	    return formatReturnValue(workWeeksFindIncome.getInterceptMiddleTwoThirds());
	}
	
	/**
	 * Estimates annual income based on a given weekly wage value using all the
	 * 	data contained in the database.
	 * <p>
	 * @param 				An integer for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenWeeklyWageEstimateIncomeAll(int weeklyWage){
	    return formatReturnValue((weeklyWage * givenWeeklyWageEstimateIncomeGetSlopeAll()) + givenWeeklyWageEstimateIncomeGetInterceptAll());
	}
	
	/**
	 * Estimates annual income based on a given weekly wage value using all the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A double for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenWeeklyWageEstimateIncomeAll(double weeklyWage){
	    return formatReturnValue((weeklyWage * givenWeeklyWageEstimateIncomeGetSlopeAll()) + givenWeeklyWageEstimateIncomeGetInterceptAll());
	}
	
	/**
	 * Estimates annual income based on a given weekly wage value using the middle 2/3 the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A integer for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenWeeklyWageEstimateIncomeMiddleTwoThirds(int weeklyWage){
	    return formatReturnValue((weeklyWage * givenWeeklyWageEstimateIncomeGetSlopeMiddleTwoThirds()) + givenWeeklyWageEstimateIncomeGetInterceptMiddleTwoThirds());
	}
	
	/**
	 * Estimates annual income based on a given weekly wage value using the middle 2/3 the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A double for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenWeeklyWageEstimateIncomeMiddleTwoThirds(double weeklyWage){
		return formatReturnValue((weeklyWage * givenWeeklyWageEstimateIncomeGetSlopeMiddleTwoThirds()) + givenWeeklyWageEstimateIncomeGetInterceptMiddleTwoThirds());
	}
	
	/**
	 * For weekly wage entry [x] returns the slope [m] for linear equation y= m(x) + b
	 *  using all the data contained in the database.
	 * <p>
	 * @return				Double - Slope [m] of linear equation
	 */
	public double givenWeeklyWageEstimateIncomeGetSlopeAll(){
	    return formatReturnValue(weeklyWageFindIncome.getSlopeAll());
	}
	
	/**
	 * For weekly wage entry [x] returns the intercept [b] for linear equation y= m(x) + b
	 *  using all the data contained in the database.
	 * <p>
	 * @return				Double - Intercept [b] of linear equation
	 */
	public double givenWeeklyWageEstimateIncomeGetInterceptAll(){
	    return formatReturnValue(weeklyWageFindIncome.getInterceptAll());
	}
	
	/**
	 * For weekly wage entry [x] returns the slope [m] for linear equation y= m(x) + b
	 *  using the middle 2/3 of the data contained in the database.
	 * <p>
	 * @return				Double - Slope [m] of linear equation
	 */
	public double givenWeeklyWageEstimateIncomeGetSlopeMiddleTwoThirds(){
		return formatReturnValue(weeklyWageFindIncome.getSlopeMiddleTwoThirds());
	}
	
	/**
	 * For weekly wage entry [x] returns the intercept [b] for linear equation y= m(x) + b
	 *  using the middle 2/3 of the data contained in the database.
	 * <p>
	 * @return				Double - Intercept [b] of linear equation
	 */
	public double givenWeeklyWageEstimateIncomeGetInterceptMiddleTwoThirds(){
	    return formatReturnValue(weeklyWageFindIncome.getInterceptMiddleTwoThirds());
	}
	
	/**
	 * Estimates annual income based on a given investment income value using all the
	 * 	data contained in the database.
	 * <p>
	 * @param 				An integer for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenInvestmentIncomeEstimateIncomeAll(int investmentIncome){
		return formatReturnValue((investmentIncome * givenInvestmentIncomeEstimateIncomeGetSlopeAll()) + givenInvestmentIncomeEstimateIncomeGetInterceptAll());
	}
	
	/**
	 * Estimates annual income based on a given investment income value using all the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A double for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenInvestmentIncomeEstimateIncomeAll(double investmentIncome){
		return formatReturnValue((investmentIncome * givenInvestmentIncomeEstimateIncomeGetSlopeAll()) + givenInvestmentIncomeEstimateIncomeGetInterceptAll());
	}
	
	/**
	 * Estimates annual income based on a given investment income value using the middle 2/3 the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A integer for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenInvestmentIncomeEstimateIncomeMiddleTwoThirds(int investmentIncome){
	    return formatReturnValue((investmentIncome * givenInvestmentIncomeEstimateIncomeGetSlopeMiddleTwoThirds()) + givenInvestmentIncomeEstimateIncomeGetInterceptMiddleTwoThirds());
	}
	
	/**
	 * Estimates annual income based on a given investment income value using the middle 2/3 the
	 * 	data contained in the database.
	 * <p>
	 * @param 				A double for number of work weeks.
	 * 							Note, input is not checked for reasonableness
	 * 							Any input value can be calculated
	 * @return				Double - Estimated annual income.
	 */
	public double givenInvestmentIncomeEstimateIncomeMiddleTwoThirds(double investmentIncome){
	    return formatReturnValue((investmentIncome * givenInvestmentIncomeEstimateIncomeGetSlopeMiddleTwoThirds()) + givenInvestmentIncomeEstimateIncomeGetInterceptMiddleTwoThirds());
	}
	
	/**
	 * For investment income entry [x] returns the slope [m] for linear equation y= m(x) + b
	 *  using all the data contained in the database.
	 * <p>
	 * @return				Double - Slope [m] of linear equation
	 */
	public double givenInvestmentIncomeEstimateIncomeGetSlopeAll(){
	    return formatReturnValue(investmentIncomeFindIncome.getSlopeAll());
	}
	
	/**
	 * For investment income entry [x] returns the intercept [b] for linear equation y= m(x) + b
	 *  using all the data contained in the database.
	 * <p>
	 * @return				Double - Intercept [b] of linear equation
	 */
	public double givenInvestmentIncomeEstimateIncomeGetInterceptAll(){
		return formatReturnValue(investmentIncomeFindIncome.getInterceptAll());
	}
	
	/**
	 * For investment income entry [x] returns the slope [m] for linear equation y= m(x) + b
	 *  using the middle 2/3 of the data contained in the database.
	 * <p>
	 * @return				Double - Slope [m] of linear equation
	 */
	public double givenInvestmentIncomeEstimateIncomeGetSlopeMiddleTwoThirds(){
		return formatReturnValue(investmentIncomeFindIncome.getSlopeMiddleTwoThirds());
	}
	
	/**
	 * For investment income entry [x] returns the intercept [b] for linear equation y= m(x) + b
	 *  using the middle 2/3 of the data contained in the database.
	 * <p>
	 * @return				Double - Intercept [b] of linear equation
	 */
	public double givenInvestmentIncomeEstimateIncomeGetInterceptMiddleTwoThirds(){
		return formatReturnValue(investmentIncomeFindIncome.getInterceptMiddleTwoThirds());
	}
	
	private double formatReturnValue(double returnValue){
		return Factory.getRoundMethod(returnValue);
	}

}
