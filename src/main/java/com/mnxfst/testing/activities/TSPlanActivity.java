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
import java.util.Map;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Defines a common interface to all activitíes
 * @author mnxfst
 * @since 21.12.2011
 */
public interface TSPlanActivity {

	/**
	 * Executed right after the instance initialization
	 * @throws TSPlanActivityExecutionException
	 */
	public abstract void postInit() throws TSPlanActivityExecutionException;
	
	/**
	 * TODO replace input/context variable by context implementation
	 * Executes a single test plan step
	 * @param input contains the data to be processed by this step
	 * @return 
	 * @throws TSPlanActivityExecutionException thrown in case the activity execution failed for some reason
	 */
	public Map<String, Serializable> execute(Map<String, Serializable> input) throws TSPlanActivityExecutionException;
	
	/**
	 * Fetch the activity identifier which must be unique throughout the whole system
	 * @return
	 */
	public String getId();
	
	/**
	 * Sets the activity identifier which must be unique throughout the whole system
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * Returns the activity name which must be unique throughout the test plan
	 * @return
	 */
	public String getName();
	
	/**
	 * Sets the activity name which must be unique throughout the test plan
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * Returns the test plan description
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Sets the test plan description
	 * @param description
	 */
	public void setDescription(String description);
	
	/**
	 * Returns the name of the activity class implementing the behavior
	 * @return
	 */
	public String getClassName();
	
	/**
	 * Sets the name of the activity class implementing the behavior
	 * @param className
	 */
	public void setClassName(String className);
	
	/**
	 * Returns the name of the next activity
	 * @return
	 */
	public String getNextActivity();
	
	/**
	 * Sets the name of the next activity
	 * @param activityName
	 */
	public void setNextActivity(String activityName);
	
	/**
	 * Returns the name of the context variable used for storing results per activity
	 * @return
	 */
	public String getContextVariable();
	
	/**
	 * Sets the name of the context variable used for storing results per activity
	 * @param variable
	 */
	public void setContextVariable(String contextVariable);
	
	/**
	 * Sets the activity configuration
	 * @param configuration
	 */
	public void setConfiguration(TSPlanConfigOption configuration);
	
	/**
	 * Gets the activity configuration
	 * @return
	 */
	public TSPlanConfigOption getConfiguration();
	
}

