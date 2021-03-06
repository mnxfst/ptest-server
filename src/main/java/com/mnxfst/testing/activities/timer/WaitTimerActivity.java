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

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.exception.TSVariableEvaluationFailedException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.ExecutionContextValueType;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Implements a simple timer that halts the {@link TSPlan} execution for a configured time (in millis). The wait time will be written
 * to the provided context using the configured variable. If the provided wait time is less than 0 it will be set to be 0. 
 * @author mnxfst
 *
 */
public class WaitTimerActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(WaitTimerActivity.class);
	
	private static final String CTX_EXPORT_WAITTIME_VARIABLE= "waitTime";
	
	private boolean fetchWaitTimeFromContext = false;
	private String waitTimeCtxPattern = null;
	private long waitTime = 0;
	private String contextExportVariableName = null;

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Failed to initialize activity '" + WaitTimerActivity.class.getName() + "' due to missing configuration options");

		String str = (String)cfgOpt.getOption("waitTime");
		if(str == null || str.isEmpty())
			throw new TSPlanActivityExecutionException("Failed to initialize activity '" + WaitTimerActivity.class.getName() + "' due to a missing wait time configuration");
		
		if((str.startsWith("${global.") || str.startsWith("${run.")) && str.endsWith("}")) {
			this.waitTimeCtxPattern = str.trim();
			this.fetchWaitTimeFromContext = true;
		}
			
		if(!fetchWaitTimeFromContext) {
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
		
		if(getContextExportVariables() != null) {
			this.contextExportVariableName = getContextExportVariables().get(CTX_EXPORT_WAITTIME_VARIABLE);
		}
		
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(java.util.Map)
	 */
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {
		
		try {			
			if(fetchWaitTimeFromContext)
				this.waitTime = (Long)ctx.evaluate(this.waitTimeCtxPattern);
		} catch(TSVariableEvaluationFailedException e) {
			throw new TSPlanActivityExecutionException("Failed to evaluate pattern '"+this.waitTimeCtxPattern+"' in order to set wait time");
		} catch(ClassCastException e) {
			throw new TSPlanActivityExecutionException("Expected to find a " + Long.class.getName() + " value while evaluating '"+this.waitTimeCtxPattern+"'. Error: " + e.getMessage());
		}
		
		if(waitTime < 0)
			waitTime = 0;
		
		try {
			Thread.sleep(waitTime);
		} catch(InterruptedException e) {
			logger.error(WaitTimerActivity.class.getName() + " interrupted. Exception: "  + e.getMessage(), e);
		}
		
		ctx.addContextValue(contextExportVariableName, Long.valueOf(waitTime), ExecutionContextValueType.RUN);
		
		return ctx;
	}

	/**
	 * @return the fetchWaitTimeFromContext
	 */
	public boolean isFetchWaitTimeFromContext() {
		return fetchWaitTimeFromContext;
	}

	/**
	 * @return the waitTimeCtxPattern
	 */
	public String getWaitTimeCtxPattern() {
		return waitTimeCtxPattern;
	}

	/**
	 * @return the waitTime
	 */
	public long getWaitTime() {
		return waitTime;
	}

	/**
	 * @return the contextExportVariableName
	 */
	public String getContextExportVariableName() {
		return contextExportVariableName;
	}

}
