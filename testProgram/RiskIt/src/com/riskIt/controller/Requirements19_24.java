package com.riskIt.controller;

import java.sql.*;

import java.util.HashMap;

import java.util.ArrayList;

import tada.TaDaMethod;

import com.riskIt.util.UtilitiesReq19_24;

import com.riskIt.interfaces.CalculateByOccupationOrIndustryCodeInterface;

import com.riskIt.db.DatabaseConnection;

import com.riskIt.util.Factory;

/**
 * Requirements19_24.java
 * Purpose: Processing of the requirements ( main menu options) 19-24.
 * 
 * @author Selma Tiganj
 * @version 1.0 7-15-09
 */

public class Requirements19_24 {

    private Connection   _conn;   
    private Statement    s; 
    private static final int ErrorValue = 35565;
    private static final int maxEduDegrees = 17;
      
    public Requirements19_24()
    {   
        _conn = null; 
        s = null;
    }

	@TaDaMethod(variablesToTrack = {"educationArray"}, correspondingDatabaseAttribute = {"education.educationmap"} )
	private void getEducationResultSet(String[] educationArray ){
		
        ResultSet rs = null;
		
        try {
			_conn = DatabaseConnection.getConnection();
			s = _conn.createStatement();
			//_conn.setAutoCommit(false);
			int i =0;
			
			s.execute(
					  "create table educationmap(LEVEL int NOT NULL, EDUCATION char(50), PRIMARY KEY (LEVEL))");
			
			s.execute("insert into educationmap values (0, ' Children')");
			s.execute("insert into educationmap values (1, ' Less than 1st grade')");
			s.execute("insert into educationmap values (2, ' 1st 2nd 3rd or 4th grade')");
			s.execute("insert into educationmap values (3, ' 5th or 6th grade')");
			s.execute("insert into educationmap values (4, ' 7th and 8th grade')");
			s.execute("insert into educationmap values (5, ' 9th grade')");
			s.execute("insert into educationmap values (6, ' 10th grade')");
			s.execute("insert into educationmap values (7, ' 11th grade')");
			s.execute("insert into educationmap values (8, ' 12th grade no diploma')");
			s.execute("insert into educationmap values (9, ' High school graduate')");
			s.execute("insert into educationmap values (10, ' Some college but no degree')");
			s.execute("insert into educationmap values (11, ' Associates degree-occup /vocational')");
			s.execute("insert into educationmap values (12, ' Associates degree-academic program')");
			s.execute("insert into educationmap values (13, ' Prof school degree (MD DDS DVM LLB JD)')");
			s.execute("insert into educationmap values (14, ' Bachelors degree(BA AB BS)')");
			s.execute("insert into educationmap values (15, ' Masters degree(MA MS MEng MEd MSW MBA)')"); 
			s.execute("insert into educationmap values (16, ' Doctorate degree(PhD EdD)')"); 
			
			
			rs = s.executeQuery("SELECT level, education FROM educationmap");
			
			if (!rs.next())
			{
				UtilitiesReq19_24.reportFailure("No rows in ResultSet. No data found in internal educationmap table.");
				return;
			}
			
			do{
				educationArray[i++] = rs.getString(2);
			}while(rs.next());
			s.execute("drop table educationmap");
			
			rs.close();
			//s.comit();
			s.close();
        } catch (SQLException e)
		{
        	UtilitiesReq19_24.printExceptionText(e);
        }
        return;
	}

//  ########################### FIND TOP STATES ###################################

     public ArrayList<String> FindTopStatesByCategory(String selection, String category ) {
      //Select top 5 states that have the most workers based on occupation category
      ResultSet rs = null;
      ArrayList<String> findBestStatesResult = new ArrayList<String>();
      int i =0;
      ArrayList<Integer> code = new ArrayList<Integer>();
      try{
       _conn = DatabaseConnection.getConnection();
       s = _conn.createStatement();
       rs = s.executeQuery(     
              "SELECT " + selection +"code FROM " + selection + " WHERE  " + selection +"="+ category );
       if (!rs.next())
       {
    	   UtilitiesReq19_24.reportFailure("No rows in ResultSet");
                return findBestStatesResult;
       }
       do{
          code.add(rs.getInt(1));
          i++;
       }while(rs.next());

       rs.close();
       s.close();
       findBestStatesResult = FindTopStatesByCode( selection, code);
       
     } catch (SQLException e)
        {
    	 UtilitiesReq19_24.printExceptionText(e);
      }
      return findBestStatesResult;
    }

    @TaDaMethod(variablesToTrack = {"ssn"}, correspondingDatabaseAttribute = {"job.ssn"} )
    public ArrayList<String> FindTopStatesByCode(String selection, ArrayList<Integer> code) {
      //Select top 5 states that have the most workers based on occupation/industry code
      ResultSet rs = null;
      ResultSet rs1 = null;
      int ssn = 0;
      String zip = "";
      String state = "";
      double[] resultCount = new double[50];
      String[] resultStates= new String[50];
      int resultIndex = 0;
      int stateIndex =0;
      ArrayList<String> findBestStatesResult = new ArrayList<String>();
      String sqlQuery ="SELECT ssn FROM job WHERE " + selection +"code IN (";
      for(int i=0; i<code.size(); i++){
          sqlQuery+=code.get(i);
          if(i<code.size()-1)
              sqlQuery+=", ";
      }
      sqlQuery+=")";

      HashMap<String, String> zipToStateMap = new HashMap<String, String>();
      HashMap<Integer, String> ssnToZipMap = new HashMap<Integer, String>();
        
      initializeSsnToZipMap(ssnToZipMap);
      
      initializeZipToStateMap(zipToStateMap);
         
      try{
        _conn = DatabaseConnection.getConnection();
    	s = _conn.createStatement();
        
        rs = s.executeQuery(sqlQuery);

        if (!rs.next())
        {
        	UtilitiesReq19_24.reportFailure("No rows in ResultSet. No SSN for code: " + code);
                return findBestStatesResult;
        }
        do{
          //System.out.println("SSN: " + rs.getInt(1));
          ssn = rs.getInt(1);
          
          zip = ssnToZipMap.get(ssn);
          if(zip==null)
             continue;
          state = zipToStateMap.get(zip);
          if(state==null)
             continue;
          stateIndex = getStateIndex(resultStates, state);
          if( stateIndex < 50){
            //System.out.println("Increasing count for state: " + resultStates[stateIndex] + " with index: " + stateIndex);
            resultCount[stateIndex]++;
          }
          else{
            //System.out.println("stateIndex: " + stateIndex);
            resultCount[resultIndex]++;
            resultStates[resultIndex] = state;
            resultIndex++;
          }         
          
       }while(rs.next());
      
        findBestStatesResult = FindBestStates(5, resultStates, resultCount, resultIndex, " Number of workers: ");
       rs.close();
       s.close();
      } catch (SQLException e)
        {
    	  UtilitiesReq19_24.printExceptionText(e);
      } 
      return findBestStatesResult;
   }

    
    private ArrayList<String> FindBestStates(int numResults, String[] resultStates, double[] resultValues, int resultIndex, String text){

       double maxValue =0;
       int maxIndex =0;
       int j=0;
       ArrayList<String> findBestStatesResult = new ArrayList<String>();
       
       System.out.println("\nThe best states are: "); 
       for(int i=0; i<numResults; i++){
          for(j=0; j<resultIndex; j++){
             if(maxValue < resultValues[j]){
                maxValue = resultValues[j];
                maxIndex=j;
             }
          }

          if(maxValue != 0){
        	findBestStatesResult.add("State: " + resultStates[maxIndex] + text + maxValue);
            System.out.println("State: " + resultStates[maxIndex] + text + maxValue);
            System.out.println("");
            maxValue = 0;
            resultValues[maxIndex] = 0;
          }
       }
       System.out.println("\n");
       return findBestStatesResult;
    }
    private int getStateIndex(String[] states, String state)
    {
       int i=0;
       //System.out.println("State passed to getStateIndex: " + state);
       for(i=0; i<50; i++)
       {
        //System.out.println("State[" +i+ "] passed to getStateIndex: " + states[i]);
        if(states[i]==null || state == null)
           continue;

        if(states[i].equals(state))
          return i;
       }
       return i;
    }

// #########################RECOMMEND BEST STATE TO WORK ######################
    @TaDaMethod(variablesToTrack = {"code"}, correspondingDatabaseAttribute = {"job.occupationcode"} )
    public ArrayList<String> RecommendBestStateToWork(int ssn){

        ResultSet rs = null;
        String[] resultStates= new String[50];
        double[] averageWage = new double[50];
        int size=0;
        ArrayList<String> findBestStatesResult = new ArrayList<String>();
        
        try{
           _conn = DatabaseConnection.getConnection();
           s = _conn.createStatement();

           rs = s.executeQuery(     
                "SELECT occupationcode FROM job WHERE ssn=" + ssn);
           if (!rs.next())
           {
        	   UtilitiesReq19_24.reportFailure("No rows in ResultSet. Occupation code not found for SSN " + ssn);
        	   return findBestStatesResult;
           }
           int code = rs.getInt(1);
           size = CalculateAverageWageForOccupationForAllStates(code, resultStates, averageWage);
           System.out.println("");
           rs.close();
           s.close();

           findBestStatesResult = FindBestStates(3, resultStates, averageWage, size, " Average weekly wage for the user's occupation: ");
           

       } catch (SQLException e)
          {
    	   UtilitiesReq19_24.printExceptionText(e);
        }
       return findBestStatesResult;
   }
    
    @TaDaMethod(variablesToTrack = {"ssn"}, correspondingDatabaseAttribute = {"job.ssn"} )
   public int CalculateAverageWageForOccupationForAllStates(int occupationCode, String[] resultStates, double[] averageWage){

         ResultSet rs = null;
         int ssn = 0;
         String zip = "";
         String state = "";
         int[] resultCount = new int[50];
         int stateIndex=0;
         int size =0;
         HashMap<String, String> zipToStateMap = new HashMap<String, String>();
         HashMap<Integer, String> ssnToZipMap = new HashMap<Integer, String>();
         HashMap<Integer, Integer> ssnToWeekWageMap = new HashMap<Integer, Integer>();

         initializeSsnToWeekWageMap(ssnToWeekWageMap);
         
         initializeSsnToZipMap(ssnToZipMap);
      
         initializeZipToStateMap(zipToStateMap);

       try{
         _conn = DatabaseConnection.getConnection();
         s = _conn.createStatement();
         
         rs = s.executeQuery("SELECT ssn FROM job WHERE occupationcode ="+occupationCode);
         if (!rs.next())
         {
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. No SSN for occupation code: " + occupationCode);
                return 0;
         }

         do{
          //System.out.println("State: " + rs1.getString(1));
          ssn = rs.getInt(1);
          
          zip = ssnToZipMap.get(ssn);
          if(zip==null)
             continue;
          state = zipToStateMap.get(zip);
          if(state==null)
             continue;
          stateIndex = getStateIndex(resultStates, state);
          if( stateIndex < 50){
            resultCount[stateIndex]++;
            averageWage[stateIndex]+= ssnToWeekWageMap.get(ssn);
          }
          else{
            resultCount[size]++;
            resultStates[size] = state;
            averageWage[size]+= ssnToWeekWageMap.get(ssn);
            size++;
          }                            
         }while(rs.next());
         rs.close();
         s.close();
      } catch (SQLException e)
        {
    	  UtilitiesReq19_24.printExceptionText(e);
      }
      for(int i=0; i<size; i++){
              averageWage[i] = averageWage[i]/resultCount[i]; 
              averageWage[i] = Factory.getRoundMethod(averageWage[i]);
     
      }
      return size;
   }

// ############################## COMPUTE AVERAGE EDUCATION ###################

   public String ComputeAverageEducationString(String selection, int code)
   {
	String education = "";
	int level;
	
	level = ComputeAverageEducation(selection,code);
        
        
	if(level != ErrorValue){
                HashMap<Integer, String> levelToEducationMap = new HashMap<Integer, String>();

       		initializeLevelToEducationHashMap(levelToEducationMap);

		education = levelToEducationMap.get((int)level);
    		System.out.println("Average education level for "+ selection + " code " + code + ": " + education);
    		System.out.println("");
    		System.out.println("");
   	}
    	else{
    		System.out.println("Error when calculating average education level for "+ selection + " code " + code);
    		System.out.println("");
    		System.out.println("");	
    	}
	return(education);
   }
   
    @TaDaMethod(variablesToTrack = {"ssn"}, correspondingDatabaseAttribute = {"job.ssn"} )
   public int ComputeAverageEducation(String selection, int code)
   {
	String education = "";
        double averageLevel = 0;
        double total =0;
        double count = 0;
        int ssn=0;
        ResultSet rs = null;
        int level =0;
        HashMap<Integer, String> ssnToEducationMap = new HashMap<Integer, String>();
        HashMap<String, Integer> educationTolevelsMap = new HashMap<String, Integer>();
        HashMap<Integer, String> levelToEducationMap = new HashMap<Integer, String>();

        initializeLevelToEducationHashMap(levelToEducationMap);
        initializeEducationToLevelHashMap(educationTolevelsMap);
	initializeSsnToEducationMap(ssnToEducationMap);
        
        try {  
             _conn = DatabaseConnection.getConnection();
             s = _conn.createStatement();

             rs = s.executeQuery(     
              "SELECT ssn FROM job WHERE " + selection +"code=" + code);

             if (!rs.next())
             {
            	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. No SSN found for " + selection + "code " + code);
            	 return ErrorValue;
             }
  
             do{
                ssn = rs.getInt(1);
                if(ssnToEducationMap.get(ssn)!=null){
                  education = ssnToEducationMap.get(ssn);
                  if(educationTolevelsMap.get(education)!=null){
                   level =  educationTolevelsMap.get(education);
                   if(level !=0){
                     total+= level;
                     count ++;
                   }
                  }
                }           
             }while(rs.next());
             rs.close();
             s.close(); 
           } catch (SQLException e)
           {
        	   UtilitiesReq19_24.printExceptionText(e);
        }
        averageLevel = total/count;
        return (int)averageLevel;
    }

// ############################## FIND OCCUPATION/INDUSTRY WITH HIGHEST/LOWES EDUCATION ###################

    //@TaDaMethod(variablesToTrack = {"codeList", "categoryList"}, correspondingDatabaseAttribute = {"job.occupationcode"} )
   public ArrayList<String> FindCccupationIndustryWithHighestLowestEducation(String selection)
   {
       String maxCategory = "";
       String minCategory = "";
       int level = 0;
       int maxCode = 0;
       int minCode = 0;
       int maxLevel = 0;
       int minLevel = 15;
       ResultSet rs = null;
       double MaxAverageIncomeBasedOnCode = 0;
       double MinAverageIncomeBasedOnCode = 0;
       int[] codeList = new int[500];
       String[] categoryList= new String[500];
       ArrayList<String> result = new ArrayList<String>();
       HashMap<Integer, String> levelToEducationMap = new HashMap<Integer, String>();

       initializeLevelToEducationHashMap(levelToEducationMap);

       try {
         _conn = DatabaseConnection.getConnection();
    	 s = _conn.createStatement();

         rs = s.executeQuery(     
              "SELECT " + selection + "code," + selection + " FROM " + selection);
         if (!rs.next())
         {
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. Code not found.");
                return result;
         }

        int i = 0;
        do{
          codeList[i] = rs.getInt(1);
          categoryList[i] = rs.getString(2);
          i++;
        }while(rs.next());

         for(int j=0; j<i; j++){

            // 0 code is not valid industry or occupation. Skipp 0
            if( (codeList[j] == 0) || (codeList ==null))
               continue; 

            level = ComputeAverageEducation(selection, codeList[j]);
            if( maxLevel <level){
                  maxLevel = level;
                  maxCode = codeList[j];
                  maxCategory = categoryList[j];
            }

            if(minLevel>level && level>0){
                 minLevel = level;
                 minCode = codeList[j];
                 minCategory = categoryList[j];
            }           
              
         }
         result.add(levelToEducationMap.get(maxLevel));
         System.out.println("The " + selection + " " + maxCategory+ "\n has the highest education level: " + levelToEducationMap.get(maxLevel));
         result.add(levelToEducationMap.get(minLevel)); 
         System.out.println("The " + selection + " " + minCategory + "\n has the lowest education level: " + levelToEducationMap.get(minLevel)); 
         System.out.println("");
         System.out.println("");

         CalculateByOccupationOrIndustryCodeInterface calc = Factory.getCalculateByOccupationOrIndustryCode();
         
         if(selection.equals("industry")){
               MaxAverageIncomeBasedOnCode = calc.calculateIncomeByIndustryCode(maxCode);
               MinAverageIncomeBasedOnCode = calc.calculateIncomeByIndustryCode(minCode);
         }
         else if(selection.equals("occupation")){
               MaxAverageIncomeBasedOnCode = calc.calculateIncomeByOccupationCode(maxCode);
               MinAverageIncomeBasedOnCode = calc.calculateIncomeByOccupationCode(minCode);
         }
         else{
               System.out.println("Invalid option: " +selection);
               return result;
         }

         double ammount = 0;
         ammount = UtilitiesReq19_24.CalculateIncomePercentageDifference(MaxAverageIncomeBasedOnCode, MinAverageIncomeBasedOnCode);
         ammount = Factory.getRoundMethod(ammount);

     
         if(MaxAverageIncomeBasedOnCode == MinAverageIncomeBasedOnCode){ 
        	   result.add("The average income for "+ selection +" with highest education level and lowest education level is equal");
               System.out.println("The average income for "+ selection +" with highest education level and lowest education level is equal"); 
         }else if(MaxAverageIncomeBasedOnCode > MinAverageIncomeBasedOnCode){
        	   result.add("The average income for "+ selection +" with highest education level is " + ammount + "% higher than for " +selection+ " with lowest education level");
               System.out.println("The average income for "+ selection +" with highest education level is " + ammount + "% higher than for " +selection+ " with lowest education level");
         }else{
        	   result.add("The average income for "+ selection +" with highest education level is " + ammount + "% lower than for " +selection+ " with lowest education level");
               System.out.println("The average income for "+ selection +" with highest education level is " + ammount + "% lower than for " +selection+ " with lowest education level");
         }
          System.out.println("");
          rs.close();
          s.close();
          
        } catch (SQLException e)
         {
        	UtilitiesReq19_24.printExceptionText(e);
        }
        return result;
   }
// #########################CALCULATE LIKELINESS TO MOVE FACTOR ######################


   @TaDaMethod(variablesToTrack = {"code", "wage", "zip", "state"}, correspondingDatabaseAttribute = {"job.occupationcode", "job.weekwage", "userrecord.zip", "ziptable.statename"} )
  public String CalculateLikelinessToMoveFactor(int ssn){
     
      ResultSet rs = null;
      ResultSet rs1 = null;
      ResultSet rs2 = null;
      ResultSet rs3 = null;
      String[] resultStates= new String[50];
      double[] averageWage = new double[50];
      int size=0;
      String state="";
      double wage =0;
      int betterStatesCount=0;
      String returnValue="";

      try{
    	  System.out.println("flow:14");
         _conn = DatabaseConnection.getConnection();
         s = _conn.createStatement();

         rs = s.executeQuery(     
              "SELECT occupationcode FROM job WHERE ssn=" + ssn);
         if (!rs.next())
         {
        	 System.out.println("flow:15");
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. Occupation code not found for SSN " + ssn);
                return ("Error");
         }
         int code = rs.getInt(1);

         
         System.out.println("");

         rs1 = s.executeQuery(     
              "SELECT weekwage FROM job WHERE ssn=" + ssn);
         if (!rs1.next())
         {
        	 System.out.println("flow:16");
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. Occupation code not found for SSN " + ssn);
                return ("Error");
         }
         wage = rs1.getInt(1);

         rs2 = s.executeQuery(     
              "SELECT zip FROM userrecord WHERE ssn=" + ssn);
         if (!rs2.next())
         {
        	 System.out.println("flow:17");
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. Occupation code not found for SSN " + ssn);
                return ("Error");
         }
         String zip = rs2.getString(1);
        
         if (zip==null)
         {
        	 System.out.println("flow:18");
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. Zip not found for SSN " + ssn);
                return ("Error");
         }

         rs3 = s.executeQuery(     
              "SELECT statename FROM ziptable WHERE zip ='" + zip +"'");
         if (!rs3.next())
         {
        	 System.out.println("flow:19");
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. State not found for SSN " + ssn);
                return ("Error");
         }
         state = rs3.getString(1); 

         rs.close();
         rs1.close();
         rs2.close();
         rs3.close();
         s.close(); 

         size = CalculateAverageWageForOccupationForAllStates(code, resultStates, averageWage);       

         for(int i=0; i<size; i++){
        	 System.out.println("flow:20");
           if((wage < averageWage[i]) && !(state.equals(resultStates[i]))){
        	   System.out.println("flow:21");
              betterStatesCount++;
           }
         } 
         
         if(betterStatesCount > size/2){
        	 System.out.println("flow:22");
           System.out.println("The likeliness to move factor is high as more than 50% of states have better average wage for this person's occupation");
           returnValue= "high";
         }
         else{
        	 System.out.println("flow:23");
           System.out.println("The likeliness to move factor is low as more than 50% of states have lower or equal average wage for this person's occupation");
           returnValue= "low";
         }                 

     } catch (SQLException e)
        {
    	 System.out.println("flow:24");
    	 UtilitiesReq19_24.printExceptionText(e);
      }
        return returnValue;
    }


// ############################## INITIALIZATION ###################################

 

   private void initializeLevelToEducationHashMap(HashMap<Integer, String> levelToEducationMap){

    	String[] resultSet = new String[maxEduDegrees];
        int i =0;
        getEducationResultSet(resultSet);

        
        do{
                levelToEducationMap.put(i, resultSet[i]);
    		i++;
        }while(i<maxEduDegrees);  
        
        
   }


      private void initializeEducationToLevelHashMap(HashMap<String, Integer> educationTolevelsMap){

    	String[] resultSet = new String[maxEduDegrees];
        int i =0;
        getEducationResultSet(resultSet);
             
        do{
                educationTolevelsMap.put(resultSet[i], i);
		i++;
        }while(i<maxEduDegrees);  
      
   }
   
      @TaDaMethod(variablesToTrack = {"a", "b"}, correspondingDatabaseAttribute = {"ssn.education", "education.education"} )
      private void initializeSsnToEducationMap(HashMap<Integer, String> ssnToEducationMap){

        ResultSet rs = null;

        try {
        	_conn = DatabaseConnection.getConnection();
             	s = _conn.createStatement();
     		rs = s.executeQuery("SELECT ssn, education FROM education");
        	if (!rs.next())
       		{
            	 	UtilitiesReq19_24.reportFailure("No rows in ResultSet. No SSN found for table education");
                	return;
        	}
             
        	do{
        		int a = rs.getInt(1);
        		String b = rs.getString(2);
                	ssnToEducationMap.put(a, b);
        	}while(rs.next());

		rs.close();
        	s.close();
        } catch (SQLException e)
          {
        	UtilitiesReq19_24.printExceptionText(e);
        }
   }

      @TaDaMethod(variablesToTrack = {"a", "b"}, correspondingDatabaseAttribute = {"ssn.userrecord", "zip.userrecord"} )
   private void initializeSsnToZipMap(HashMap<Integer, String> ssnToZipMap){

       ResultSet rs = null;
       try{
         _conn = DatabaseConnection.getConnection();
         s = _conn.createStatement();
 
         rs = s.executeQuery("SELECT ssn, zip FROM userrecord");
         if (!rs.next())
         {
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. No SSN, zip found for table userrecord");
                return;
         }

         do{
        	 int a = rs.getInt(1);
     		 String b = rs.getString(2);
             ssnToZipMap.put(a, b);
         }while(rs.next());
         rs.close();
         s.close();
       } catch (SQLException e)
          {
    	   UtilitiesReq19_24.printExceptionText(e);
       }
   }

      @TaDaMethod(variablesToTrack = {"a", "b"}, correspondingDatabaseAttribute = {"ssn.job", "weekwage,job"} )   
   private void initializeSsnToWeekWageMap(HashMap<Integer, Integer> ssnToWeekWageMap){
       ResultSet rs = null;
       try{
         _conn = DatabaseConnection.getConnection();
         s = _conn.createStatement();

         rs = s.executeQuery("SELECT ssn, weekwage FROM job");
         if (!rs.next())
         {
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. No SSN, weekwage found for table job");
                return;
         }
             
         do{
        	 int a = rs.getInt(1);
     		 int b = rs.getInt(2);
             ssnToWeekWageMap.put(a, b);
         }while(rs.next());
         rs.close(); 
         s.close();
       } catch (SQLException e)
          {
    	   UtilitiesReq19_24.printExceptionText(e);
       }
   }

      @TaDaMethod(variablesToTrack = {"a", "b"}, correspondingDatabaseAttribute = {"zip.ziptable", "statename.ziptable"} )
   private void initializeZipToStateMap(HashMap<String, String> zipToStateMap){
       ResultSet rs = null;
       try{
         _conn = DatabaseConnection.getConnection();
         s = _conn.createStatement();

         rs = s.executeQuery("SELECT zip, statename FROM ziptable");
         if (!rs.next())
         {
        	 UtilitiesReq19_24.reportFailure("No rows in ResultSet. No zip, statename found for table job");
                return;
         }
             
         do{
        	 String a = rs.getString(1);
     		 String b = rs.getString(2);
                zipToStateMap.put(a, b);
         }while(rs.next());
         rs.close();
         s.close();
       } catch (SQLException e)
          {
    	   UtilitiesReq19_24.printExceptionText(e);
       }

   }

}
