package com.riskIt.data;


/**
 * This class is a data structure that holds information of customer using
 * SSN and their income and their score. This class has all accessor 
 * and mutator methods
 * @author Poornima Tantry
 *
 */
public class ScoreData 
{
	private long ssn;
	private double income;
	private int score;
	
	public void setSSN(long s)
	{
		ssn = s;
	}
	public void setIncome(double i)
	{
		income = i;
	}
	public void setScore(int s)
	{
		score = s;
	}
	
	
	public long getSSN()
	{
		return ssn;
	}
	public double getIncome()
	{
		return income;
	}
	public int getScore()
	{
		return score;
	}

	
}
