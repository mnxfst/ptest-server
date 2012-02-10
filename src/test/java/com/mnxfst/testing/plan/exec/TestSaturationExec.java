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
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
		
		for(threads = 1; threads <= 30;  threads++) {
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
	
	
	public void doTestCase1() throws Exception {

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/simpleHttpRequestTest.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
		
		boolean interrupt = false;
		
		int threads = 1;
		int recurrences = 1;
		int maxRuntime = 40;
		int warmupRuns = 5;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss,SSS");

		// instantiate environment and execute tests
		TSPlanExecEnvironment env = null;
		TSPlanExecEnvironmentResult result = null;
		
		Map<String, Serializable> vars = new HashMap<String, Serializable>();
		vars.put("scenarioId", "junitscenario");
		vars.put("productId", "junittest");
		vars.put("runId", "junitrun");
		vars.put("threadCount", String.valueOf(threads));
		vars.put("waitTime", "0");
		vars.put("localhostName", "localhost");
		vars.put("measuringPointId", "TC1");
		vars.put("date", formatter.format(new Date()));
		
		
		
// ${scenarioId}-${productId}-${runId}-${threadCount}-${waitTime}-${uuid} ${localhostName} ${measuringPointId} ${initTimestamp} ${date} ${sampleTestplanExecutionDuration} true true		
		
		for(threads = 1; threads <= 8;  threads++) {

			vars.put("threadCount", String.valueOf(threads));
			vars.put("date", formatter.format(new Date()));

			System.out.println("--------------------------------------------------------------------");
			System.out.println("Threads: " + threads);
			for(recurrences = 1; recurrences <= 30; recurrences++) {
				env = new TSPlanExecEnvironment("env-"+threads+"-"+recurrences, plan, recurrences, TSPlanRecurrenceType.TIMES, threads, vars);
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
	
	public void doTestCase2() throws Exception {
		
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/simpleSOAPHttpRequest.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
		
		boolean interrupt = false;
		
		int threads = 1;
		int recurrences = 1;
		int maxRuntime = 40;
		int warmupRuns = 5;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss,SSS");

		// instantiate environment and execute tests
		TSPlanExecEnvironment env = null;
		TSPlanExecEnvironmentResult result = null;
		
		Map<String, Serializable> vars = new HashMap<String, Serializable>();
		vars.put("scenarioId", "junitscenario");
		vars.put("productId", "junittest");
		vars.put("runId", "junitrun");
		vars.put("threadCount", String.valueOf(threads));
		vars.put("waitTime", "0");
		vars.put("localhostName", "localhost");
		vars.put("measuringPointId", "TC1");
		vars.put("date", formatter.format(new Date()));
		
		
		
// ${scenarioId}-${productId}-${runId}-${threadCount}-${waitTime}-${uuid} ${localhostName} ${measuringPointId} ${initTimestamp} ${date} ${sampleTestplanExecutionDuration} true true		
		
		for(threads = 1; threads <= 1;  threads++) {

			vars.put("threadCount", String.valueOf(threads));
			vars.put("date", formatter.format(new Date()));

			System.out.println("--------------------------------------------------------------------");
			System.out.println("Threads: " + threads);
			for(recurrences = 1; recurrences <= 1; recurrences++) {
				env = new TSPlanExecEnvironment("env-"+threads+"-"+recurrences, plan, recurrences, TSPlanRecurrenceType.TIMES, threads, vars);
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
	
	public void testTestCase3() throws Exception {
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleHTTPTestPlan.xml");
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
		
		TSPlanExecEnvironment env = new TSPlanExecEnvironment("exec-1", plan, 10, TSPlanRecurrenceType.TIMES, 1);
		env.execute();
	}
	
	
	@Test
	public void testCase4() throws Exception {

		
		int threads = 1;
		int recurrences = 1;
		int maxRuntime = 40;
		int warmupRuns = 5;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss,SSS");

//		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("c:/temp/AddressIntTestPlan.xml");
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("c:/temp/placeOrderTestPlan_noTimeouts.xml");
		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
		
		Map<String, Serializable> vars = new HashMap<String, Serializable>();
		vars.put("scenarioId", "001");
		vars.put("productId", "001");
		vars.put("runId", "0001");
		vars.put("threadCount", Integer.valueOf(threads));
		vars.put("waitTime", new Integer(0));
		vars.put("localhostName", "vhost0103");
		vars.put("measuringPointOutId", "TCO");
		vars.put("measuringPointInId", "TCI");		
		
		TSPlanExecEnvironment env = new TSPlanExecEnvironment("exec-1", plan, recurrences, TSPlanRecurrenceType.TIMES, threads, vars);
		TSPlanExecEnvironmentResult e = env.execute();
		
		System.out.println("Test plan: " + e.getTestPlanName());
		System.out.println("Execution environment: " + e.getExecutionEnvironmentId());
		System.out.println("Start: " + new Date(e.getStartMillis()));
		System.out.println("End: " + new Date(e.getEndMillis()));
		System.out.println("Duration: " + (e.getEndMillis() - e.getStartMillis()) + "ms");
		System.out.println("Average duration: " + e.getAverageDurationMillis() + "ms");
		System.out.println("Max. duration (single run): " + e.getSingleRunExecutionDurationMax() + "ms");
		System.out.println("Min. duration (single run): " + e.getSingleRunExecutionDurationMin() + "ms");
		System.out.println("Avg. duration (single run): " + e.getSingleRunExecutionDurationAverage() + "ms");
		System.out.println("Errors: " + e.getErrors());
		
	}
	
	
	public void testExecClient() throws ClientProtocolException, IOException, SAXException, ParserConfigurationException {
		
		HttpGet getMethod = new HttpGet("/?execute=1&threads=4&recurrences=1&recurrencetype=times&testplan=AddressIntTestPlan.xml&scenarioId=sec1");
		HttpHost host = new HttpHost("localhost", 9090);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(host, getMethod);
		InputStream s = response.getEntity().getContent();

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(s);
		s.close();
		Node rootNode = doc.getFirstChild();
		System.out.println(rootNode.getNodeName());
		NodeList childs = rootNode.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++) {
			System.out.println(childs.item(i));
		}
	}
	
	public void testSaturationTest() throws ClientProtocolException, IOException, SAXException, ParserConfigurationException {
		
		
		
	}
	
	@Test
	public void testDummey() {
		System.out.println(new Date((long)1328891265670L) + ": 2012-02-10 16:27:45.670");
		System.out.println(new Date((long)1328891266127L) + ": 2012-02-10 16:27:46.127");
	}
	

}
