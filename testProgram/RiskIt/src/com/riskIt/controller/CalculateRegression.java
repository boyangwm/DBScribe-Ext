package com.riskIt.controller;

import java.util.ArrayList;

import com.riskIt.data.CalculateRegressionDataStructure;


/**
 * CalculateRegression.java
 * Purpose: Calculation class to calculate linear regression based on two
 * 	lists of integers.  Called by EstiamteIncome Class to calculate the
 * 	simple linear regression values.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

/**
 * Object to perform all calculations necessary to calculate
 * regression analysis on data contained in the database.
 */
public class CalculateRegression {
	
	/**
	 * Performs regression analysis for two lists of integers
	 * <p>
	 * @param 				An arrayList<Integer> - first data
	 * @param 				An arrayList<Integer> - second data
	 * @return				CalculateRegressionDataStructure
	 * 							This is a data transfer object used to return
	 * 							the linear regression equation y = m(x) + b
	 * 						Note: Calculations will be performed on the entire
	 * 						data set, and the middle 2/3's of the data set so a
	 * 						comparison can be made regarding the accuracy of the
	 * 						linear regression formula.
	 */
	public CalculateRegressionDataStructure calculateRegressionNumbers(ArrayList<Integer> firstDataSet, ArrayList<Integer> secondDataSet) throws IllegalArgumentException {
		
		if(firstDataSet == null || secondDataSet == null){
			throw new IllegalArgumentException();
		}
		
		double slopeAll = 0;
		double interceptAll = 0;
		double slopeMiddleTwoThirds = 0;
		double interceptMiddleTwoThirds = 0;
		int secondNumber = 0;
		double size = firstDataSet.size();
		double firstSum = 0;
		double secondSum = 0;
		double sumFirstMultSecond = 0;
		double sumFirstSquared = 0;
		
		ArrayList<calcRegressionStructure> variableIncomeList = new ArrayList<calcRegressionStructure>();
		
		// Calculate slope and intercept using full data set.
		int count = 0;
		for(int firstNumber : firstDataSet){
			secondNumber = secondDataSet.get(count);
			
			variableIncomeList.add(new calcRegressionStructure(firstNumber,secondNumber));
			count++;
			firstSum = firstSum + firstNumber;
			secondSum = secondSum + secondNumber;
			sumFirstMultSecond = sumFirstMultSecond + (firstNumber * secondNumber);
			sumFirstSquared = sumFirstSquared + (firstNumber * firstNumber);
		}
		
		double numerator = ((size * sumFirstMultSecond) - (firstSum * secondSum));
		double denominator = ((size * sumFirstSquared) - (firstSum * firstSum));

		if(denominator == 0){
			slopeAll = 0;
		} else {
			slopeAll = numerator / denominator;
		}
		
		if(size == 0) {
			interceptAll = 0;
		} else {
			interceptAll = (secondSum - (slopeAll*firstSum))/size;
		}
		
		// Calculate slope and intercept for middle 2/3 of data
		// sorted based on Income
		
		secondNumber = 0;
		size = 0;
		firstSum = 0;
		secondSum = 0;
		sumFirstMultSecond = 0;
		sumFirstSquared = 0;
		numerator = 0;
		denominator = 0;
		
		count = 0;
		int firstNumber;
		for(calcRegressionStructure i : variableIncomeList){
			count++;
			if(count > (int)(variableIncomeList.size() * (1.0/6.0)) && count < (int)(variableIncomeList.size() * (5.0/6.0))){
				size++;
				firstNumber = i.getFirstValue();
				secondNumber = i.getSecondValue();
				firstSum = firstSum + firstNumber;
				secondSum = secondSum + secondNumber;
				sumFirstMultSecond = sumFirstMultSecond + (firstNumber * secondNumber);
				sumFirstSquared = sumFirstSquared + (firstNumber * firstNumber);
			}
		}
		
		numerator = ((size * sumFirstMultSecond) - (firstSum * secondSum));
		denominator = ((size * sumFirstSquared) - (firstSum * firstSum));
		
		if(denominator == 0){
			slopeMiddleTwoThirds = 0;
		} else {
			slopeMiddleTwoThirds = numerator / denominator;
		}
		
		if(size == 0) {
			interceptMiddleTwoThirds = 0;
		} else {
			interceptMiddleTwoThirds = (secondSum - (slopeAll*firstSum))/size;
		}
		
		return new CalculateRegressionDataStructure(slopeAll,interceptAll,slopeMiddleTwoThirds,interceptMiddleTwoThirds);
	}
	
	private class calcRegressionStructure implements Comparable<calcRegressionStructure>{
		final int firstValue;
		final int secondValue;
		
		calcRegressionStructure(int firstValue, int secondValue){
			this.firstValue = firstValue;
			this.secondValue = secondValue;
		}
		
		int getFirstValue(){
			return firstValue;
		}
		
		int getSecondValue(){
			return secondValue;
		}

		public int compareTo(calcRegressionStructure c) {
			return this.getFirstValue() - c.getFirstValue();
		}
	}

}
