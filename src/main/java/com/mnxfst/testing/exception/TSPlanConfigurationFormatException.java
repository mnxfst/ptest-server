package com.mnxfst.testing.exception;

import com.mnxfst.testing.plan.TSPlan;

/**
 * Thrown in case the {@link TSPlan} configuration could not be read
 * @author mnxfst
 *
 */
public class TSPlanConfigurationFormatException extends Exception {

	private static final long serialVersionUID = 553079579146496014L;


	public TSPlanConfigurationFormatException() {		
	}
	
	public TSPlanConfigurationFormatException(String msg) {
		super(msg);
	}
	
	public TSPlanConfigurationFormatException(Throwable cause) {
		super(cause);
	}
	
	public TSPlanConfigurationFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
