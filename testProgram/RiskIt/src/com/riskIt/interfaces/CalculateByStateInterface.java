package com.riskIt.interfaces;

import java.util.ArrayList;

/**
 * CalculateByStateInterface.java
 * Purpose: Interface for CalculateByState Class
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public interface CalculateByStateInterface {
	
	public ArrayList<String> getStateList();
	
	public boolean isInStateList(String state);
	
	public double calculateIncomeByState(String state);
	
	public double calculateWeeklyWageByState(String state);
	
	public double calculateIncomeByAllStates();
	
	public double calculagteWeeklyWageByAllState();

}
