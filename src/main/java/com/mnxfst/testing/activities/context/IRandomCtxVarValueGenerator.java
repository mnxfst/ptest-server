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

package com.mnxfst.testing.activities.context;

import java.io.Serializable;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Common interface to all random value generators 
 * @author ckreutzfeldt
 *
 * @param <E>
 */
public interface IRandomCtxVarValueGenerator<E extends Serializable> {

	/** config options prefix used for identifying relevant settings */
	public static final String GENERATE_VARIABLE_NAME_PREFIX = "generate.variable.";

	/**
	 * Initializes the value generator. This method is being called after automatic instantiation by {@link RandomCtxVarGenActivity}
	 * @param cfgOpt
	 * @param generatorCfgOptPrefix
	 * @throws TSPlanActivityExecutionException
	 */
	public void init(TSPlanConfigOption cfgOpt, String generatorCfgOptPrefix) throws TSPlanActivityExecutionException;
	
	/**
	 * Generates a value
	 * @return
	 */
	public E generate();
	
}
