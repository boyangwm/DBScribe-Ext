package com.riskIt.app;

import java.util.ArrayList;

import com.riskIt.controller.AgentManager;
import com.riskIt.controller.Requirements19_24;
import com.riskIt.controller.UserManager;
import com.riskIt.data.Invitation;

public class testCalculateScoreDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	//pads the entries by zero to accomodate for copy 
		try{
		System.out.println("TestCase: Calculate Score");
		AgentManager agent;
		agent = new AgentManager();
		agent.calculateScore(Integer.parseInt(args[0]));
		}catch(Exception e){
			System.out.println("exception");
		}
		try{
		System.out.println("TestCase: Calculate CustomreQuotes");
		UserManager user;
		user = new UserManager();
		ArrayList<Invitation> inv1 = new ArrayList<Invitation>();
		Invitation cust1 = new Invitation();
		long ssn = Integer.parseInt(args[0]);
		double deductible = 50;
		double worth = 10000;
		cust1 = user.calculateQuoteForCustomer(ssn, deductible, worth, inv1);
		cust1.getPremium();
		
	}catch(Exception e){
		System.out.println("exception");
	}
		
		
	try{
		System.out.println("TestCase: Calculate Likeliness To Move Factor");
		Requirements19_24 requirements;
		requirements = new Requirements19_24();
		requirements.CalculateLikelinessToMoveFactor(Integer.parseInt(args[0]));
		
	}catch(Exception e){
		System.out.println("exception");
	}
	}

}
