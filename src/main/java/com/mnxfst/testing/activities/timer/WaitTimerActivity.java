package com.mnxfst.testing.activities.timer;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Implements a simple timer that halts the {@link TSPlan} execution for a configured time (in millis). The wait time will be written
 * to the provided context using the configured variable. If the provided wait time is less than 0 it will be set to be 0. 
 * @author mnxfst
 *
 */
public class WaitTimerActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(WaitTimerActivity.class);
	
	private long waitTime = 0;

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#postInit()
	 */
	public void postInit() throws TSPlanActivityExecutionException {

		// fetch options and transfer them to inner variables
		TSPlanConfigOption cfgOpt = getConfiguration();
		
		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Failed to initialize activity '" + WaitTimerActivity.class.getName() + "' due to missing configuration options");

		String str = (String)cfgOpt.getOption("waitTime");
		if(str == null || str.isEmpty())
			throw new TSPlanActivityExecutionException("Failed to initialize activity '" + WaitTimerActivity.class.getName() + "' due to a missing wait time configuration");
		
		try {
			waitTime = Long.parseLong(str);
		} catch(NumberFormatException e) {
			throw new TSPlanActivityExecutionException("Failed to initialize activity '" + WaitTimerActivity.class.getName() + "' due to an invalid value ('"+str+"') being passed to the waitTime configuration");
		}
		
		if(waitTime < 0) {
			waitTime = 0;
			
			if(logger.isDebugEnabled()) 
				logger.debug("Received wait time value is less than 0 ('"+waitTime+"'). Resetting wait time to 0");
		}
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(java.util.Map)
	 */
	public Map<String, Serializable> execute(Map<String, Serializable> input) throws TSPlanActivityExecutionException {
		
		try {
			Thread.sleep(waitTime);
		} catch(InterruptedException e) {
			logger.error(WaitTimerActivity.class.getName() + " interrupted. Exception: "  + e.getMessage(), e);
		}
		
		input.put(getContextVariable(), Long.valueOf(waitTime));
		
		return input;
	}

}
