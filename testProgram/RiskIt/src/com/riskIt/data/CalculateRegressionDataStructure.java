package com.riskIt.data;

/**
 * CalculateRegressionDataStructure.java
 * Purpose: Data Structure used to return slop and intercept values to
 * 	EstiamteIncome class after they have been calculated by CalculateRegression
 * 	Class.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class CalculateRegressionDataStructure {
	
	final double slopeAll;
	final double interceptMainAll;
	final double slopeMiddleTwoThirds;
	final double interceptMiddleTwoThirds;
	
	public CalculateRegressionDataStructure(double slopeALl, double interceptAll, double slopeMiddleTwoThirds, double interceptMiddleTwoThirds){
		this.slopeAll = slopeALl;
		this.interceptMainAll = interceptAll;
		this.slopeMiddleTwoThirds = slopeMiddleTwoThirds;
		this.interceptMiddleTwoThirds = interceptMiddleTwoThirds;
	}
	
	public double getSlopeAll(){
		return slopeAll;
	}
	
	public double getInterceptAll(){
		return interceptMainAll;
	}
	
	public double getSlopeMiddleTwoThirds(){
		return slopeMiddleTwoThirds;
	}
	
	public double getInterceptMiddleTwoThirds(){
		return interceptMiddleTwoThirds;
	}
}
