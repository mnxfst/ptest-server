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

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Test case for {@link RandomUUIDValueGenerator} 
 * @author ckreutzfeldt
 * @since 31.01.2012
 */
public class TestRandomUUIDValueGenerator {

	@Test
	public void testGenerate() throws TSPlanActivityExecutionException {

		RandomUUIDValueGenerator gen = new RandomUUIDValueGenerator();
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("generate.variable.arg0.class", RandomUUIDValueGenerator.class.getName());
		
		try {
			gen.init(cfgOpt, "generate.variable.arg0");
			Assert.fail("UUID type missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("generate.variable.arg0.uuidtype", "test");
		
		try {
			gen.init(cfgOpt, "generate.variable.arg0");
			Assert.fail("Invalid option 'uuidtype'");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("generate.variable.arg0.uuidType", "test");
		try {
			gen.init(cfgOpt, "generate.variable.arg0");
			Assert.fail("Invalid type 'test'");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfgOpt.addOption("generate.variable.arg0.uuidType", "time");
		gen.init(cfgOpt, "generate.variable.arg0");
		
		cfgOpt.addOption("generate.variable.arg0.uuidType", "mac");
		gen.init(cfgOpt, "generate.variable.arg0");
		
		UUID[] uuids = new UUID[10];
		for(int i = 0; i < 10; i++) {
			uuids[i] = gen.generate();
		}
		
		for(int i = 0; i < 10; i++) {			
			for(int j = i+1; j < 10; j++) {
				
				Assert.assertNotSame("The uuids must not be equal", uuids[i], uuids[j]);
			}
		}
	}
	
}
