package com.riskIt.app;

import com.riskIt.controller.Requirements19_24;

public class testLiklinessToMoveFactor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Requirements19_24 requirements;
		requirements = new Requirements19_24();
		requirements.CalculateLikelinessToMoveFactor(Integer.parseInt(args[0]));
	}

}
