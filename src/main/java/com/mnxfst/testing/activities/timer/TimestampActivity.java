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

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext;

/**
 * Creates or updates a time stamp stored in the variable defined through <i>variable</i>. 
 * @author mnxfst
 * @since 20.12.2011
 */
public class TimestampActivity extends AbstractTSPlanActivity {

	private static final String CTX_EXPORT_TIMESTAMP_VARIABLE= "timestamp";
	
	private String contextExportVariableName = null;
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {
		
		if(getContextExportVariables() != null) {
			this.contextExportVariableName = getContextExportVariables().get(CTX_EXPORT_TIMESTAMP_VARIABLE);
		}

	}

	public ITSPlanExecutionContext execute(ITSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {

		if(ctx == null)
			throw new TSPlanActivityExecutionException("Missing required activity context!");
		
		ctx.addTransientVariable(contextExportVariableName, Long.valueOf(System.currentTimeMillis()));

		return ctx;
	}

}
