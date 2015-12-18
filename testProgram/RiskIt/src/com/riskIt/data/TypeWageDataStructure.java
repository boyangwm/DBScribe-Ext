package com.riskIt.data;

import com.riskIt.interfaces.TypeWageDataStructureInterface;

/**
 * EducationWageDataStructure.java
 * Purpose: Data Structure to hold final list of education level, weekWage and
 * 	workWeeks for CalculateByEducation.java
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class TypeWageDataStructure implements TypeWageDataStructureInterface{
	
	private final String type;
	private final double weekWage;
	private final double workWeeks;
	
	public TypeWageDataStructure(String type, double weekWage, double workWeeks){
		this.type = type;
		this.weekWage = weekWage;
		this.workWeeks = workWeeks;
	}
	
	public String getType(){
		return type;
	}
	
	public double getWeekWage(){
		return weekWage;
	}
	
	public double getWorkWeeks(){
		return workWeeks;
	}

}
