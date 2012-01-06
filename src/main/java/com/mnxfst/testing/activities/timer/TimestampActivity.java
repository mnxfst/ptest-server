package com.mnxfst.testing.activities.timer;

import java.io.Serializable;
import java.util.Map;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;

/**
 * Creates or updates a time stamp stored in the variable defined through <i>variable</i>. 
 * @author mnxfst
 * @since 20.12.2011
 */
public class TimestampActivity extends AbstractTSPlanActivity {

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(java.util.Map)
	 */
	public Map<String, Serializable> execute(Map<String, Serializable> input) throws TSPlanActivityExecutionException {

		if(input == null)
			throw new TSPlanActivityExecutionException("Missing required input!");
		
		input.put(getContextVariable(), Long.valueOf(System.currentTimeMillis()));

		return input;
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#postInit()
	 */
	public void postInit() throws TSPlanActivityExecutionException {
	}
}
