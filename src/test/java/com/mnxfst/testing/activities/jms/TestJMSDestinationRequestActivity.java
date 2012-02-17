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

package com.mnxfst.testing.activities.jms;

import java.security.AccessControlException;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Test case for {@link JMSDestinationRequestActivity}
 * @author ckreutzfeldt
 * @since 01.02.2012
 */
public class TestJMSDestinationRequestActivity {

	private static final String JMS_TESTPLAN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ptestplan><name>JMS Testplan</name><description>Sample</description><creationDate>2012-02-18</creationDate><createdBy>mnxfst</createdBy><initActivity>jms</initActivity>"+
	"<activities><activity id=\"jmsMessage\" name=\"jms\">"+
			"<description>Sends a JMS message</description>"+
			"<class>com.mnxfst.testing.activities.jms.JMSDestinationRequestActivity</class>"+
			"<contextExportVars><timestamp>ctxMsg</timestamp></contextExportVars>"+
			"<configuration>"+
			"<destinationName>esptopic</destinationName>"+
			"<jmsPayloadTemplate><![CDATA[<payload>content</payload>]]></jmsPayloadTemplate>"+
			"</configuration>"+
			"<nextActivity>finish</nextActivity>"+
			"</activity></activities></ptestplan>";
	
	@Test
	public void testInitialize() throws TSPlanActivityExecutionException {
		
		JMSDestinationRequestActivity activity = new JMSDestinationRequestActivity();
		
		try {
			activity.initialize(null);
			Assert.fail("Missing required cfg opts");
		}  catch(TSPlanActivityExecutionException e) {
			//
		}
		
		try {
			activity.initialize(new TSPlanConfigOption());
			Assert.fail("Missing required connection type");
		}  catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Connection factory class missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("connectionFactoryClass", "content");		
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Destination name missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
				
		cfgOpt.addOption("destinationName", "espTestTopic");
		
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Payload missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("jmsPayloadTemplate", "content");		
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Connection factory jndi name missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("connectionFactoryLookupName", "content");		
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Missing required provider url missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("providerUrl", "content");		
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Missing required principal missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("principal", "content");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Missing required credentials missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("credentials", "content");		
		cfgOpt.addOption("vendor.config.com.sonicsw.jndi.mfcontext.domain", "dmTestDomain");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Invalid options");
		} catch(TSPlanActivityExecutionException e) {
			// 
		}
	}
}

