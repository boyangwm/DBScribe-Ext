package com.riskIt.interfaces;

/**
 * EstimateIncomeInterface.java
 * Purpose: Interface for EstimateIncome Class
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public interface EstimateIncomeInterface {
	
	public EstimateIncomeDTOInterface getValues(int ssn);
	
	public double givenWorkWeeksEstimateIncomeAll(int workWeeks);
	
	public double givenWorkWeeksEstimateIncomeAll(double workWeeks);
	
	public double givenWorkWeeksEstimateIncomeMiddleTwoThirds(int workWeeks);
	
	public double givenWorkWeeksEstimateIncomeMiddleTwoThirds(double workWeeks);
	
	public double givenWorkWeeksEstimateIncomeGetSlopeAll();
	
	public double givenWorkWeeksEstimateIncomeGetInterceptAll();
	
	public double givenWorkWeeksEstimateIncomeGetSlopeMiddleTwoThirds();
	
	public double givenWorkWeeksEstimateIncomeGetInterceptMiddleTwoThirds();
	
	public double givenWeeklyWageEstimateIncomeAll(int weeklyWage);
	
	public double givenWeeklyWageEstimateIncomeAll(double weeklyWage);
	
	public double givenWeeklyWageEstimateIncomeMiddleTwoThirds(int weeklyWage);
	
	public double givenWeeklyWageEstimateIncomeMiddleTwoThirds(double weeklyWage);
	
	public double givenWeeklyWageEstimateIncomeGetSlopeAll();
	
	public double givenWeeklyWageEstimateIncomeGetInterceptAll();
	
	public double givenWeeklyWageEstimateIncomeGetSlopeMiddleTwoThirds();
	
	public double givenWeeklyWageEstimateIncomeGetInterceptMiddleTwoThirds();
	
	public double givenInvestmentIncomeEstimateIncomeAll(int investmentIncome);
	
	public double givenInvestmentIncomeEstimateIncomeAll(double investmentIncome);
	
	public double givenInvestmentIncomeEstimateIncomeMiddleTwoThirds(int investmentIncome);
	
	public double givenInvestmentIncomeEstimateIncomeMiddleTwoThirds(double investmentIncome);
	
	public double givenInvestmentIncomeEstimateIncomeGetSlopeAll();
	
	public double givenInvestmentIncomeEstimateIncomeGetInterceptAll();
	
	public double givenInvestmentIncomeEstimateIncomeGetSlopeMiddleTwoThirds();
	
	public double givenInvestmentIncomeEstimateIncomeGetInterceptMiddleTwoThirds();

}
