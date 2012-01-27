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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSVariableEvaluationFailedException;

/**
 * Test case for {@link TSPlanExecutionContext}
 * @author mnxfst
 * @since 26.01.2012
 */
public class TestTSPlanExecutionContext {

	@Test
	public void testEvaluate() throws TSVariableEvaluationFailedException {
		
		TSPlanExecutionContext ctx = new TSPlanExecutionContext();
		try {
			ctx.evaluate(null, null);
			Assert.fail("Invalid input values");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("", null);
			Assert.fail("Invalid input values");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("test", null);
			Assert.fail("Invalid input values");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("test", "");
			Assert.fail("Invalid input values");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("test", "test");
			Assert.fail("Invalid replacement pattern");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("test", "${global");
			Assert.fail("Invalid replacement pattern");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("test", "${run");
			Assert.fail("Invalid replacement pattern");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}

		try {
			ctx.evaluate("test", "${global.");
			Assert.fail("Invalid replacement pattern");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}

	}
	
	@Test
	public void testExtractGetterMethodNames() {
		
		TSPlanExecutionContext ctx = new TSPlanExecutionContext();
		
		try {
			ctx.extractGetterMethodNames(null,  null);
			Assert.fail("Must throw an NPE");
		} catch(NullPointerException e) {
			//
		}
		
		try {
			ctx.extractGetterMethodNames("",  null);
			Assert.fail("Must throw an NPE");
		} catch(NullPointerException e) {
			//
		}
		
		String[] result = ctx.extractGetterMethodNames("${global.var.}",  "${global.");
		Assert.assertNotNull("The result must not be null", result);
		Assert.assertEquals("The size must be 0", 0, result.length);
		
		result = ctx.extractGetterMethodNames("${global.var.attr1.attr2}",  "${global.");
		System.out.println(result.length);
		for(int i = 0; i < result.length; i++)
			Assert.assertEquals("The getter must be named getAttr"+(i+1), "getAttr"+(i+1), result[i]);
		
		// TODO more testing
	}
	
	@Test
	public void testFindGetterMethodsForNames() throws Exception {
		
		TSPlanExecutionContext ctx = new TSPlanExecutionContext();
		String[] getterMethodNames = ctx.extractGetterMethodNames("${global.var.class.name}",  "${global.");
		List<Method> methods = new ArrayList<Method>();
		Object result = ctx.evaluateObject(new String("100"), getterMethodNames, methods);
		Assert.assertNotNull("The result must not be null", result);
		Assert.assertEquals("The result must be " + String.class.getName(), String.class.getName(), result);
		Assert.assertNotNull("The methods list must not be null", methods);
		Assert.assertEquals("The list of methods must contain 2 elements", 2, methods.size());
		
		result = ctx.evaluateObject(new String("100"), methods);
		Assert.assertNotNull("The result must not be null", result);
		Assert.assertEquals("The result must be " + String.class.getName(), String.class.getName(), result);
	}
	
}
