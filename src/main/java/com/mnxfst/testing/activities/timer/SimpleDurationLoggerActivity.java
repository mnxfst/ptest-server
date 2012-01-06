package com.mnxfst.testing.activities.timer;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.activities.TSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

public class SimpleDurationLoggerActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(SimpleDurationLoggerActivity.class);
	
	private String startTimestampVariable = null;
	private String endTimestampVariable = null;
		
	public void postInit() throws TSPlanActivityExecutionException {

		// fetch options and transfer them to inner variables
		TSPlanConfigOption cfgOpt = getConfiguration();

		startTimestampVariable = (String)cfgOpt.getOption("startTimestampVariable");
		endTimestampVariable = (String)cfgOpt.getOption("endTimestampVariable");
		
		if(startTimestampVariable == null || startTimestampVariable.isEmpty())
			throw new TSPlanActivityExecutionException("Failed to get variable to read start timestamp from");
		
		if(endTimestampVariable == null || endTimestampVariable.isEmpty())
			throw new TSPlanActivityExecutionException("Failed to get variable to read end timestamp from");

	}

	public Map<String, Serializable> execute(Map<String, Serializable> input) throws TSPlanActivityExecutionException {
		
		Long startTimestamp = (Long)input.get(startTimestampVariable);
		Long endTimestamp = (Long)input.get(endTimestampVariable);

		if(startTimestamp == null)
			throw new TSPlanActivityExecutionException("Failed to read start timestamp from context varibale '"+startTimestampVariable+"'");
		if(endTimestamp == null)
			throw new TSPlanActivityExecutionException("Failed to read start timestamp from context varibale '"+endTimestampVariable+"'");
		
		logger.debug("Duration: " + (endTimestamp.longValue()-startTimestamp.longValue()) + "ms");
		
		return input;
	}
}
