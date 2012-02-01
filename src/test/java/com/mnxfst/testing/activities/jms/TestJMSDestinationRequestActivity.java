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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.Describable;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Test case for {@link JMSDestinationRequestActivity}
 * @author ckreutzfeldt
 * @since 01.02.2012
 */
public class TestJMSDestinationRequestActivity {

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
		cfgOpt.addOption("connectiontype", "test");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Missing required connection type option");
		}  catch(TSPlanActivityExecutionException e) {
			//
		}
		cfgOpt.addOption("connectionType", "test");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Invalid connection type");
		}  catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("connectionType", "queue");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Destination name missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("destinationName", "esptopic");
		activity.initialize(cfgOpt);

		cfgOpt.addOption("connectionType", "topic");
		activity.initialize(cfgOpt);
		for(int i = 0; i < 100; i++) 
			activity.execute(null);
		activity.shutdown();
	}
}
