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
 * Random firstname value generator
 * 
 * @author mkalsow
 * 
 */
public class RandomFirstNameValueGenerator implements
		IRandomCtxVarValueGenerator<String> {

	private Random randomNumberGenerator = new Random();

	/**
	 * Default constructor
	 */
	public RandomFirstNameValueGenerator() {
	}

	/**
	 * @see com.mnxfst.testing.activities.context.random.IRandomCtxVarValueGenerator#init(com.mnxfst.testing.plan.config.TSPlanConfigOption,
	 *      java.lang.String)
	 */
	public void init(TSPlanConfigOption cfgOpt, String generatorCfgOptPrefix)
			throws TSPlanActivityExecutionException {
	}

	/**
	 * @see com.mnxfst.testing.activities.context.random.IRandomCtxVarValueGenerator#generate()
	 */
	public String generate() {

		if (randomNumberGenerator.nextInt(2) == 0) {
			return "Horst" + randomNumberGenerator.nextInt(10000);
		} else {
			return "Hans";
		}

	}
}
