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

package com.mnxfst.testing.plan;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.mnxfst.testing.activities.TSPlanActivity;
import com.mnxfst.testing.activities.http.HTTPRequestActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.exception.TSPlanConfigurationFormatException;
import com.mnxfst.testing.exception.TSPlanInstantiationException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

public class TestTSPlanBuilder {

	@Test
	public void testPopulate() throws SAXException, IOException, ParserConfigurationException, TSPlanConfigurationFormatException, TSPlanInstantiationException, TSPlanActivityExecutionException {
		
		try {
			TSPlanBuilder.getInstance().buildPlan(null);
			Assert.fail("Invalid configuration");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		ByteArrayInputStream bin = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test></test>".getBytes());
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Unexpected root node");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ptestplan></ptestplan>".getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("No child nodes found");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}

		bin = new ByteArrayInputStream(new String ("<ptestplan><name></name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>mnxfst</createdBy></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Name missing");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description></description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>mnxfst</createdBy></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Description missing");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate></creationDate><createdBy>mnxfst</createdBy></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Creation date empty");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<createdBy>mnxfst</createdBy></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Creation date missing");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>11.12-2011</creationDate><createdBy>mnxfst</createdBy></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Creation date invalid");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy></createdBy></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Created by missing");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>me</createdBy></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Number of exec environments missing");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>me</createdBy><numberOfExecutionEnvironments/></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Number of exec environments missing");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>me</createdBy><numberOfExecutionEnvironments>2</numberOfExecutionEnvironments></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Number of threads per exec environments missing");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>me</createdBy><numberOfExecutionEnvironments>2</numberOfExecutionEnvironments><numberOfThreadPerExecEnv/></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Number of threads per exec environments missing");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>me</createdBy><numberOfExecutionEnvironments>2</numberOfExecutionEnvironments><numberOfThreadPerExecEnv>j</numberOfThreadPerExecEnv></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Invalid value for threads per exec environments");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>me</createdBy><numberOfExecutionEnvironments>2</numberOfExecutionEnvironments><numberOfThreadPerExecEnv>4</numberOfThreadPerExecEnv></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Invalid number of recurrences for recurrences type");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>me</createdBy><numberOfExecutionEnvironments>2</numberOfExecutionEnvironments><numberOfThreadPerExecEnv>4</numberOfThreadPerExecEnv><recurrences>infinite</recurrences></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("Init activity element missing");
		} catch(TSPlanConfigurationFormatException e) {
			// 
		}
		
		bin = new ByteArrayInputStream(new String ("<ptestplan><name>testName</name><description>sample description</description>"+
												   "<creationDate>2011-12-20</creationDate><createdBy>me</createdBy><numberOfExecutionEnvironments>2</numberOfExecutionEnvironments><numberOfThreadPerExecEnv>4</numberOfThreadPerExecEnv><recurrences>10s</recurrences><initActivity>test</initActivity></ptestplan>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			
			TSPlanBuilder.getInstance().buildPlan(doc);
			Assert.fail("No init activity found for name test");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
		
		Assert.assertNotNull("The plan must not be null", plan);
		
		TSPlanConfigOption singleOption = plan.getConfigurationOption("cassandraStats");		
		Assert.assertNotNull("The result must not be null", singleOption);
		Assert.assertEquals("The options must be named 'cassandraStats'", "cassandraStats", singleOption.getName());
		Assert.assertEquals("The url must be localhost", "localhost", singleOption.getOption("url"));
		Assert.assertEquals("The port must be 9160", "9160", singleOption.getOption("port"));
		Assert.assertEquals("The user must be testUser", "testUser", singleOption.getOption("user"));	
		Assert.assertEquals("The password must be {SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd", "{SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd", singleOption.getOption("password"));
		Assert.assertEquals("The keyspace must be stats", "stats", singleOption.getOption("keyspace"));
		Assert.assertNull("The empty attribute value must be null", singleOption.getOption("empty"));
		Assert.assertEquals("The size of the options set must be 5", 5, singleOption.getOptions().size());

		Assert.assertNotNull("A config set must exist for 'smtpServer'", plan.getConfigurationOption("smtpServer"));

		HTTPRequestActivity httpReqActivity = (HTTPRequestActivity)plan.getActivity("httpRequestIn");
		Assert.assertNotNull("The request activity must not be null", httpReqActivity);
		Assert.assertEquals("The proxy url must be 'proxy.mnxfst.com'", "proxy.mnxfst.com", httpReqActivity.getConfiguration().getOption("proxyUrl"));
		Assert.assertEquals("The proxy port must be '4321'", "4321", httpReqActivity.getConfiguration().getOption("proxyPort"));
		Assert.assertEquals("The proxy user must be 'testUser'", "testUser", httpReqActivity.getConfiguration().getOption("proxyUser"));
		Assert.assertEquals("The proxy password must be '{SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd'", "{SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd", httpReqActivity.getConfiguration().getOption("proxyPassword"));
		Assert.assertEquals("The testKey param has the value testValue", "testValue", httpReqActivity.getConfiguration().getOption("request.param.testKey"));
		Assert.assertEquals("The anotherKey param has the value anotherValue", "anotherValue", httpReqActivity.getConfiguration().getOption("request.param.anotherKey"));
		
		
	}
	
	@Test
	public void testParseSimpleTextNode() throws SAXException, IOException, ParserConfigurationException {
		
		ByteArrayInputStream bin = new ByteArrayInputStream("<test>sample</test>".getBytes());
		Document simpleDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
						
		Assert.assertEquals("The value must be sample", "sample", TSPlanBuilder.getInstance().parseSimpleTextNode(simpleDoc.getFirstChild()));
	}
	
	@Test
	public void testParseConfigOptions() throws TSPlanConfigurationFormatException, SAXException, IOException, ParserConfigurationException {

		TSPlanBuilder builder = TSPlanBuilder.getInstance();

		ByteArrayInputStream bin = new ByteArrayInputStream(new String ("<ptestplan></ptestplan>").getBytes());
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		Map<String, TSPlanConfigOption> options = builder.parseGlobalConfigurationOptions(doc);
		Assert.assertNotNull("The result must not be null", options);
		Assert.assertTrue("The result must be empty", options.isEmpty());
		
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		
		options = builder.parseGlobalConfigurationOptions(doc);
		Assert.assertNotNull("The result must not be null", options);
		Assert.assertEquals("The result must contain 3 config options", 5, options.size());
		Assert.assertNotNull("There must be an option for 'cassandraStats'", options.get("cassandraStats"));
		Assert.assertNotNull("There must be an option for 'httpRequestConfig'", options.get("httpRequestConfig"));
		Assert.assertNotNull("There must be an option for 'smtpServer'", options.get("smtpServer"));
		Assert.assertNotNull("There must be an option for 'soapRequestConfig'", options.get("soapRequestConfig"));
		Assert.assertNull("There must not be an option for 'doesNotExist'", options.get("doesNotExist"));
		
	}
	
	@Test
	public void testParseActivities() throws TSPlanConfigurationFormatException, SAXException, IOException, ParserConfigurationException {
		
		TSPlanBuilder builder = TSPlanBuilder.getInstance();
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");
		Map<String, TSPlanConfigOption> opt = builder.parseGlobalConfigurationOptions(doc);
		builder.parseActivities(doc, opt);
		
	}
	
	@Test
	public void testGetActivityParserInstance() throws TSPlanInstantiationException {
		TSPlanBuilder builder = TSPlanBuilder.getInstance();
		try {
			builder.getActivityParserInstance(String.class.getName());
			Assert.fail("Invalid class");
		} catch(TSPlanInstantiationException e) {			
		}
		
		TSPlanActivity activity = builder.getActivityParserInstance(HTTPRequestActivity.class.getName());
		Assert.assertNotNull("The activity object must not be null", activity);

	}
		
	
}
