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

package com.mnxfst.testing.activities.context.random;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.activities.context.random.IRandomCtxVarValueGenerator;
import com.mnxfst.testing.activities.context.random.RandomIntValueGenerator;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Test case for {@link RandomIntValueGenerator}
 * @author ckreutzfeldt
 * @since 11.01.2012
 */
public class TestRandomIntValueGenerator {

	@Test
	public void testInit() throws TSPlanActivityExecutionException {
		
		try {
			new RandomIntValueGenerator().init(null, null);
			Assert.fail("Missing required configuration options");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		try {
			new RandomIntValueGenerator().init(new TSPlanConfigOption(), null);
			Assert.fail("Missing required prefix info");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		try {
			new RandomIntValueGenerator().init(new TSPlanConfigOption(), "test");
			Assert.fail("Missing required minValue option");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("generator.minValue", "1");
		
		try {
			new RandomIntValueGenerator().init(cfgOpt, "generator.");
			Assert.fail("Missing required maxValue option");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfgOpt.addOption("generator.maxValue", "10");
		new RandomIntValueGenerator().init(cfgOpt, "generator");
	}
	
	@Test
	public void testExecute() throws TSPlanActivityExecutionException {

		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("generator.minValue", "1");
		cfgOpt.addOption("generator.maxValue", "10");

		IRandomCtxVarValueGenerator<Integer> gen = new RandomIntValueGenerator();
		gen.init(cfgOpt, "generator");
		Assert.assertNotNull("The result must not be null", gen.generate());
		for(int i = 0; i < 1000; i++) {
			int g = gen.generate().intValue();
			Assert.assertTrue("The value must be greater than 0 and less than 11", (1 <= g && g < 11));
		}
	}
	
}
