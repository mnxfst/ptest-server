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

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.ExecutionContextValueType;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Test case for {@link ContextVarDecrementActivity}
 * @author ckreutzfeldt
 * @since 01.02.2012 
 */
public class TestContextVarDecrementActivity {

	@Test
	public void testInitialize() throws TSPlanActivityExecutionException {
		
		ContextVarDecrementActivity activity = new ContextVarDecrementActivity();
		
		try {
			activity.initialize(null);
			Assert.fail("Invalid config options");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		try {
			activity.initialize(new TSPlanConfigOption());
			Assert.fail("Missing required value");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("contextvariable", "test");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Config option name must be case sensitive");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("contextVariable", "test");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Config option missing require variable type");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("contextVariableType", "test");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Invalid context variable type");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("contextVariableType", "run");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Missing decrement value");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("decrementValue", "test");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Invalid decrement value");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("decrementValue", "10");
		activity.initialize(cfgOpt);
		Assert.assertEquals("The context variable name must be test", "test", activity.getContextVariable());
		Assert.assertEquals("The context variable type must be run", ExecutionContextValueType.RUN, activity.getContextVariableType());
		Assert.assertEquals("The decrement value must be 10", 10, activity.getDecrementValue());
		
		cfgOpt.addOption("contextVariableType", "global");
		activity.initialize(cfgOpt);
		Assert.assertEquals("The context variable name must be test", "test", activity.getContextVariable());
		Assert.assertEquals("The context variable type must be global", ExecutionContextValueType.GLOBAL, activity.getContextVariableType());
		Assert.assertEquals("The decrement value must be 10", 10, activity.getDecrementValue());
	}
	
	@Test
	public void testExecute() throws TSPlanActivityExecutionException {

		ContextVarDecrementActivity activity = new ContextVarDecrementActivity();
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("contextVariable", "test");
		cfgOpt.addOption("contextVariableType", "run");
		cfgOpt.addOption("decrementValue", "10");
		activity.initialize(cfgOpt);
		
		try {
			activity.execute(null);
			Assert.fail("Invalid context");
		} catch(NullPointerException e) {
			//
		}

		try {
			activity.execute(new TSPlanExecutionContext());
			Assert.fail("Variable missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanExecutionContext ctx = new TSPlanExecutionContext();
		ctx.addContextValue("test", "value", ExecutionContextValueType.RUN);
		try {
			activity.execute(ctx);
			Assert.fail("Variable type invalid");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		ctx.addContextValue("test", (long)20, ExecutionContextValueType.GLOBAL);
		try {
			activity.execute(ctx);
			Assert.fail("Variable not contained in right context");
		} catch(TSPlanActivityExecutionException e) {
			//		
		}

		ctx.addContextValue("test", (long)20, ExecutionContextValueType.RUN);
		Assert.assertEquals("The context value must be 20", Long.valueOf(20), (Long)ctx.getContextValue("test", ExecutionContextValueType.RUN));
		activity.execute(ctx);
		Assert.assertEquals("The context value must be 10", Long.valueOf(10), (Long)ctx.getContextValue("test", ExecutionContextValueType.RUN));

	}
}
