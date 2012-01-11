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

import com.mnxfst.testing.exception.TSPlanMissingException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanBuilder;
import com.mnxfst.testing.plan.TSPlanExecutorResult;
import com.mnxfst.testing.plan.exec.TSPlanExecutor;

/**
 * Test cases for {@link TSPlanExecutor}
 * @author mnxfst
 * @since 16.12.2011
 */
public class TestTSPlanExecutor {

	@Test
	public void testExecute() throws Exception {
		
		try {
			new TSPlanExecutor(null, null, null, 10, TSPlanRecurrenceType.TIMES).call();
			Assert.fail("No valid test plan defined");
		} catch(TSPlanMissingException e) {
			//
		}
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
		TSPlanExecutorResult result = new TSPlanExecutor(plan, "junit", "exec-1", 1, TSPlanRecurrenceType.TIMES).call();
		Assert.assertNotNull("The result must not be null", result);
		Assert.assertTrue("The execution time must be greater than 5ms", 5 <= result.getDurationMillis());
		
		
		
		

		
	}
	
}
