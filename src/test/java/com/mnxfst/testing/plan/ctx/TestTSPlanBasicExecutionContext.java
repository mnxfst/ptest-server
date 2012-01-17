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

package com.mnxfst.testing.plan.ctx;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test case for {@link TSPlanBasicExecutionContext}
 * @author mnxfst
 * @since 17.01.2012
 */
public class TestTSPlanBasicExecutionContext {

	@Test
	public void testTransientVariables() {
		
		TSPlanBasicExecutionContext ctx = new TSPlanBasicExecutionContext();
		Assert.assertNotNull("The set of variable names must not be null", ctx.getTransientVariableNames());
		Assert.assertTrue("The set of transient variable names must be empty", ctx.getTransientVariableNames().isEmpty());
		Assert.assertNull("No such key exists", ctx.getTransientVariable("no-such-key"));
		
		// add variable - check for variable names list size, value existence
		ctx.addTransientVariable("test-var", "test-value");
		Assert.assertFalse("The set of transient variable names must not be empty", ctx.getTransientVariableNames().isEmpty());
		Assert.assertEquals("The size of the set of transient variable names must be 1", 1, ctx.getTransientVariableNames().size());
		Assert.assertEquals("The value for test-var must be test-value", "test-value", ctx.getTransientVariable("test-var"));
		
		// clear transient variables
		ctx.refreshTransientVariables();
		Assert.assertNotNull("The set of variable names must not be null", ctx.getTransientVariableNames());
		Assert.assertTrue("The set of transient variable names must be empty", ctx.getTransientVariableNames().isEmpty());
		Assert.assertNull("No such key exists", ctx.getTransientVariable("no-such-key"));
	}
	
	@Test
	public void testDurableVariables() {

		
		TSPlanBasicExecutionContext ctx = new TSPlanBasicExecutionContext();
		Assert.assertNotNull("The set of variable names must not be null", ctx.getDurableVariableNames());
		Assert.assertTrue("The set of transient variable names must be empty", ctx.getDurableVariableNames().isEmpty());
		Assert.assertNull("No such key exists", ctx.getDurableVariable("no-such-key"));
		
		// add variable - check for variable names list size, value existence
		ctx.addDurableVariable("test-var", "test-value");
		Assert.assertFalse("The set of durable variable names must not be empty", ctx.getDurableVariableNames().isEmpty());
		Assert.assertEquals("The size of the set of durable variable names must be 1", 1, ctx.getDurableVariableNames().size());
		Assert.assertEquals("The value for test-var must be test-value", "test-value", ctx.getDurableVariable("test-var"));

	}
	
}
