package com.riskIt.util;

import java.sql.Connection;
import java.sql.SQLException;

import com.riskIt.controller.CalculateByEducation;
import com.riskIt.controller.CalculateByOccupationOrIndustryCode;
import com.riskIt.controller.CalculateByRace;
import com.riskIt.controller.CalculateByState;
import com.riskIt.controller.EstimateIncome;
import com.riskIt.data.EstimateIncomeDTO;
import com.riskIt.data.TypeWageDataStructure;
import com.riskIt.db.DatabaseConnection;
import com.riskIt.implObjects.WeeklyImpl;
import com.riskIt.implObjects.WeeklyImplWithDescription;
import com.riskIt.implObjects.YearlyImpl;
import com.riskIt.implObjects.YearlyImplWithDescription;
import com.riskIt.interfaces.CalcImplInterface;
import com.riskIt.interfaces.CalcImplWithDescriptionInterface;
import com.riskIt.interfaces.CalculateByEducationInterface;
import com.riskIt.interfaces.CalculateByOccupationOrIndustryCodeInterface;
import com.riskIt.interfaces.CalculateByRaceInterface;
import com.riskIt.interfaces.CalculateByStateInterface;
import com.riskIt.interfaces.EstimateIncomeDTOInterface;
import com.riskIt.interfaces.TypeWageDataStructureInterface;
import com.riskIt.interfaces.EstimateIncomeInterface;
import com.riskIt.interfaces.StartInterface;
import com.riskIt.ui.CalculateWeeklyWageUI;
import com.riskIt.ui.EstimateIncomeUI;
import com.riskIt.ui.IncomeByEducationUI;
import com.riskIt.ui.IncomeByOccupationOrIndustryCodeUI;
import com.riskIt.ui.IncomeByRaceUI;
import com.riskIt.ui.IncomeByStateUI;

/**
 * Factory.java
 * Purpose: Factory class that uses static factories to create and return
 * 	objects used in system thus allowing for decoupling.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class Factory {
	
	private Factory(){}
	
	public static Connection getConnection(){
		try {
			return DatabaseConnection.getConnection();
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
			return null;
		}
	}
	
	public static CalculateByRaceInterface getCalculateByRace(){
		return new CalculateByRace();
	}
	
	public static StartInterface getIncomeByRace(){
		return new IncomeByRaceUI("income");
	}
	
	public static StartInterface getWeeklyWageByRace(){
		return new IncomeByRaceUI("wage");
	}
	
	public static CalculateByStateInterface getCalculateByState() {
		return new CalculateByState();
	}
	
	
	public static StartInterface getIncomeByState(){
		return new IncomeByStateUI("income");
	}
	
	public static StartInterface getWeeklyWageByState(){
		return new IncomeByStateUI("wage");
	}
	
	public static CalculateByOccupationOrIndustryCodeInterface getCalculateByOccupationOrIndustryCode(){
		return new CalculateByOccupationOrIndustryCode();
	}
	
	public static StartInterface getIncomeByOccupationOrIndustryCode(){
		return new IncomeByOccupationOrIndustryCodeUI("income");
	}
	
	public static StartInterface getWeeklyWageByOccupationOrIndustryCode(){
		return new IncomeByOccupationOrIndustryCodeUI("wage");
	}
	
	public static TypeWageDataStructureInterface getTypeWageDataStructure(String type, double wage, int workWeeks){
		return new TypeWageDataStructure(type, wage, workWeeks);
	}
	
	public static CalculateByEducationInterface getCalculateByEducation(){
		return new CalculateByEducation();
	}
	
	public static TypeWageDataStructureInterface getEducationWageDataStructure(String education, int weekWage, int workWeeks){
		return new TypeWageDataStructure(education, weekWage, workWeeks);
	}
	
	public static StartInterface getIncomeByEducation(){
		return new IncomeByEducationUI("income");
	}
	
	public static StartInterface getWeeklyWageByEducation(){
		return new IncomeByEducationUI("wage");
	}
	
	public static StartInterface getWeeklyWageForCategories(){
		return new CalculateWeeklyWageUI();
	}
	
	public static StartInterface getEstimateIncomeGUI() {
		return new EstimateIncomeUI();
	}
	
	public static EstimateIncomeInterface getEstimateIncome(){
		return new EstimateIncome();
	}
	
	public static double getRoundMethod(double value){
		return new RoundValue().roundValue(value);
	}
	
	public static CalcImplInterface getWeeklyImpl(){
		return new WeeklyImpl();
	}
	
	public static CalcImplInterface getYearlyImpl(){
		return new YearlyImpl();
	}
	
	public static CalcImplWithDescriptionInterface getWeeklyImplWithDescription(String description){
		return new WeeklyImplWithDescription(description);
	}
	
	public static CalcImplWithDescriptionInterface getYearlyImplWithDescription(String description){
		return new YearlyImplWithDescription(description);
	}
	
	public static EstimateIncomeDTOInterface getEstimateIncomeDTO(String name, int ssn, String race,
			String education, int occupationCode, int industryCode,
			int weeklyWage, int workWeeks, int investmentIncome){
		return new EstimateIncomeDTO(name, ssn, race, education, occupationCode, industryCode,
				weeklyWage, workWeeks, investmentIncome);
	}
		
}
