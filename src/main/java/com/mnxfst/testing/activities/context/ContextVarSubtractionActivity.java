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

package com.mnxfst.testing.activities.context;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext;

/**
 * This activity takes two numerical context values and subtracts them from each other  
 * @author ckreutzfeldt
 * @since 11.01.2012
 */
public class ContextVarSubtractionActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(ContextVarSubtractionActivity.class);
	
	private static final String CTX_EXPORT_SUBTRACTION_RESULT_VARIABLE = "subtractionResult";
	
	private String leftHandVariable = null;
	private String rightHandVariable = null;
	private String contextExportVariableName = null;
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Required configuration options missing for activity '"+getName()+"'");
		
		this.leftHandVariable = (String)cfgOpt.getOption("leftHandVariable");
		this.rightHandVariable = (String)cfgOpt.getOption("rightHandVariable");
		
		if(leftHandVariable == null || leftHandVariable.isEmpty())
			throw new TSPlanActivityExecutionException("Failed to get variable to read left hand value from");

		if(rightHandVariable == null || rightHandVariable.isEmpty())
			throw new TSPlanActivityExecutionException("Failed to get variable to read right hand value from");

		if(getContextExportVariables() != null) {
			this.contextExportVariableName = getContextExportVariables().get(CTX_EXPORT_SUBTRACTION_RESULT_VARIABLE);
		}

		
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext)
	 */
	public ITSPlanExecutionContext execute(ITSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {
		
		if(ctx == null)
			throw new TSPlanActivityExecutionException("Required activity context missing");
		
		Long left = (Long)ctx.getTransientVariable(leftHandVariable);
		Long right = (Long)ctx.getTransientVariable(rightHandVariable);

		if(left == null)
			throw new TSPlanActivityExecutionException("Failed to read left hand value from context varibale '"+left+"'");
		if(right == null)
			throw new TSPlanActivityExecutionException("Failed to read right hand value from context varibale '"+right+"'");

		if(logger.isDebugEnabled())
			logger.debug("subtraction["+left+" - "+right+" = " + (left-right)+"]");
		
		ctx.addTransientVariable(contextExportVariableName, (left-right));
		
		return ctx;
	}
}
