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
import com.mnxfst.testing.plan.ctx.ExecutionContextValueType;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Creates or updates a time stamp stored in the variable defined through <i>variable</i>. 
 * @author mnxfst
 * @since 20.12.2011
 */
public class TimestampActivity extends AbstractTSPlanActivity {

	private static final String CTX_EXPORT_TIMESTAMP_VARIABLE= "timestamp";
	
	private String[] contextExportVariables = null;
	private boolean export = false;
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {
		
		if(getContextExportVariables() != null) {
			String tmp = getContextExportVariables().get(CTX_EXPORT_TIMESTAMP_VARIABLE);
			if(tmp != null && !tmp.isEmpty()) {
				String[] vars = tmp.split(",");
				if(vars != null && vars.length > 0) {
					contextExportVariables = new String[vars.length];
					for(int i = 0; i < vars.length; i++) {
						contextExportVariables[i] = vars[i].trim();
					}
				}
			}			
		}

		
		if(contextExportVariables == null)
			contextExportVariables = new String[0];

		export = (contextExportVariables.length > 0);
	}

	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {

		if(ctx == null)
			throw new TSPlanActivityExecutionException("Missing required activity context!");
		
		if(export) {
			Long timestamp = Long.valueOf(System.currentTimeMillis());
			for(int i = 0; i < contextExportVariables.length; i++)
				ctx.addContextValue(contextExportVariables[i], timestamp, ExecutionContextValueType.RUN);
		}

		return ctx;
	}

}
