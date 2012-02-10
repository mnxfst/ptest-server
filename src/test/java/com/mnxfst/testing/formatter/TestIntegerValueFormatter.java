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

package com.mnxfst.testing.formatter;

import junit.framework.Assert;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Test case for {@link IntegerValueFormatter}
 * @author ckreutzfeldt
 * @since 09.02.2012
 */
public class TestIntegerValueFormatter {

	@Test
	public void testInitialize() throws TSPlanActivityExecutionException {
		
		try {
			new IntegerValueFormatter().configure(null);
			Assert.fail("No config options provided");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		try {
			new IntegerValueFormatter().configure(new TSPlanConfigOption());
			Assert.fail("Required config option missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("pattern", "pattern"); 
		new IntegerValueFormatter().configure(cfgOpt);
	}
	
	@Test
	public void testFormat() throws TSPlanActivityExecutionException {

		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("pattern", "pattern"); 
		IntegerValueFormatter format = new IntegerValueFormatter();
		format.configure(cfgOpt);
		
		try {
			format.format(null);
			Assert.fail("Invalid input");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		Assert.assertNotNull("The result must not be null", format.format(1));		
		Assert.assertEquals("The result must be 'pattern1'", "pattern1", format.format(1));
		
		cfgOpt.addOption("pattern", "#0000");
		format.configure(cfgOpt);
		Assert.assertEquals("The result must be 0001", "0001", format.format(1));
		Assert.assertEquals("The result must be 0012", "0012", format.format(12));
		Assert.assertEquals("The result must be 0123", "0123", format.format(123));
		Assert.assertEquals("The result must be 1234", "1234", format.format(1234));
		Assert.assertEquals("The result must be 12345", "12345", format.format(12345));
		
	}
	
}
