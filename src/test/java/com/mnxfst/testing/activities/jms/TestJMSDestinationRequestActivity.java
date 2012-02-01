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
//		activity.initialize(cfgOpt); TODO MOCK!
	}
}
