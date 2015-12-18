package com.riskIt.interfaces;

/**
 * CalcImplWithDescriptionInterface.java
 * Purpose: Interface for implementation classes to
 * 	allow calculation classes to use dynamic binding 
 * 	and state to determine how they will run.  Specialization
 * 	needed to deal with occupation and industry code values.
 * 
 * @author Bryan Angone
 * @version 1.0 7-12-09
 */

public interface CalcImplWithDescriptionInterface extends CalcImplInterface {

	public String getDescription();
}
