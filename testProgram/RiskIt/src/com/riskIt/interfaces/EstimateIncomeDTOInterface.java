package com.riskIt.interfaces;

/**
 * EstimateIncomeDTOInterface.java
 * Purpose: Interface interface used for transferring data used
 * 	in the estimate income requirement.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public interface EstimateIncomeDTOInterface {
	
	public String getName();
	
	public int getSSN();
	
	public String getRace();
	
	public String getEducation();
	
	public int getOccupationCode();
	
	public int getIndustryCode();
	
	public int getWeeklyWage();
	
	public int getWorkWeeks();
	
	public int getInvestmentIncome();
	
	public String toString();

}
