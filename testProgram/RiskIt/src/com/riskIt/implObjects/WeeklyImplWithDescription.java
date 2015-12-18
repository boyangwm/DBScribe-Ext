package com.riskIt.implObjects;

import java.util.ArrayList;

import com.riskIt.interfaces.CalcImplWithDescriptionInterface;
import com.riskIt.interfaces.TypeWageDataStructureInterface;
import com.riskIt.util.Factory;

/**
 * WeeklyImplWithDescription.java
 * Purpose: Implementation class to allow calculation class
 * 	to introduce late binding and state to calculate values
 * 	based on Weekly Wage for a given value in the database.
 * 	Specialization needed to deal with occupation and industry
 * 	code values.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class WeeklyImplWithDescription implements CalcImplWithDescriptionInterface {

	private final String description;
	
	public WeeklyImplWithDescription(String description){
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void addToArrayList(ArrayList<TypeWageDataStructureInterface> dataAL, String code, int weekWage, int workWeeks) {
		if(weekWage > 0){
			dataAL.add(Factory.getTypeWageDataStructure(code, weekWage,workWeeks));
		}
	}

	public double calculateAverage(String code, TypeWageDataStructureInterface i) {
		if(code == null){
			return 0;
		} else if (i.getType() != null && code != null && i.getType().trim().equalsIgnoreCase(code)){
			return i.getWeekWage();
		} else {
			return 0;
		}
	}

}
