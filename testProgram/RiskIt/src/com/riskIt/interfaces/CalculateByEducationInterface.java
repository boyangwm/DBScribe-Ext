package com.riskIt.interfaces;

import java.util.ArrayList;

/**
 * CalculateByEducationInterface.java
 * Purpose: Interface for CalculateByEducation class
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public interface CalculateByEducationInterface {
	
	public ArrayList<String> getEducationLevelList();
	
	public boolean isInEducationList(String education);
	
	public double calculateIncomeByEducationLevel(String education);
	
	public double calculateIncomeByAllEducationLevels();
	
	public double calculateWeeklyWageByEducationLevel(String education);
	
	public double calculateWeeklyWageByAllEducationLevels();

}
