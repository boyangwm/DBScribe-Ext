package com.riskIt.app;

import java.util.ArrayList;

import com.riskIt.controller.UserManager;
import com.riskIt.data.Invitation;

public class testCustomerQuotesDriver {

	/**
	 * @param args
	 */

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UserManager user;
		user = new UserManager();
		ArrayList<Invitation> inv1 = new ArrayList<Invitation>();
		Invitation cust1 = new Invitation();
		long ssn = Integer.parseInt(args[0]);
		double deductible = 50;
		double worth = 10000;
		cust1 = user.calculateQuoteForCustomer(ssn, deductible, worth, inv1);
		cust1.getPremium();
	
	}

}
