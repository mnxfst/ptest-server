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

package com.mnxfst.testing.activities.log;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.ExecutionContextValueType;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Test case for {@link ContextLog4jActivity}
 * @author mnxfst
 *
 */
public class TestContextLog4jActivity {

	@Test
	public void testPostInit() throws TSPlanActivityExecutionException {
		
		ContextLog4jActivity activity = new ContextLog4jActivity();
		activity.setName("test-activity");
		
		try {
			activity.initialize(null);
			Assert.fail("No valid config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();

		try {
			activity.initialize(cfgOpt);
			Assert.fail("Config options missing conversion pattern");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("conversionPattern", "%d [%t] %-5p %c - %m%n");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Log message missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("logMessage", "[start=${initTimestamp}, end=${finalTimestamp}]");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Log appender missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logAppender", null);
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Invalid log appender");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logAppender", "no such appender");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Unknown log appender");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logAppender", "file");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Unsupported log appende rtyp 'FILE'");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("logAppender", "console");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Config options missing log level");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("logLevel", null);
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Config options missing log level");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logLevel", "no such type");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Invalid log level");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logLevel", "warn");
		activity.initialize(cfgOpt);
	}

	@Test
	public void testExecute() throws TSPlanActivityExecutionException {

		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("logAppender", "console");
		cfgOpt.addOption("logLevel", "warn");
		cfgOpt.addOption("conversionPattern", "%d [%t] %-5p %c - %m%n");
		cfgOpt.addOption("logMessage", "[start=${run.initTimestamp}, end=${run.finalTimestamp}]");

		ContextLog4jActivity activity = new ContextLog4jActivity();
		activity.setName("test-activity");
		
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("log4jMsg", "ctxLog4jMsg");
		activity.setContextExportVariables(vars);
		activity.initialize(cfgOpt);
		
		try {
			activity.execute(null);
			Assert.fail("Context missing");
		} catch(TSPlanActivityExecutionException e) {
			
		}
		
		TSPlanExecutionContext ctx = new TSPlanExecutionContext();
		activity.execute(ctx);
		
		ctx.addContextValue("initTimestamp", Long.valueOf(1234), ExecutionContextValueType.RUN);
		ctx.addContextValue("finalTimestamp", "5678", ExecutionContextValueType.RUN);
		ctx = activity.execute(ctx);
		Assert.assertNotNull("The result must not be null", ctx);
		Assert.assertEquals("The log message must be equal to '[start=1234, end=5678'", "[start=1234, end=5678]", ctx.getContextValue("ctxLog4jMsg", ExecutionContextValueType.RUN));
	}
	
	@Test
	public void testGetContextVariablesFromString() {
		
		String logPattern = "[start=${initTimestamp}, end=${finalTimestamp}]";
		Map<String, String> vars = (new ContextLog4jActivity()).getContextVariablesFromString(logPattern);
		Assert.assertNotNull("The result map must not be null", vars);
		Assert.assertEquals("The result must contain 2 elements", 2, vars.size());
		
		Assert.assertEquals("The variable pattern must be '\\$\\{initTimestamp\\}'", "\\$\\{initTimestamp\\}", vars.get("${initTimestamp}"));
		Assert.assertEquals("The variable pattern must be '\\$\\{finalTimestamp\\}'", "\\$\\{finalTimestamp\\}", vars.get("${finalTimestamp}"));
		Assert.assertNull("No such variable pattern", vars.get("\\$\\{FINALTIMESTAMP\\}"));
	}
	
}
