package com.riskIt.util;

import java.math.BigDecimal;

/**
 * RoundValue.java
 * Purpose: Method takes a double value and returns a double value
 * 	rounded to two decimal places using the 'bankers round' method of
 * 	the BigDecimal class.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public class RoundValue {

	public double roundValue(double value) {
		BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	    bd.doubleValue();
		
		return bd.doubleValue();
	}

}
