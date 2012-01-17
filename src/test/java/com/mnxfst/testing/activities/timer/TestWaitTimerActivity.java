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

package com.mnxfst.testing.activities.timer;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.TSPlanBasicExecutionContext;

/**
 * Test case for {@link WaitTimerActivity}
 * @author mnxfst
 * @since 17.01.2012
 */
public class TestWaitTimerActivity {

	@Test
	public void testExecute() throws TSPlanActivityExecutionException {
		
		WaitTimerActivity activity = new WaitTimerActivity();
		
		try {
			activity.initialize(null);
			Assert.fail("Invalid config options");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		try {
			activity.initialize(new TSPlanConfigOption());
			Assert.fail("Invalid config options");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("waitTime", "12345a");
		try {
			activity.initialize(cfgOpt);
			Assert.fail("Invalid config options");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("waitTime", "20");
		activity.initialize(cfgOpt);
		long start = System.currentTimeMillis();
		activity.execute(new TSPlanBasicExecutionContext());
		long end = System.currentTimeMillis();
		System.out.println((end-start));
		Assert.assertTrue("The duration must be longer than 20ms", (20 <= (end-start)));
		
	}
	
}
