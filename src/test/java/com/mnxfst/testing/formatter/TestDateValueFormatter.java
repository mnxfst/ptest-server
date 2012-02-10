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

import java.util.Date;

import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Unit test for {@link DateValueFormatter}
 * @author ckreutzfeldt
 * @since 10.02.2012
 */
public class TestDateValueFormatter {

	@Test
	public void testConfigure() {
		// TODO
	}
	
	@Test
	public void testFormat() throws TSPlanActivityExecutionException {
		
		TSPlanConfigOption cfgOpt = new TSPlanConfigOption();
		cfgOpt.addOption("pattern", "yyyy-MM-dd HH:mm:ss,SSS");
		cfgOpt.addOption("timezone", "UTC");
		DateValueFormatter f = new DateValueFormatter();
		f.configure(cfgOpt);
		
		System.out.println(f.format(new Date()));
		
		
	}
	
}
