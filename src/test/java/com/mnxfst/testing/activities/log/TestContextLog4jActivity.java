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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

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
			activity.postInit();
			Assert.fail("No valid config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		activity.setConfiguration(cfgOpt);

		try {
			activity.postInit();
			Assert.fail("Config options missing conversion pattern");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("conversionPattern", "%d [%t] %-5p %c - %m%n");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Log message missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("logMessage", "[start=${initTimestamp}, end=${finalTimestamp}]");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Log appender missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logAppender", null);
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Invalid log appender");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logAppender", "no such appender");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Unknown log appender");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logAppender", "file");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Unsupported log appende rtyp 'FILE'");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("logAppender", "console");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Config options missing log level");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("logLevel", null);
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Config options missing log level");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logLevel", "no such type");
		activity.setConfiguration(cfgOpt);
		try {
			activity.postInit();
			Assert.fail("Invalid log level");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("logLevel", "warn");
		activity.setConfiguration(cfgOpt);
		activity.postInit();		
	}

	@Test
	public void testExecute() throws TSPlanActivityExecutionException {

		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("logAppender", "console");
		cfgOpt.addOption("logLevel", "warn");
		cfgOpt.addOption("conversionPattern", "%d [%t] %-5p %c - %m%n");
		cfgOpt.addOption("logMessage", "[start=${initTimestamp}, end=${finalTimestamp}]");

		ContextLog4jActivity activity = new ContextLog4jActivity();
		activity.setName("test-activity");
		activity.setConfiguration(cfgOpt);
		activity.setContextVariable("ctxLog4jMsg");
		activity.postInit();		
		
		try {
			activity.execute(null);
			Assert.fail("Context missing");
		} catch(TSPlanActivityExecutionException e) {
			
		}
		
		Map<String, Serializable> ctx = new HashMap<String, Serializable>();
		activity.execute(ctx);
		
		ctx.put("initTimestamp", Long.valueOf(1234));
		ctx.put("finalTimestamp", "5678");
		ctx = activity.execute(ctx);
		Assert.assertNotNull("The result must not be null", ctx);
		Assert.assertEquals("The log message must be equal to '[start=1234, end=5678'", "[start=1234, end=5678]", ctx.get("ctxLog4jMsg"));
	}
	
	@Test
	public void testGetContextVariablesFromString() {
		
		String logPattern = "[start=${initTimestamp}, end=${finalTimestamp}]";
		Map<String, String> vars = (new ContextLog4jActivity()).getContextVariablesFromString(logPattern);
		Assert.assertNotNull("The result map must not be null", vars);
		Assert.assertEquals("The result must contain 2 elements", 2, vars.size());		
		Assert.assertEquals("The variable pattern must be '\\$\\{initTimestamp\\}'", "\\$\\{initTimestamp\\}", vars.get("initTimestamp"));
		Assert.assertEquals("The variable pattern must be '\\$\\{finalTimestamp\\}'", "\\$\\{finalTimestamp\\}", vars.get("finalTimestamp"));
		Assert.assertNull("No such variable pattern", vars.get("\\$\\{FINALTIMESTAMP\\}"));
	}
	
}
