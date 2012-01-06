package com.mnxfst.testing.activities.jms;

import java.io.Serializable;
import java.util.Map;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;

/**
 * Activity implementation for sending requests to a jms destination 
 * @author ckreutzfeldt
 *
 */
public class JMSDestinationRequestActivity extends AbstractTSPlanActivity {

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#postInit()
	 */
	public void postInit() throws TSPlanActivityExecutionException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(java.util.Map)
	 */
	public Map<String, Serializable> execute(Map<String, Serializable> input)
			throws TSPlanActivityExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

}
