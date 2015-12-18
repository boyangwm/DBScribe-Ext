package com.riskIt.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * CalculateByOccupationOrIndustryCodeInterface.java
 * Purpose: Interface for CalculateByOccupationOrIndustryCode
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public interface CalculateByOccupationOrIndustryCodeInterface {

	public ArrayList<Integer> getOccupationCodeList();
	
	public ArrayList<Integer> getIndustryCodeList();
	
	public HashMap<Integer, String> getOccupationCodeAndNameMap();
	
	public HashMap<Integer, String> getIndustryCodeAndNameMap();
	
	public boolean isValidOccupationCode(int code);
	
	public boolean isValidIndustryCode(int code);
	
	public double calculateIncomeByOccupationCode (int code);
	
	public double calculateWeeklyWageByOccupationCode (int code);
	
	public double calculateIncomeByIndustryCode (int code);
	
	public double calculateWeeklyWageByIndustryCode (int code);

}
