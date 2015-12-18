package com.riskIt.interfaces;

import java.util.ArrayList;

/**
 * CalculateByRaceInterface.java
 * Purpose: Interface for CalculateByRace Class
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public interface CalculateByRaceInterface {
	
	public ArrayList<String> getRaceList();
	
	public boolean isInRaceList(String race);
	
	public double calculateWeeklyWageByRace(String race);
	
	public double calculateWeelkyWageByAllRaces();
	
	public double calculateIncomeByRace(String race);
	
	public double calculateIncomeByAllRaces();

}
