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

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.exception.TSPlanConfigurationFormatException;
import com.mnxfst.testing.exception.TSPlanExecutionFailedException;
import com.mnxfst.testing.exception.TSPlanInstantiationException;
import com.mnxfst.testing.exception.TSPlanMissingException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanBuilder;
import com.mnxfst.testing.plan.TSPlanExecEnvironmentResult;

/**
 * Implements a basic saturation test which automatically increments the number of
 * reiterations and threads according to the results in order to find the saturation
 * load for this system 
 * @author mnxfst
 * @since 13.01.2011
 */
public class TestSaturationExec {

	@Test
	public void doTest() throws SAXException, IOException, ParserConfigurationException, TSPlanConfigurationFormatException, TSPlanInstantiationException, TSPlanActivityExecutionException, TSPlanMissingException, TSPlanExecutionFailedException {
		
		
		
		
		int threads = 1;
		int recurrences = 1;
		int maxRuntime = 21;
		int warmupRuns = 5;
		
		boolean interrupt = false;
		// fetch test plan and instantiate corresponding entity
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);

		// instantiate environment and execute tests
		TSPlanExecEnvironment env = null;
		TSPlanExecEnvironmentResult result = null;
		
		for(threads = 1; threads <= 8;  threads++) {
			System.out.println("--------------------------------------------------------------------");
			System.out.println("Threads: " + threads);
			for(recurrences = 1; recurrences <= 30; recurrences++) {
				env = new TSPlanExecEnvironment("env-"+threads+"-"+recurrences, plan, recurrences, TSPlanRecurrenceType.TIMES, threads);
				result = env.execute();
				System.out.println("\tThreads: " + threads + ", recurrences: " + recurrences + ", overallAvg: " + result.getAverageDurationMillis() + ", avgDuration: " + result.getSingleRunExecutionDurationAverage() + ", min: " + result.getSingleRunExecutionDurationMin() + ", max: " + result.getSingleRunExecutionDurationMax());
				warmupRuns = warmupRuns - 1;
				
				if(warmupRuns <= 0 && result.getSingleRunExecutionDurationMax() >= maxRuntime) {
					interrupt = true;
					break;
				}
			}
			if(interrupt)
				break;
		}
		
		
	}
	
}