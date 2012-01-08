/*
 *  ptest-server and client provides you with a performance test utility
 *  Copyright (C) 2012  Christian Kreutzfeldt <mnxfst@googlemail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.mnxfst.testing.activities.timer;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
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
