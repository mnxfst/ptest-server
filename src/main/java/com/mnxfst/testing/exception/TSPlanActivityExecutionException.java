package com.mnxfst.testing.exception;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;


/**
 * Thrown in case the execution of an {@link AbstractTSPlanActivity} failed 
 * @author mnxfst
 * @since 20.12.2011
 */
public class TSPlanActivityExecutionException extends Exception {

	private static final long serialVersionUID = -7603339871596720266L;

	public TSPlanActivityExecutionException() {		
	}
	
	public TSPlanActivityExecutionException(String msg) {
		super(msg);
	}
	
	public TSPlanActivityExecutionException(Throwable cause) {
		super(cause);
	}
	
	public TSPlanActivityExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
