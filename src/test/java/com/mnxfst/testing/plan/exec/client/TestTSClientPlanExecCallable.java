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

package com.mnxfst.testing.plan.exec.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.mnxfst.testing.exception.TSClientExecutionException;

/**
 * Test case for {@link TSClientPlanExecCallable}
 * @author ckreutzfeldt
 * @since 13.02.2012
 */
public class TestTSClientPlanExecCallable {

	@Test
	public void testParseResponseCode() throws TSClientExecutionException, SAXException, IOException, ParserConfigurationException {

		XPath xpath = XPathFactory.newInstance().newXPath();
		TSClientPlanExecCallable callable = new TSClientPlanExecCallable("host", 1, "/");

		ByteArrayInputStream bin = new ByteArrayInputStream(new String("<testExecutionResponse></testExecutionResponse>").getBytes());
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			Assert.assertEquals("The response code must be 4", 4, callable.parseResponseCode(doc.getFirstChild(), xpath));
			Assert.fail("No such response code");
		} catch(TSClientExecutionException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String("<testExecutionResponse><responseCodes>4</responseCodes></testExecutionResponse>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			Assert.assertEquals("The response code must be 4", 4, callable.parseResponseCode(doc.getFirstChild(), xpath));
			Assert.fail("No such response code");
		} catch(TSClientExecutionException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String("<testExecutionResponse><responseCode>abc</responseCode></testExecutionResponse>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			Assert.assertEquals("The response code must be 4", 4, callable.parseResponseCode(doc.getFirstChild(), xpath));
			Assert.fail("No such response code");
		} catch(TSClientExecutionException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String("<testExecutionResponse><responseCode>4</responseCode><errorCodes><errorCode>1</errorCode></errorCodes><errorMessage></errorMessage></testExecutionResponse>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		Assert.assertEquals("The response code must be 4", 4, callable.parseResponseCode(doc.getFirstChild(), xpath));
	}

	@Test
	public void testParseResponseIdentifier() throws TSClientExecutionException, SAXException, IOException, ParserConfigurationException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		TSClientPlanExecCallable callable = new TSClientPlanExecCallable("host", 1, "/");

		ByteArrayInputStream bin = new ByteArrayInputStream(new String("<testExecutionResponse></testExecutionResponse>").getBytes());
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			Assert.assertEquals("The result identifier must be 514de9d0-5618-11e1-88d6-0022fad0126c", "514de9d0-5618-11e1-88d6-0022fad0126c", callable.parseResultIdentifier(doc.getFirstChild(), xpath));
			Assert.fail("No such result identifier");
		} catch(TSClientExecutionException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String("<testExecutionResponse><resultIdentifiers>dsad</resultIdentifiers></testExecutionResponse>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			Assert.assertEquals("The result identifier must be 514de9d0-5618-11e1-88d6-0022fad0126c", "514de9d0-5618-11e1-88d6-0022fad0126c", callable.parseResultIdentifier(doc.getFirstChild(), xpath));
			Assert.fail("No such result identifier");
		} catch(TSClientExecutionException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String("<testExecutionResponse><responseCode>1</responseCode><resultIdentifier>514de9d0-5618-11e1-88d6-0022fad0126c</resultIdentifier></testExecutionResponse>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		Assert.assertEquals("The result identifier must be 514de9d0-5618-11e1-88d6-0022fad0126c", "514de9d0-5618-11e1-88d6-0022fad0126c", callable.parseResultIdentifier(doc.getFirstChild(), xpath));
	}

	
	public void testParseErrorCodes() throws SAXException, IOException, ParserConfigurationException, TSClientExecutionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		TSClientPlanExecCallable callable = new TSClientPlanExecCallable("host", 1, "/");

		ByteArrayInputStream bin = new ByteArrayInputStream(new String("<testExecutionResponse></testExecutionResponse>").getBytes());
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		Assert.assertNotNull("The result must not be null", callable.parseErrorCodes(doc.getFirstChild(), xpath));
		
		bin = new ByteArrayInputStream(new String("<testExecutionResponse><responseCode>4</responseCode><errorCodes><errorCode>5</errorCode><errorCode>3</errorCode></errorCodes><errorMessage></errorMessage></testExecutionResponse>").getBytes());
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		List<Long> results = callable.parseErrorCodes(doc.getFirstChild(), xpath);
		Assert.assertNotNull("The result must not be null", results);
		Assert.assertEquals("The result must contain 2 elements", 2, results.size());
		

		
	}
}
