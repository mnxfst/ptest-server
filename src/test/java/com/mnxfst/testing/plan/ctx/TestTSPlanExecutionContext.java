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

import java.io.Serializable;
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
			ctx.evaluate(null);
			Assert.fail("Invalid input values");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("");
			Assert.fail("Invalid input values");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("test");
			Assert.fail("Invalid input values");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("${global");
			Assert.fail("Invalid replacement pattern");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
		try {
			ctx.evaluate("${run");
			Assert.fail("Invalid replacement pattern");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}

		try {
			ctx.evaluate("${global.");
			Assert.fail("Invalid replacement pattern");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}

		try {
			ctx.evaluate("${run.");
			Assert.fail("Invalid replacement pattern");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}

		// no such context variable
		Assert.assertNull(ctx.evaluate("${global.}"));
		Assert.assertNull(ctx.evaluate("${global.test.class.name}"));
		Assert.assertNull(ctx.evaluate("${global.test.class.name}"));
		ctx.addContextValue("test", "test-string", ExecutionContextValueType.GLOBAL);
		
		Assert.assertTrue("The pattern mapping cache must be empty", ctx.getReplacementPatternMapping().isEmpty());
		long start = System.currentTimeMillis();
		Assert.assertEquals("The value must be " + String.class.getName(), String.class.getName(), ctx.evaluate("${global.test.class.name}"));		
		long end = System.currentTimeMillis();
		long duration = (end-start);
		Assert.assertFalse("The pattern mapping cache must not be empty", ctx.getReplacementPatternMapping().isEmpty());
		Assert.assertNotNull("There must be an element for ${global.test.class.name}", ctx.getReplacementPatternMapping().get("${global.test.class.name}"));
		
		start = System.currentTimeMillis();
		Assert.assertEquals("The value must be " + String.class.getName(), String.class.getName(), ctx.evaluate("${global.test.class.name}"));
		end = System.currentTimeMillis();
		long cachedResultDuration = (end-start);
		Assert.assertTrue("The duration for the first run must be either equal or larger than the second value", (duration >= cachedResultDuration));

		//		ctx.addContextValue("test", "testvalue", ExecutionContextValueType.GLOBAL);
//		Assert.assertNotNull(ctx.evaluate("test", "${global.}"));
//		Assert.assertEquals("The result must be 'testvalue'", "testvalue", ctx.evaluate("test", "${global.}"));
//		Assert.assertEquals("The result must be 'testvalue'", "testvalue", ctx.evaluate("test", "${global.test}"));
		
		
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
		for(int i = 0; i < result.length; i++)
			Assert.assertEquals("The getter must be named getAttr"+(i+1), "getAttr"+(i+1), result[i]);
		
		// TODO more testing
	}
	
	@Test
	public void testExtractContextVariableName() {
		TSPlanExecutionContext ctx = new TSPlanExecutionContext();
		
		Assert.assertNotNull("The result must not be null", ctx.extractContextVariableName("${global.variable.test}", "${global."));		
		Assert.assertEquals("The result must be 'variable'", "variable", ctx.extractContextVariableName("${global.variable.test}", "${global."));		
		Assert.assertEquals("The result must be 'variable'", "variable", ctx.extractContextVariableName("${global.variable}", "${global."));		

	}
	
	@Test
	public void testExtractGetterMethods() throws TSVariableEvaluationFailedException {
		
		TSPlanExecutionContext ctx = new TSPlanExecutionContext();
		List<Method> methods = new ArrayList<Method>();
		Serializable type = new String("test");
		ctx.extractGetterMethods(type.getClass(), new String[]{"getClass","getName"}, methods);
		methods.clear();
		
		ctx.extractGetterMethods(null, null,  methods);
		Assert.assertNotNull("The methods list not null", methods);
		Assert.assertTrue("The methods list is empty", methods.isEmpty());
		
		ctx.extractGetterMethods(String.class, null,  methods);
		Assert.assertNotNull("The methods list not null", methods);
		Assert.assertTrue("The methods list is empty", methods.isEmpty());
		
		ctx.extractGetterMethods(String.class, new String[0],  methods);
		Assert.assertNotNull("The methods list not null", methods);
		Assert.assertTrue("The methods list is empty", methods.isEmpty());
		
		try {
			ctx.extractGetterMethods(String.class, new String[]{"nosuchmethod"},  methods);
			Assert.fail("No such method");
		} catch(TSVariableEvaluationFailedException e) {
			//
		}
	}
	
//	@Test
//	public void testFindGetterMethodsForNames() throws TSVariableEvaluationFailedException  {
//		
//		TSPlanExecutionContext ctx = new TSPlanExecutionContext();
//		String[] getterMethodNames = ctx.extractGetterMethodNames("${global.var.class.name}",  "${global.");
//		List<Method> methods = new ArrayList<Method>();
//		Object result = ctx.evaluateObject(new String("100"), getterMethodNames, methods);
//		Assert.assertNotNull("The result must not be null", result);
//		Assert.assertEquals("The result must be " + String.class.getName(), String.class.getName(), result);
//		Assert.assertNotNull("The methods list must not be null", methods);
//		Assert.assertEquals("The list of methods must contain 2 elements", 2, methods.size());
//		
//		result = ctx.evaluateObject(new String("100"), methods);
//		Assert.assertNotNull("The result must not be null", result);
//		Assert.assertEquals("The result must be " + String.class.getName(), String.class.getName(), result);
//	}
	
	
}
