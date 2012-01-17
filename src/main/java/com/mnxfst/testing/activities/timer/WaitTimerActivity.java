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
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

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
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

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
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {
		
		try {
			Thread.sleep(waitTime);
		} catch(InterruptedException e) {
			logger.error(WaitTimerActivity.class.getName() + " interrupted. Exception: "  + e.getMessage(), e);
		}
		
		ctx.addTransientVariable(getContextVariable(), Long.valueOf(waitTime));
		
		return ctx;
	}

}
