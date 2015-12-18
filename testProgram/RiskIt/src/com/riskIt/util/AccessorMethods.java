package com.riskIt.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.riskIt.data.ScoreData;

/**
 * This class provides supporter methods that are needed for all other classes
 * @author Poornima Tantry
 *
 */
public class AccessorMethods
{

	public AccessorMethods()
	{

	}

	/**
	 * Method to calculate income using weekwage, number of workweek,
	 * dividends, losses and gains
	 * @param week - number of weeks
	 * @param wage - wage per week
	 * @param gains - capital gains
	 * @param loss - capital losses
	 * @param dividend - stock dividends
	 * @return income - the calculated income for the person
	 * @author Poornima Tantry
	 */


	public double calculateIncome(int week, int wage, int gains, int loss, int dividend)
	{
		double income = 0;

		income = (double)(week * wage ) + ( gains - loss) + dividend;

		return income;
	}

	/**
	 * This method accepts variables all data to calculate income and then
	 * sorts it in ascending order to find the range for given zipcode.
	 * @param week - number of work weeks for customer
	 * @param wage - week wage for customer
	 * @param gains - customer's capital gains
	 * @param loss - customer's capital losses
	 * @param dividend - customer's stock dividends
	 * @param ssn - ssn of the customer
	 * @author Poornima Tantry
	 */
	
	public ArrayList<ScoreData> calculateRange(int week, int wage, int gains, int loss,
							   int dividend, long ssn, ArrayList<ScoreData> sData)
	{
		double income = 0;
		sData.clear();

		try
		{
			income = calculateIncome(week, wage, gains, loss, dividend);

			ScoreData data = new ScoreData();
			data.setIncome(income);

			data.setSSN(ssn);
			sData.add(data);

			if(sData.size() > 0)
				sData = sortData(sData);
		}
		catch(Exception e)
		{
			System.out.println("Exception in calculateRange");
			e.printStackTrace();
		}
		return sData;
	}

	/**
	 * Method to sort the data based on income
	 * @param arraylist - list of people with ssn, income and score
	 * @return arraylist - sorted list based on income
	 * @author Poornima Tantry
	 */


	  public ArrayList<ScoreData> sortData(ArrayList<ScoreData> sData)
	  {

	  	for(int i =1; i < sData.size(); i++)
	  	{
	  		ScoreData temp = sData.get(i);
	  		double _income = temp.getIncome();
	  		int j = i;

	  		while(j > 0 && sData.get(j-1).getIncome() > _income)
	  		{
	  			sData.set(j, sData.get(j-1));
	  			j--;
	  		}
	  		sData.set(j, temp);
	  	}
	  	return sData;
	  }

/**
 * This method calculates score based on margin values
 * @param unionMember - finds if customer is union member
 * @param indus_stab - industry stability code
 * @param occu_stab - occupation stability code
 * @param edu - education of customer
 * @param tempScore - score calculated till this point
 * @return tempScore - the adjusted score value
 * @author Poornima Tantry
 */
	public double checkScoreRange(String unionMember, int indus_stab, int occu_stab,
									String edu, double tempScore)
	{
		double sqrt_s;


		//check if person is in union
		if((unionMember == null || unionMember.equals(" No"))
				&& tempScore < RiskItConstants.THRESHOLD)
		{
			sqrt_s = (int)Math.sqrt(indus_stab * occu_stab) * 10;

			//check if sqrt_s is in 1 - 10 range
			if(sqrt_s >= 0 && sqrt_s <=10 )
				tempScore = 0;
			else if(sqrt_s > 10 && sqrt_s <= 60) // range of 10-60
			{
				tempScore = tempScore * (sqrt_s/80);
				if( edu.equals(" Masters degree(MA MS MEng MEd MSW MBA)") ||
					edu.equals(" Bachelors degree(BA AB BS)")||
					edu.equals(" Doctorate degree(PhD EdD)")||
					edu.equals(" Prof school degree (MD DDS DVM LLB JD)"))
				{
					tempScore = tempScore + (tempScore/100 *20);
				}
			}
			else if(sqrt_s >60 && sqrt_s <=100) //range of 60-100
			{
				tempScore = tempScore *(sqrt_s/100);
				{
					if( edu.equals(" Masters degree(MA MS MEng MEd MSW MBA)") ||
						edu.equals(" Bachelors degree(BA AB BS)")||
						edu.equals(" Doctorate degree(PhD EdD)")||
						edu.equals(" Prof school degree (MD DDS DVM LLB JD)"))
					{
						tempScore = tempScore + (tempScore/100 *12);
					}
				}
			}
			else if(sqrt_s > 100)
				tempScore = 100;
		}
		return tempScore;
	}

/**
 * This method accepts marital status, age , number of employees and self-employment
 * count and accordingly adjusts the score value
 * @param marital_status - marital status of person
 * @param age - age of person
 * @param selfEmp - if he/she self employed
 * @param noOfEmp - number of employees working with the customer
 * @param tempScore - temporary score calculated till this point
 * @return tempscore - the newly adjusted score value
 * @author Poornima Tantry
 */
	public double checkScoreForStatus(String marital_status,  int age, int selfEmp,
										int noOfEmp, double tempScore)
	{

		//check if person is married score goes up 10 %
		if(marital_status.trim().equals("Married-civilian spouse present") && age < 65)
			tempScore = tempScore + (tempScore/100 *20);
		else if(marital_status.trim().equals("Never married") && age < RiskItConstants.FIFTY_AGE)
			tempScore = tempScore + (tempScore/100 * RiskItConstants.TEN_PERCENT);
		else if((marital_status.trim().equals("Divorced")||
				marital_status.trim().equals("Widowed")) &&
				(age < RiskItConstants.FIFTY_AGE))
		{
			tempScore = tempScore - (tempScore/100 *RiskItConstants.TEN_PERCENT);
		}
		else if((marital_status.trim().equals("Divorced")||
				 marital_status.trim().equals("Widowed")||
				 marital_status.trim().equals("Never married"))&&
				 age > RiskItConstants.FIFTY_AGE)
		{
			tempScore = tempScore - (tempScore/100 * RiskItConstants.FIFTY_PERCENT);
		}
		else if(selfEmp == 0 )
			tempScore = tempScore + (tempScore/100 * RiskItConstants.TWENTY_PERCENT);
		else if( selfEmp > 0 && noOfEmp > RiskItConstants.MAX_EMP_COUNT)
			tempScore = tempScore - (tempScore/100 * RiskItConstants.TEN_PERCENT);
		else if(selfEmp > 0 && noOfEmp <= RiskItConstants.MAX_EMP_COUNT)
			tempScore = tempScore + (tempScore/100 * RiskItConstants.TEN_PERCENT);

		return tempScore;
	}

	 /**
	   * calculate the percentile of the person with all people in same zipcode
	   * @param income - get income of the person
	   * @param arraylist - list of customer holding ssn, income and score
	   * @return percent - the percentile value
	   * @author Poornima Tantry
	   */
	
	public int calculatePercentile(double income, ArrayList<ScoreData> sData)
	{
		int percent = 0, count = 0;
		double tempPercent = 0.0;

		//check for number of people below the given income
		for(int i=0; i < sData.size(); ++i)
		{
			if(sData.get(i).getIncome() < income)
				++ count;
		}
		int size = sData.size();

		//if only 1 record
		if(sData.size() == 1)
			count = 1;

		if(size < 1)
			size = 1;

			tempPercent = (count)/size ;
			percent = (int)tempPercent;

		return percent;
	}
	
	/**
	 * This method checks to see if the given input contains only numbers
	 * @param input - string containing the input
	 * @param invalidNum - invalid code
	 * @return - return the right number format
	 * @author Poornima Tantry
	 */
	
	public int checkLetterDigit(String input, int invalidNum)
	{
		boolean letterFlag = false;
		int choice = 0;
		
		if(input.length() <= 0)
			choice = 0;
		else
		{
			char[] temparr = input.toCharArray();
			for(int i=0; i<temparr.length; ++i)
			{
				if(!Character.isDigit(temparr[i]))
				{
					letterFlag = true;
					choice = invalidNum;
					break;
				}
			}
			
			if(!letterFlag)
			choice = Integer.parseInt(input);
		}
		return choice;
	}
	
	/**
	 * This method checks to see if all the letters are string
	 * @param input - String input value
	 * @return - String representation of input, else empty string
	 * @author Poornima Tantry
	 */
	public String checkLetter(String input)
	{
		boolean letterFlag = false;
		String choice = "";
		
		if(input.length() <= 0)
			choice = "";
		else
		{
			char[] temparr = input.toCharArray();
			for(int i=0; i<temparr.length; ++i)
			{
				if(!Character.isLetter(temparr[i]))
				{
					letterFlag = true;
					choice = "";
					break;
				}
			}
			
			if(!letterFlag)
			choice = input;
		}
		return choice;
	}
	
	public void printRs(ResultSet rs) throws SQLException{
		int industrycode;
	   int occupationcode;
	   int average;

	    //Ensure we start with first row
	   try{
        rs.beforeFirst();
	    while(rs.next()){
	      //Retrieve by column name
	      industrycode= rs.getInt("industrycode");
	     occupationcode = rs.getInt("occupationcode");
	      average = rs.getInt("meanweekwage");

	      //Display values
	      System.out.print("Row Number=" + rs.getRow());
	      System.out.print(", industrycode: " + industrycode);
	      System.out.print(", occupationcode: " + occupationcode);
	      System.out.println(", meanweekwage: $" + average);
	    }
	    System.out.println();
	   }catch(NullPointerException e){
		   e.printStackTrace();
	   }
	}
	public void printResult(ResultSet rs) throws SQLException{
		int ssn;
		String workclass;
	    int industrycode;
	    int occupationcode;
	    String unionmember;
	    int employersize;
	    int weekwage;
	    int selfemployed;
	    int workweeks;


	    //Ensure we start with first row

        rs.beforeFirst();
	    while(rs.next()){
	      //Retrieve by column name
	    	ssn = rs.getInt("ssn");
	    	workclass = rs.getString("workclass");
	        industrycode= rs.getInt("industrycode");
	        occupationcode = rs.getInt("occupationcode");
	        unionmember = rs.getString("unionmember");
	        employersize = rs.getInt("employersize");
	        weekwage = rs.getInt("weekwage");
	        selfemployed = rs.getInt("selfemployed");
	        workweeks = rs.getInt("workweeks");

	      //Display values
	      System.out.print("Row Number=" + rs.getRow());
	      System.out.println("," + " ssn: " + ssn);
	      System.out.println("," + " workclass: " + workclass);
	      System.out.println(","   + " industrycode: " + industrycode);
	      System.out.println(","   + " occupationcode: " + occupationcode);
	      System.out.println("," + " unionmember: " + unionmember);
	      System.out.println("," + " employersize: " + employersize);
	      System.out.println("," + " weekwage: " + weekwage);
	      System.out.println("," + " selfemployed: " + selfemployed);

	      System.out.println("," + " workweeks: " + workweeks);
	    }
	    System.out.println();
	  }

	public void printIndustry(ResultSet rs) throws SQLException{
		int industrycode;
	   String industry;
	   int Stability;

	    //Ensure we start with first row

       rs.beforeFirst();
	    while(rs.next()){
	      //Retrieve by column name
	      industrycode= rs.getInt("industrycode");
	     industry = rs.getString("industry");
	      Stability = rs.getInt("Stability");

	      //Display values
	      System.out.print("Row Number=" + rs.getRow());
	      System.out.print(", industrycode: " + industrycode);
	      System.out.print(", industry: " + industry);
	      System.out.println(", Stability: " + Stability);
	    }
	    System.out.println();
	  }
	public void printOccupation(ResultSet rs) throws SQLException{
		int occupationcode;
	   String occupation;
	   int Stability;

	    //Ensure we start with first row

        rs.beforeFirst();
	    while(rs.next()){
	      //Retrieve by column name
	      occupationcode= rs.getInt("occupationcode");
	      occupation = rs.getString("occupation");
	      Stability = rs.getInt("Stability");

	      //Display values
	      System.out.print("Row Number=" + rs.getRow());
	      System.out.print(", occupationcode: " + occupationcode);
	      System.out.print(", occupation: " + occupation);
	      System.out.println(", Stability: " + Stability);
	    }
	      System.out.println();
	  }
	public int getOccupation(int[] code, int occupcode)
	    {
	       int i=0;

	       for(i=0; i<50; i++)
	       {

	        if(code[i]==0 || occupcode == 0)
	           continue;

	        if(code[i] == occupcode)
	          return i;
	       }
	       return i;
	    }
	public int getIndustryCode(int[] code, int inccode)
	    {
			   int i=0;
			   for(i=0; i<50; i++)
			  {
			      if(code[i]==0 || inccode == 0)
			           continue;
			      if(code[i] == inccode)
			          return i;
			   }
			       return i;
	    }
}
