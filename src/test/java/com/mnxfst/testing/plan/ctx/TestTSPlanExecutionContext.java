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
	
}
