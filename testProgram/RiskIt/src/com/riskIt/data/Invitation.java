package com.riskIt.data;

public class Invitation 
{
	/**
	 * This class is a data structure to hold the values of the selected customer
	 * This class has all accessor and mutator properties.
	 * @author Poornima Tantry 
	 */
	private int agent_id;
	private int age;
	private long user_ssn;
	private double property_value;
	private double score;
	private double premium;
	private double deductible;
	private double expo_unit;
	private double expenseExpo;
	private double lossCoverage;
	private String zipcode;
	private String stateName;
	private int acceptInvitation;
	
	public void setAgentID(int id)
	{
		agent_id = id;
	}
	public void setUserSSN(long ssn)
	{
		user_ssn = ssn;
	}
	public void setPropertyValue(double value)
	{
		property_value = value;
	}
	public void setScore(double sc)
	{
		score = sc;
	}
	public void setPremium(double primi)
	{
		premium = primi;
	}
	public void setDeductible(double deduct)
	{
		deductible = deduct;
	}
	public void setExpoUnit(double expo)
	{
		expo_unit = expo;
	}
	public void setExpenseExpo(double expo)
	{
		expenseExpo = expo;
	}
	public void setlossCoverage(double loss)
	{
		lossCoverage = loss;
	}
	public void setZipcode(String zip)
	{
		zipcode = zip;
	}
	public void setStateName(String state)
	{
		stateName = state;
	}
	public void setAge(int age)
	{
		this.age = age;
	}
	public void setAcceptInvite(int num)
	{
		acceptInvitation = num;
	}
	
	
	public int getAgentId()
	{
		return agent_id;
	}
	public long getUserSSN()
	{
		return user_ssn;
	}
	public double getPropertyValue()
	{
		return property_value;
	}
	public double getScore()
	{
		return score;
	}
	public double getPremium()
	{
		return premium;
	}
	public double getDeductible()
	{
		return deductible;
	}
	public double getExpoUnit()
	{
		return expo_unit;
	}
	public double getExpenseExpo()
	{
		return expenseExpo;
	}
	public double getlossCoverage()
	{
		return lossCoverage;
	}
	public String getzipCode()
	{
		return zipcode;
	}
	public String getStateName()
	{
		return stateName;
	}
	public int getAge()
	{
		return age;
	}
	public int getAcceptInvite()
	{
		 return acceptInvitation;
	}
	
	
	
}
