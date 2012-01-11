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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Test case for {@link RandomCtxVarGenActivity}
 * @author ckreutzfeldt
 * @since 11.01.2012
 */
public class TestRandomCtxVarGenActivity {

	@Test
	public void testPostInit() throws TSPlanActivityExecutionException {

		try {
			new RandomCtxVarGenActivity().postInit();
			Assert.fail("No config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		RandomCtxVarGenActivity activity = new RandomCtxVarGenActivity();
		activity.setName("junit-test");
		activity.setConfiguration(new TSPlanConfigOption());

		try {
			activity.postInit();
			Assert.fail("No config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("no-such-context-gen", "test");
		activity.setConfiguration(cfgOpt);
		
		try {
			activity.postInit();
			Assert.fail("No proper configuration provided");
			Assert.fail("No config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generate.variable.arg0", "unknown-class");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Unknown generator class");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generate.variable.arg0", RandomIntValueGenerator.class.getName());
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Missing required configuration settings for random integer generator");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generate.variable.arg0.minValue", "1");
		cfgOpt.addOption("generate.variable.arg0.maxValue", "10");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("No generator config contained");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generate.variable.arg0.class", RandomIntValueGenerator.class.getName());
		cfgOpt.addOption("generate.variable.arg0.minValue", "1");
		cfgOpt.addOption("generate.variable.arg0.maxValue", "10");
		activity.setConfiguration(cfgOpt);
		activity.postInit();

		Map<String, Serializable> ctx = activity.execute(new HashMap<String, Serializable>());
		Assert.assertNotNull("The context must not be null", ctx);
		Assert.assertEquals("The context must contain one element", 1, ctx.size());
		Assert.assertTrue("The value of arg0 must be available", (ctx.get("arg0") != null));

	}
	
}
