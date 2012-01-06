package com.mnxfst.testing.exception;

import com.mnxfst.testing.plan.TSPlan;


/**
 * Thrown in case the execution of an {@link TSPlan} failed due to
 * a general or unknown reason
 * @author mnxfst
 *
 */
public class TSPlanExecutionFailedException extends Exception {

	private static final long serialVersionUID = -4825237744035123502L;

	public TSPlanExecutionFailedException() {		
	}
	
	public TSPlanExecutionFailedException(String msg) {
		super(msg);
	}
	
	public TSPlanExecutionFailedException(Throwable cause) {
		super(cause);
	}
	
	public TSPlanExecutionFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
