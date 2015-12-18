package com.riskIt.ui;

import com.riskIt.controller.Requirements19_24;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import com.riskIt.interfaces.CalculateByOccupationOrIndustryCodeInterface;
import com.riskIt.util.Factory;

/**
 * SubMenuChoices19_24
 * Purpose: UI Component for Requirements (main menu options) 19-24.
 * 
 * @author Selma Tiganj
 * @version 1.0 7-13-09
 */

public class SubMenuChoices19_24 {

	private Scanner in;
	private Requirements19_24 RequestManager;
	
	public SubMenuChoices19_24()
	{
		in = new Scanner(System.in);	
		RequestManager = new Requirements19_24();
	}
	
//  ########################### FIND TOP STATES ###################################

    public void FindTopStatesMenu(String selection)
    {
		
    	int choice = 0;
	String getChoice = "";
	String category="";
        
        ArrayList<Integer> code = new ArrayList<Integer>(); // this is needed to have one function for search by code and category 

		do
		{
			System.out.println("\n\n1. Find the top 5 states that have the most workers for given " + selection + " category");
			System.out.println("2. Find the top 5 states that have the most workers for given " + selection + " code");
                        System.out.println("3. Display all " + selection + " codes with associated " + selection + " category names");
                        System.out.println("0. Return to the previous menu");
			System.out.print("Enter your choice : ");
                     try{
			getChoice = in.nextLine();	
			
			System.out.println("");
			
			if(getChoice.length() > 0)
				choice = Integer.parseInt(getChoice);
			else
				choice = 0;
		     }catch(NumberFormatException e){
				System.out.println("Invalid input!!");
                                choice = 0;
	             }	
			switch(choice)
			{
                        case 0:
					break;
			case 1:
					System.out.print("Please enter the " + selection + " category: "); 
                                        if (in.hasNextInt()) {
                                                System.out.println("Invalid Input: " + in.nextLine());
                                        }else{
                    			        category =  in.nextLine() ;                             
                    			        RequestManager.FindTopStatesByCategory(selection, category);
                                        }
					break;
			case 2: 
                                     try{
                  			System.out.print("Please enter the " + selection + " code: "); 
                  			getChoice =  in.nextLine() ; 
                  			if(getChoice.length() > 0)
                	  			code.add(Integer.parseInt(getChoice));
			      		else
			    	  		code.add(0); 
                                                                
                  			RequestManager.FindTopStatesByCode(selection, code);
                                        code.clear();
				     }catch(NumberFormatException e){
				         System.out.println("Invalid input!!");
	                             }
					break;
                        case 3:
                                     if( selection.equals("occupation"))
                                         displayOccupationMap();
                                     else
                                         displayIndustryMap();
                                       break;  
			
			default:
					System.out.println("You chose invalid option. Please try again...");
				break;				 
			
			}
                    
		}while(choice > 0 && choice < 4);
     }	

    
 // #########################RECOMMEND BEST STATE TO WORK ######################

    public void RecommendBestStateToWorkMenu(){

     int ssn = 0;
     String getChoice = "";

     do
     {
    	 System.out.print("Please enter 9 digits user's SSN or enter 0 to return to previous menu: ");
        getChoice = in.nextLine();	
			
        System.out.println("");
	try{		
            if(getChoice.length() == 9 ){                 
        		ssn = Integer.parseInt(getChoice);
        		RequestManager.RecommendBestStateToWork(ssn);
            }
            else if (getChoice.length()>0){
                ssn = Integer.parseInt(getChoice);
                if(ssn!=0)
                 System.out.println("You entered invalid SSN.");
            }
        }catch(NumberFormatException e){
			System.out.println("Invalid input!!");
	}	   

     }while(ssn > 0 );
  }


//############################## COMPUTE AVERAGE EDUCATION ###################

    public void ComputeAverageEducationMenu()
    {
	    String getChoice = "";
	    int choice =0;
	    int code=0;
	    String selection="";

		do
		{
			System.out.println("1. Compute average education level by industry");
			System.out.println("2. Compute average education level by occupation");
                        System.out.println("3. Display all industry codes with associated industry category names");
                        System.out.println("4. Display all occupation codes with associated occupation category names");
            		System.out.println("0. Return to the previous menu");
			System.out.print("Enter your choice : ");
			getChoice = in.nextLine();	
			
			System.out.println("");
		     try{
			if(getChoice.length() > 0)
				choice = Integer.parseInt(getChoice);
			else
				choice = 0;
                     }catch(NumberFormatException e){
				System.out.println("Invalid input!!");
                                choice = 0;
	             } 
			
			switch(choice)
			{
            case 0:
                   break;
			case 1: 
                   		System.out.print("Please enter the industry code:");
			       	getChoice = in.nextLine();                              
			       	System.out.println("");
			       	selection = "industry";
                                try{
			       	 if(getChoice.length() > 0)
			       	   code = Integer.parseInt(getChoice);	                              
                   		 RequestManager.ComputeAverageEducationString(selection,code);
                                }catch(NumberFormatException e){
			             System.out.println("Invalid input!!");
	                        }
			       break;
			case 2: 
                   	       	System.out.print("Please enter the occupation code:");
			       	getChoice = in.nextLine();                              
			       	System.out.println("");
			       	selection = "occupation";
                                try{
			       	  if(getChoice.length() > 0)
				     code = Integer.parseInt(getChoice);	                              
                   		  RequestManager.ComputeAverageEducationString(selection,code);
                                }catch(NumberFormatException e){
			              System.out.println("Invalid input!!");
	                        }
			       break;

                        case 3:
                               displayIndustryMap();
                               break;
                        case 4:
                               displayOccupationMap();
                               break;
			default:
					System.out.println("Invalid option. Please try again...");
				break;					
					 			
			}
            
		}while(choice > 0 && choice<5);
  }	

//############################## FIND OCCUPATION/INDUSTRY WITH HIGHEST/LOWES EDUCATION ###################

    public void FindOccIndustryWithHighestLowestEducationMenu()
    {
	    String getChoice = "";
	    int choice =0;

		do
		{
			System.out.println("\n\n1. Find industries in which workers have the highest and lowest education");
			System.out.println("2. Find occupations in which workers have the highest and lowest education");
            		System.out.println("0. Return to the previous menu");
			System.out.print("Enter your choice : ");
			getChoice = in.nextLine();	
			
			System.out.println("");
			System.out.println("");
                     try{
			if(getChoice.length() > 0)
				choice = Integer.parseInt(getChoice);
			else
				choice = 0;
                     }catch(NumberFormatException e){
			             System.out.println("Invalid input!!");
                                     choice = 0;
	             }
			
			switch(choice)
			{
             	case 0:
                          break;
             	case 1: 	                              
             		RequestManager.FindCccupationIndustryWithHighestLowestEducation("industry");
			       break;
             	case 2: 
             		RequestManager.FindCccupationIndustryWithHighestLowestEducation("occupation");
			       break;
             	default:
             		System.out.println("Invalid option. Please try again...");
				break;					
					 			
			}
		}while(choice > 0 && choice<3);
    }	
    
 // #########################CALCULATE LIKELINESS TO MOVE FACTOR ######################

    public void CalculateLikelinessToMoveFactorMenu(){

     int ssn = 0;
     String getChoice = "";

     do
     {
    	 System.out.print("Please enter 9 digits user's SSN or enter 0 to return to previous menu: ");
        getChoice = in.nextLine();	
			
        System.out.println("");
	try{		
            if(getChoice.length() == 9 ){
        	ssn = Integer.parseInt(getChoice);
        	RequestManager.CalculateLikelinessToMoveFactor(ssn);
            }
            else if (getChoice.length()>0){
                ssn = Integer.parseInt(getChoice);
                if(ssn!=0)
                 System.out.println("You entered invalid SSN.");
            }
        }catch(NumberFormatException e){
			             System.out.println("Invalid input!!");
	}	   

     }while(ssn > 0 );

  } 

 // ############################## Display Helper Methods ######################################

   private void displayOccupationMap(){
           HashMap<Integer,String> occupationMap = null;
           
           // Get Occupation Map 
	   if (occupationMap == null){
	        	occupationMap = Factory.getCalculateByOccupationOrIndustryCode().getOccupationCodeAndNameMap();
	   }
 	   for(int i = 0; i < occupationMap.size(); i ++){
		   String stringToInsert = " : ";
		   if(i < 10){
			   stringToInsert = " " + stringToInsert;
		   }
		   System.out.println("Occupation code " + i + stringToInsert + occupationMap.get(i));
	   }
 	   System.out.println();
    }
	
    private void displayIndustryMap(){
           HashMap<Integer, String> industryMap = null;
          
           // Get Industry Map 
           if (industryMap == null){
	        	industryMap = Factory.getCalculateByOccupationOrIndustryCode().getIndustryCodeAndNameMap();
	   }
 
 	   for(int i = 0; i < industryMap.size(); i ++){
		   String stringToInsert = " : ";
		   if(i < 10){
			   stringToInsert = " " + stringToInsert;
		   }
		   System.out.println("Industry code " + i + stringToInsert + industryMap.get(i));
	   }
 	   System.out.println();
    }   
}    


 