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

package com.mnxfst.testing.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.mnxfst.testing.exception.TSClientExecutionException;

/**
 * Test case for {@link TSClientPlanResultCollectCallable}
 * @author ckreutzfeldt
 * @since 13.02.2012
 */
public class TestTSClientPlanResultCollectCallable {

	@Test
	public void testParseResultIdentifer() throws TSClientExecutionException, SAXException, IOException, ParserConfigurationException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		StringBuffer result = new StringBuffer("<testExecutionResponse><resultIdentifier>7789eb70-562d-11e1-87b8-0022fad0126c</resultIdentifier><responseCode>2</responseCode><testplan>PlaceOrder Testplan without timeouts</testplan><executionEnvironment>ptest-server</executionEnvironment><start>1329128907687</start><end>1329128907796</end><averageDuration>109</averageDuration><singleMinDuration>110</singleMinDuration><singleMaxDuration>111</singleMaxDuration><singleAverageDuration>112</singleAverageDuration><errors>0</errors><averageDurationMedian>9.2</averageDurationMedian></testExecutionResponse>");
		ByteArrayInputStream bin = new ByteArrayInputStream(result.toString().getBytes());
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);

		TSClientPlanResultCollectCallable c = new TSClientPlanResultCollectCallable("", 1, "");
		
		// both cases are being tested in another callable test as well so we do not implement all 
		Assert.assertEquals("The result must be 2", 2, c.parseIntValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_RESPONSE_CODE, xpath));
		Assert.assertEquals("The result identifier must be 7789eb70-562d-11e1-87b8-0022fad0126c", "7789eb70-562d-11e1-87b8-0022fad0126c", c.parseStringValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_RESULT_IDENTIFIER, xpath));
		Assert.assertEquals("The result must be 'PlaceOrder Testplan without timeouts'", "PlaceOrder Testplan without timeouts", c.parseStringValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_TEST_PLAN, xpath));
		Assert.assertEquals("The result must be 'ptest-server'", "ptest-server", c.parseStringValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_ENVIRONMENT, xpath));
		Assert.assertEquals("The result must be '1329128907687'", 1329128907687L, c.parseLongValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_START, xpath));
		Assert.assertEquals("The result must be '1329128907796'", 1329128907796L, c.parseLongValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_END, xpath));
		Assert.assertEquals("The result must be '109'", 109L, c.parseLongValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_AVERAGE_DURATION, xpath));
		Assert.assertEquals("The result must be '110'", 110L, c.parseLongValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_SINGLE_MIN_DURATION, xpath));
		Assert.assertEquals("The result must be '111'", 111L, c.parseLongValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_SINGLE_MAX_DURATION, xpath));
		Assert.assertEquals("The result must be '112'", 112L, c.parseLongValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_SINGLE_AVERAGE_DURATION, xpath));
		Assert.assertEquals("The result must be '9.2'", 9.2f, c.parseDoubleValue(doc.getFirstChild(), TSClientPlanResultCollectCallable.TEST_EXEC_AVERAGE_MEDIAN, xpath), 0.1);
	}
	
}
