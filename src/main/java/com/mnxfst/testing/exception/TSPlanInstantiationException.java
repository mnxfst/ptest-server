package com.mnxfst.testing.exception;

import com.mnxfst.testing.plan.TSPlan;


/**
 * Thrown in case the instantiation of a {@link TSPlan} failed
 * @author mnxfst
 *
 */
public class TSPlanInstantiationException extends Exception {

	private static final long serialVersionUID = -3823797908252268085L;

	public TSPlanInstantiationException() {		
	}
	
	public TSPlanInstantiationException(String msg) {
		super(msg);
	}
	
	public TSPlanInstantiationException(Throwable cause) {
		super(cause);
	}
	
	public TSPlanInstantiationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
