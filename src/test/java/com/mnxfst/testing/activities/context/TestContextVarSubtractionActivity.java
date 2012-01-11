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
 * Test case for {@link ContextVarSubtractionActivity}
 * @author mnxfst
 *
 */
public class TestContextVarSubtractionActivity {

	@Test
	public void testPostInit() throws TSPlanActivityExecutionException {
		
		try {
			new ContextVarSubtractionActivity().postInit();
			Assert.fail("Config options missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		ContextVarSubtractionActivity activity = new ContextVarSubtractionActivity();
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Required left hand options missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("leftHandVariable", "left");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Required right hand options missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("rightHandVariable", "right");
		activity.setConfiguration(cfgOpt);
		activity.postInit();
	}
	
	@Test
	public void testExecute() throws TSPlanActivityExecutionException {

		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		ContextVarSubtractionActivity activity = new ContextVarSubtractionActivity();
		activity.setContextVariable("result");
		cfgOpt.addOption("leftHandVariable", "left");
		cfgOpt.addOption("rightHandVariable", "right");
		activity.setConfiguration(cfgOpt);
		activity.postInit();
		
		try {
			activity.execute(null);
			Assert.fail("No context provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		try {
			activity.execute(new HashMap<String, Serializable>());
			Assert.fail("Left hand variable missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		Map<String, Serializable> ctx = new HashMap<String, Serializable>();
		ctx.put("left", Long.valueOf(2));
		try {
			activity.execute(ctx);
			Assert.fail("Right hand variable missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
			
		ctx.put("right", Long.valueOf(3));
		ctx = activity.execute(ctx);
		Assert.assertNotNull("The context must not be null", ctx);
		Assert.assertEquals("The number of variables within the context must be 3", 3, ctx.size());
		Assert.assertEquals("The left hand variable value must be 2", Long.valueOf(2), (Long)ctx.get("left"));
		Assert.assertEquals("The left hand variable value must be 3", Long.valueOf(3), (Long)ctx.get("right"));
		Assert.assertEquals("The subtraction result must be -1", Long.valueOf(-1), (Long)ctx.get("result"));
	}
	
}
