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

import java.text.DecimalFormat;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Formats a provided value according to a configured pattern
 * @author ckreutzfeldt
 * @since 09.02.2012
 */
public class IntegerValueFormatter implements TSValueFormatter<Integer> {

	
	private static final String CFG_OPT_PATTERN = "pattern";
	
	private DecimalFormat numberFormatter = null;
	
	/**
	 * @see com.mnxfst.testing.formatter.TSValueFormatter#configure(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void configure(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Missing required configuration options");
		
		String pattern = (String)cfgOpt.getOption(CFG_OPT_PATTERN);
		if(pattern == null || pattern.isEmpty())
			throw new TSPlanActivityExecutionException("Missing required configuration option '"+CFG_OPT_PATTERN+"'");
		
		try {
			this.numberFormatter = new DecimalFormat(pattern);
		} catch(Exception e) {
			throw new TSPlanActivityExecutionException("Invalid number format pattern '"+pattern+"'. Error: " + e.getMessage());
		}
		
	}

	/**
	 * @see com.mnxfst.testing.formatter.TSValueFormatter#format(java.io.Serializable)
	 */
	public String format(Integer input) throws TSPlanActivityExecutionException {
		try {
			return numberFormatter.format(input);
		} catch(IllegalArgumentException e) {
			throw new TSPlanActivityExecutionException("Invalid input provided. Expected an integer object but received: " + input + ". Error: " + e.getMessage());
		}
	}

}
