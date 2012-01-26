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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext;

/**
 * Generates random values and stores in the test plan context using the given names 
 * @author ckreutzfeldt
 *
 */
public class RandomCtxVarGenActivity extends AbstractTSPlanActivity {
	
	private static final Logger logger = Logger.getLogger(RandomCtxVarGenActivity.class);

	/** config options prefix used for identifying relevant settings */
	private static final String GENERATE_VARIABLE_NAME_PREFIX = "generate.variable.";
		
	/** holds a mapping from a context variable name to a value generator */
	private Map<String, IRandomCtxVarValueGenerator<?>> valueGenerators = new HashMap<String, IRandomCtxVarValueGenerator<?>>(); 

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {
		
		if(cfgOpt == null || cfgOpt.getOptions().isEmpty())
			throw new TSPlanActivityExecutionException("No configuration options found for random ctx generator activity '"+getName()+"'");
		
		for(String key : cfgOpt.getOptions().keySet()) {
			if(key.startsWith(GENERATE_VARIABLE_NAME_PREFIX) && key.endsWith(".class")) {
				String randomValueGeneratorClassName = (String)cfgOpt.getOption(key);				
				String ctxVarName = key.substring(GENERATE_VARIABLE_NAME_PREFIX.length(), key.length() - 6);
				
				try {
					Class<?> genClass = Class.forName(randomValueGeneratorClassName);
					Object generatorInstance = genClass.newInstance();
					if(generatorInstance instanceof IRandomCtxVarValueGenerator<?>) {
						((IRandomCtxVarValueGenerator<?>)generatorInstance).init(cfgOpt, GENERATE_VARIABLE_NAME_PREFIX + ctxVarName );
						valueGenerators.put(ctxVarName, (IRandomCtxVarValueGenerator<?>)generatorInstance);
					} else {
						throw new TSPlanActivityExecutionException("No such random value generator: " + randomValueGeneratorClassName);
					}
					
					if(logger.isDebugEnabled())
						logger.debug("Successfully added random context value generator: [class="+randomValueGeneratorClassName+", activity="+getName()+"]");
				} catch(InstantiationException e) {
					throw new TSPlanActivityExecutionException("Failed to instantiate value generator class " + randomValueGeneratorClassName + ". Error: " + e.getMessage());
				} catch (ClassNotFoundException e) {
					throw new TSPlanActivityExecutionException("Failed to instantiate value generator class " + randomValueGeneratorClassName + ". Error: " + e.getMessage());
				} catch (IllegalAccessException e) {
					throw new TSPlanActivityExecutionException("Failed to instantiate value generator class " + randomValueGeneratorClassName + ". Error: " + e.getMessage());
				}				
			}
		}		

		if(valueGenerators.isEmpty())
			throw new TSPlanActivityExecutionException("No value generator configuration found in activity settings. Either remove the activity or provide a proper configuration");
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext)
	 */
	public ITSPlanExecutionContext execute(ITSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {
		
		// iterate through context variable names, generate values and write them back into the contxt
		for(String varName : valueGenerators.keySet()) {
			IRandomCtxVarValueGenerator<?> generator = valueGenerators.get(varName);
			ctx.addTransientVariable(varName, generator.generate());
			
			if(logger.isDebugEnabled())
				logger.debug("generate[ctxVar: " + varName + ", value=" + ctx.getTransientVariable(varName)+"]");
		}
		
		return ctx;
	}

	
	
}
