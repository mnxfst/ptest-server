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

/**
 * Test case for {@link TSPlanBuilder}
 * @author mnxfst
 *
 */
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
		
//		TSPlanConfigOption singleOption = plan.getConfigurationOption("rndCtxValGenCfg");		
//		Assert.assertNotNull("The result must not be null", singleOption);
//		Assert.assertEquals("The options must be named 'rndCtxValGenCfg'", "rndCtxValGenCfg", singleOption.getName());
//		Assert.assertEquals("The value of generate.variable.arg0 must be integer", "integer", singleOption.getOption("generate.variable.arg0"));
//		Assert.assertEquals("The value of generate.variable.arg1 must be string", "string", singleOption.getOption("generate.variable.arg1"));
//		Assert.assertEquals("The value of generate.variable.arg2 must be boolean", "boolean", singleOption.getOption("generate.variable.arg2"));
//		Assert.assertEquals("The value of generate.variable.arg3 must be date", "date", singleOption.getOption("generate.variable.arg3"));
		
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
		Assert.assertEquals("The result must contain 3 config options", 1, options.size());
		Assert.assertNotNull("There must be an option for 'rndCtxValGenCfg'", options.get("rndCtxValGenCfg"));
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
