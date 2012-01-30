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

import com.mnxfst.testing.activities.context.random.RandomCtxVarGenActivity;
import com.mnxfst.testing.activities.context.random.RandomIntValueGenerator;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.ExecutionContextValueType;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Test case for {@link RandomCtxVarGenActivity}
 * @author ckreutzfeldt
 * @since 11.01.2012
 */
public class TestRandomCtxVarGenActivity {

	@Test
	public void testPostInit() throws TSPlanActivityExecutionException {

		try {
			new RandomCtxVarGenActivity().initialize(null);
			Assert.fail("No config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		RandomCtxVarGenActivity activity = new RandomCtxVarGenActivity();
		activity.setName("junit-test");		

		try {
			activity.initialize(new TSPlanConfigOption());
			Assert.fail("No config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("no-such-context-gen", "test");		
		
		try {
			activity.initialize(cfgOpt);
			Assert.fail("No proper configuration provided");
			Assert.fail("No config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generate.variable.arg0", "unknown-class");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Unknown generator class");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generate.variable.arg0", RandomIntValueGenerator.class.getName());
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Missing required configuration settings for random integer generator");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generate.variable.arg0.minValue", "1");
		cfgOpt.addOption("generate.variable.arg0.maxValue", "10");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("No generator config contained");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generate.variable.arg0.class", RandomIntValueGenerator.class.getName());
		cfgOpt.addOption("generate.variable.arg0.minValue", "1");
		cfgOpt.addOption("generate.variable.arg0.maxValue", "10");
		activity.initialize(cfgOpt);

		TSPlanExecutionContext ctx = activity.execute(new TSPlanExecutionContext());
		Assert.assertNotNull("The context must not be null", ctx);
		Assert.assertEquals("The context must contain one element", 1, ctx.getContextValueNames(ExecutionContextValueType.RUN).size());
		Assert.assertTrue("The value of arg0 must be available", (ctx.getContextValue("arg0", ExecutionContextValueType.RUN) != null));

	}
	
}
