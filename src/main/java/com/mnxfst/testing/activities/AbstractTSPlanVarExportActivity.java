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

package com.mnxfst.testing.activities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.formatter.DateValueFormatter;
import com.mnxfst.testing.formatter.IntegerValueFormatter;
import com.mnxfst.testing.formatter.TSValueFormatter;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Provides a common parent to all classes exporting variables to any location. This class provides
 * convenience methods for formatting export and alike 
 * @author ckreutzfeldt
 * @since 10.02.2012
 */
public abstract class AbstractTSPlanVarExportActivity extends AbstractTSPlanActivity {

	private Map<String, TSValueFormatter<? extends Serializable>> variableExportFormatter = new HashMap<String, TSValueFormatter<? extends Serializable>>();	

	private IntegerValueFormatter threadCountFormatter = null;
	private IntegerValueFormatter waitTimeFormatter = null;
	private DateValueFormatter dateFormatter = null;

	public AbstractTSPlanVarExportActivity() {
		
		try {
			// TODO should be configurable
			TSPlanConfigOption threadCountCfg = new TSPlanConfigOption();
			threadCountCfg.addOption("pattern", "#0000");
			this.threadCountFormatter = new IntegerValueFormatter();
			this.threadCountFormatter.configure(threadCountCfg);
			
			TSPlanConfigOption waitTimeCfg = new TSPlanConfigOption();
			waitTimeCfg.addOption("pattern", "#0000");
			this.waitTimeFormatter = new IntegerValueFormatter();
			this.waitTimeFormatter.configure(waitTimeCfg);
			
			TSPlanConfigOption dateCfg = new TSPlanConfigOption();
			dateCfg.addOption("pattern", "yyyy-MM-dd HH:mm:ss.SSS");
			dateCfg.addOption("timezone", "UTC");
			this.dateFormatter = new DateValueFormatter();
			this.dateFormatter.configure(dateCfg);
			System.out.println(dateFormatter.format(new Date())  + " --- ");
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

		
	}
	
	public String format(String logPattern, Serializable value) throws TSPlanActivityExecutionException {

		// TODO test and refactor!!!
		if(logPattern != null) {
			if(value != null) {
				if(logPattern.equalsIgnoreCase("${global.threads}")) {
					Integer v = null;
					if(value instanceof String)
						v = Integer.valueOf((String)value);
					else
						v = (Integer)value;
					return this.threadCountFormatter.format(v);
				} else if(logPattern.equalsIgnoreCase("${global.waitTime}")) {
					Integer v = null;
					if(value instanceof String)
						v = Integer.valueOf((String)value);
					else
						v = (Integer)value;
					return this.waitTimeFormatter.format(v);
				}
				else if(logPattern.equalsIgnoreCase("${run.initDate}"))
					return this.dateFormatter.format(new Date((Long)value));
				else if(logPattern.equalsIgnoreCase("${run.finalDate}"))
					return this.dateFormatter.format(new Date((Long)value));
				return value.toString();
			}			
		}
		return "";
	}

}
