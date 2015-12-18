package com.riskIt.data;

import java.util.HashMap;

import com.riskIt.interfaces.EstimateIncomeDTOInterface;
import com.riskIt.util.Factory;

/**
 * EstimateIncomeDTO.java
 * Purpose: Object used to transfer data to used for the
 * 	estimate income function.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class EstimateIncomeDTO implements EstimateIncomeDTOInterface{
	
	final String name;
	final int ssn;
	final String race;
	final String education;
	final int occupationCode;
	final int industryCode;
	final int weeklyWage;
	final int workWeeks;
	final int investmentIncome;
	
	final String industryCodeDescription;
	final String occupationCodeDescription;

	public EstimateIncomeDTO(String name, int ssn, String race,
			String education, int occupationCode, int industryCode,
			int weeklyWage, int workWeeks, int investmentIncome){
		
		this.name = name;
		this.ssn = ssn;
		this.race = race;
		this.education = education;
		this.occupationCode = occupationCode;
		this.industryCode = industryCode;
		this.weeklyWage = weeklyWage;
		this.workWeeks = workWeeks;
		this.investmentIncome = investmentIncome;
		
		this.industryCodeDescription = setIndustryCodeDescription(industryCode);
		this.occupationCodeDescription = setOccupationCodeDescription(occupationCode);
		
	}
	
	private String setIndustryCodeDescription(int industryCode){
		HashMap<Integer, String> industryMap = null;
		industryMap = Factory.getCalculateByOccupationOrIndustryCode().getIndustryCodeAndNameMap();
		return industryMap.get(industryCode);
	}
	
	private String setOccupationCodeDescription(int occupationCode){
		HashMap<Integer,String> occupationMap = null;
		occupationMap = Factory.getCalculateByOccupationOrIndustryCode().getOccupationCodeAndNameMap();
		return occupationMap.get(occupationCode);
	}
	
	public String getName(){
		if(name == null || name.trim().equalsIgnoreCase("null")){
			return "None Entered";
		} else {
			return name;
		}
	}
	public int getSSN(){
		return ssn;
	}
	
	public String getRace(){
		if(race == null || race.trim().equalsIgnoreCase("null")){
			return "None Entered";
		} else {
			return race;
		}
	}
	
	public String getEducation(){
		if(education == null || education.trim().equalsIgnoreCase("null")){
			return "None Entered";
		} else {
			return education;
		}
	}
	
	public int getOccupationCode(){
		return occupationCode;
	}
	
	public int getIndustryCode(){
		return industryCode;
	}
	
	public int getWeeklyWage(){
		return weeklyWage;
	}
	
	public int getWorkWeeks(){
		return workWeeks;
	}
	
	public int getInvestmentIncome(){
		return investmentIncome;
	}
	
	public String toString(){
		return "Name: " + name + "\nSSN: " + ssn + "\nRace: " + race + "\nEducation: " + education +
		"\nOccupation Code: " + occupationCode + " : " + occupationCodeDescription +
		"\nIndustryCode: " + industryCode + " : " + industryCodeDescription +
		"\nWeekly Wage: " + weeklyWage + "\nWork Weeks: " + workWeeks + "\nInvestment Income: " + investmentIncome;
	}

}
