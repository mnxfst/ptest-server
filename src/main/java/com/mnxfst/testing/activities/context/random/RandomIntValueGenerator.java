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

import java.util.Random;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Random integer value generator
 * @author ckreutzfeldt
 *
 */
public class RandomIntValueGenerator implements IRandomCtxVarValueGenerator<Integer> {

	private static final String CFG_OPT_MIN_VALUE = ".minValue";
	private static final String CFG_OPT_MAX_VALUE = ".maxValue";
	
	private int minValue = Integer.MIN_VALUE;
	private int maxValue = Integer.MAX_VALUE;
	
	private Random randomGenerator = new Random();
	
	/**
	 * Default constructor
	 */
	public RandomIntValueGenerator() {		
	}

	/**
	 * @see com.mnxfst.testing.activities.context.random.IRandomCtxVarValueGenerator#init(com.mnxfst.testing.plan.config.TSPlanConfigOption, java.lang.String)
	 */
	public void init(TSPlanConfigOption cfgOpt, String generatorCfgPrefix) throws TSPlanActivityExecutionException {
		
		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Missing required configuration options");
		
		if(generatorCfgPrefix == null || generatorCfgPrefix.isEmpty())
			throw new TSPlanActivityExecutionException("Missing required config option prefix");
	
		String tmp = (String)cfgOpt.getOption(generatorCfgPrefix + CFG_OPT_MIN_VALUE);
		if(tmp != null && !tmp.isEmpty()) {				
			try {
				this.minValue = Integer.parseInt(tmp);
			} catch(NumberFormatException e) {
				throw new TSPlanActivityExecutionException("Failed to parse configured min value '"+tmp+"' into a numerical representation");
			}
		} else {
			throw new TSPlanActivityExecutionException("Missing required minValue configuration option for random integer value generator");
		}
		
		tmp = (String)cfgOpt.getOption(generatorCfgPrefix + CFG_OPT_MAX_VALUE);
		if(tmp != null && !tmp.isEmpty()) {
			try {
				this.maxValue = Integer.parseInt(tmp);
			} catch(NumberFormatException e) {
				throw new TSPlanActivityExecutionException("Failed to parse configured max value '"+tmp+"' into a numerical representation");
			}
		} else {
			throw new TSPlanActivityExecutionException("Missing required maxValue configuration option for random integer value generator");
		}
	}
	
	/**
	 * @see com.mnxfst.testing.activities.context.random.IRandomCtxVarValueGenerator#generate()
	 */
	public Integer generate() {
		return randomGenerator.nextInt(maxValue) + minValue;
	}

}
