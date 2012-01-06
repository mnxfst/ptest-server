package com.mnxfst.testing.exception;

/**
 * Thrown in case a method requires a test plan which has not been provided
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSPlanMissingException extends Exception {

	private static final long serialVersionUID = 4124402489855120746L;

	public TSPlanMissingException() {		
	}
	
	public TSPlanMissingException(String msg) {
		super(msg);
	}
	
	public TSPlanMissingException(Throwable cause) {
		super(cause);
	}
	
	public TSPlanMissingException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	
}
