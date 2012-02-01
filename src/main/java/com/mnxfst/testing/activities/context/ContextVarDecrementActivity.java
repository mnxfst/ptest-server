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
import com.mnxfst.testing.plan.ctx.ExecutionContextValueType;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Decrements a referenced context value (<b>attention:</b> must be of type {@link Long})
 * @author ckreutzfeldt
 * @since 01.02.2012
 */
public class ContextVarDecrementActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(ContextVarDecrementActivity.class);
	
	/** context variable to decrement */
	private String contextVariable = null;
	/** context variable type, default: run */
	private ExecutionContextValueType contextVariableType = ExecutionContextValueType.RUN;
	/** value to decrement by the referenced variable */
	private long decrementValue = 0;
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {
		
		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Required configuration options missing");
		
		// extract context variable and validate the result
		this.contextVariable = (String)cfgOpt.getOption("contextVariable");
		if(this.contextVariable == null)
			throw new TSPlanActivityExecutionException("Required config option 'contextVariable' missing for activity '"+getName()+"'");
		
		// extract the context variable type, parse it and validate the result
		String tmp = (String)cfgOpt.getOption("contextVariableType");
		if(tmp != null && !tmp.isEmpty()) {
			if(tmp.trim().equalsIgnoreCase("global"))
				this.contextVariableType = ExecutionContextValueType.GLOBAL;
			else if(tmp.trim().equalsIgnoreCase("run"))
				this.contextVariableType = ExecutionContextValueType.RUN;
			else
				throw new TSPlanActivityExecutionException("Invalid context variable type '"+tmp+"' found for activity '"+getName()+"'");
		} else {
			throw new TSPlanActivityExecutionException("Required configuration option 'contextVariableType' missing for activity '"+getName()+"'");
		}
		
		// extract the decrement value, parse it and validate the result
		tmp = (String)cfgOpt.getOption("decrementValue");
		if(tmp != null && !tmp.isEmpty()) {
			try {
				this.decrementValue = Long.parseLong(tmp);
			} catch(NumberFormatException e) {
				throw new TSPlanActivityExecutionException("Invalid decrement value '"+tmp+"' found for activity '"+getName()+"'. Expecting numerical values");
			}
		} else {
			throw new TSPlanActivityExecutionException("Required configuration option 'decrementValue' missing for activity '"+getName()+"'");
		}
		
		if(logger.isDebugEnabled())
			logger.debug("decrementActivity[contextVariable="+contextVariable+", variableType="+contextVariableType+", decrementValue="+decrementValue+"]");
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(com.mnxfst.testing.plan.ctx.TSPlanExecutionContext)
	 */
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {
		
		try {
			Long ctxVar2Decrement = (Long)ctx.getContextValue(this.contextVariable, this.contextVariableType);
			if(ctxVar2Decrement != null) {
				ctx.addContextValue(this.contextVariable, ctxVar2Decrement-decrementValue, this.contextVariableType);
			} else {
				throw new TSPlanActivityExecutionException("Expected to find a " + Long.class.getName() + " value for context variable '"+this.contextVariable+"', but the retrieved object was null");
			}
		} catch(ClassCastException e) {
			throw new TSPlanActivityExecutionException("Expected to find a " + Long.class.getName() + " value for context variable '"+this.contextVariable+"'. Error: " + e.getMessage());
		} 
		
		return ctx;
	}

	public String getContextVariable() {
		return contextVariable;
	}

	public ExecutionContextValueType getContextVariableType() {
		return contextVariableType;
	}

	public long getDecrementValue() {
		return decrementValue;
	}

}
