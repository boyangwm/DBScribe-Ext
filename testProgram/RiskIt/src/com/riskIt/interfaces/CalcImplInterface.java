package com.riskIt.interfaces;

import java.util.ArrayList;

/**
 * CalcImplInterface.java
 * Purpose: Interface for implementation classes to
 * 	allow calculation classes to use dynamic binding 
 * 	and state to determine how they will run.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public interface CalcImplInterface {
	
	public void addToArrayList(ArrayList<TypeWageDataStructureInterface> typeWageAL, String code, int weekWage, int workWeeks);
	
	public double calculateAverage(String state, TypeWageDataStructureInterface i);
}
