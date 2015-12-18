package com.riskIt.util;

import java.sql.SQLException;

public class UtilitiesReq19_24 {
    	public static double CalculateIncomePercentageDifference(double MaxAverageIncome, double MinAverageIncome){

        if( MaxAverageIncome > MinAverageIncome )
           return (100*MinAverageIncome/MaxAverageIncome);
        else
           return (100*MaxAverageIncome/MinAverageIncome);

       }
       /**
        * Reports a data verification failure to System.err with the given message.
        *
        * @param message A message describing what failed.
        */
       public static void reportFailure(String message) {
    	   System.out.println("flow:26");
           System.err.println("\nData verification failed:");
           System.err.println('\t' + message);
       }

       public static void printExceptionText(SQLException e){
         while (e != null)
         {
        	 System.out.println("flow:25");
        	 System.err.println("\n----- SQLException -----");
        	 System.err.println("  SQL State:  " + e.getSQLState());
        	 System.err.println("  Error Code: " + e.getErrorCode());
        	 System.err.println("  Message:    " + e.getMessage());
        	 // for stack traces, refer to derby.log or uncomment this:
        	 //e.printStackTrace(System.err);
        	 e = e.getNextException();
         }
       }
}
