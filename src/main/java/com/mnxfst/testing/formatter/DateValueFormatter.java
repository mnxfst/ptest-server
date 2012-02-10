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

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.TimeZone;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Implements a {@link Date} value formatter
 * @author ckreutzfeldt
 * @since 10.02.2012
 */
public class DateValueFormatter implements TSValueFormatter<Date> {

	private static final String CFG_OPT_PATTERN = "pattern";
	private static final String CFG_OPT_TIMEZONE = "timezone";
	
	private SimpleDateFormat dateFormatter = null;
	private String timezone = null;
	
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
			this.dateFormatter = new SimpleDateFormat(pattern);
		} catch(Exception e) {
			throw new TSPlanActivityExecutionException("Invalid date format pattern '"+pattern+"'. Error: " + e.getMessage());
		}
		
		this.timezone = (String)cfgOpt.getOption(CFG_OPT_TIMEZONE);
		if(this.timezone == null || this.timezone.isEmpty())
			throw new TSPlanActivityExecutionException("Missing required timezone option");
		
		try {
			this.dateFormatter.setTimeZone(TimeZone.getTimeZone(this.timezone));
		} catch(Exception e) {
			throw new TSPlanActivityExecutionException("Invalid timezone '"+timezone+"'. Error: " + e.getMessage());
		}
		
	}

	/**
	 * @see com.mnxfst.testing.formatter.TSValueFormatter#format(java.io.Serializable)
	 */
	public String format(Date input) throws TSPlanActivityExecutionException {
		try {
			return dateFormatter.format(input);
		} catch(IllegalArgumentException e) {
			throw new TSPlanActivityExecutionException("Invalid input provided. Expected an integer object but received: " + input + ". Error: " + e.getMessage());
		}
	}

}
