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

package com.mnxfst.testing.plan.exec;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;

import com.mnxfst.testing.exception.TSPlanInstantiationException;
import com.mnxfst.testing.exception.TSPlanMissingException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanBuilder;
import com.mnxfst.testing.plan.TSPlanExecEnvironmentResult;

/**
 * Test cases for {@link TSPlanExecEnvironment}
 * @author mnxfst
 * @since 16.12.2011
 */
public class TestTSPlanExecEnvironment {

	@Test
	public void testExecute() throws Exception {
		
		try {
			new TSPlanExecEnvironment(null,  null, -1, null, -1);
			Assert.fail("Mssing env identifier");
		} catch(TSPlanInstantiationException e) {
			//
		}
		
		try {
			new TSPlanExecEnvironment("test-env", null, 2, TSPlanRecurrenceType.TIMES, 2);
			Assert.fail("Missing test plan class");
		} catch(TSPlanMissingException e) {
			//
		}		

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);

		try {
			new TSPlanExecEnvironment("test-env", plan, 0, TSPlanRecurrenceType.TIMES, -1);
			Assert.fail("Invalid number of recurrences");
		} catch(TSPlanInstantiationException e) {
			//
		}		

		try {
			new TSPlanExecEnvironment("test-env", plan, 1, null, -1);
			Assert.fail("Unknown recurrence type");
		} catch(TSPlanInstantiationException e) {
			//
		}		

		try {
			new TSPlanExecEnvironment("test-env", plan, 1, TSPlanRecurrenceType.UNKNOWN, -1);
			Assert.fail("Unknown recurrence type");
		} catch(TSPlanInstantiationException e) {
			//
		}		

		try {
			new TSPlanExecEnvironment("test-env", plan, 1, TSPlanRecurrenceType.TIMES, 0);
			Assert.fail("Invalid number of threads");
		} catch(TSPlanInstantiationException e) {
			//
		}		

		TSPlanExecEnvironment env = new TSPlanExecEnvironment("test-env", plan, 10, TSPlanRecurrenceType.TIMES, 2);

		TSPlanExecEnvironmentResult results = env.execute();
		Assert.assertNotNull("The result set must not be null", results);
		
		// TODO valid test  for result

	}
	
	
	@Test
	public void testParallelPlanExecution() throws Exception {
		
		// fetch test plan and instantiate corresponding entity
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);

		// instantiate environment and execute tests
		TSPlanExecEnvironment env = new TSPlanExecEnvironment("env-1", plan, 5, TSPlanRecurrenceType.TIMES, 8);
		TSPlanExecEnvironmentResult result = env.execute();

		Assert.assertNotNull("The result must not be null", result);
		// TODO valid test  for result

	}
}
