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

import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Defines a common parent to single steps within a {@link ITSPlan test plan}.
 * These steps a so-called <i>activities</i>
 * @author mnxfst
 * @since 20.12.2011
 */
public abstract class AbstractTSPlanActivity implements TSPlanActivity {
	
	/** common identifier to all activities of the same type */
	public String id = null;
	/** unique activity name */
	public String name = null;
	/** test plan dependent activity description */
	public String description = null;
	/** activity class name */
	public String className = null;
	/** name of next activity */
	public String nextActivity = null;
	/** variable used for storing results etc */
	private String contextVariable = null;
	/** configuration */
	private TSPlanConfigOption configuration = null;
	
	public AbstractTSPlanActivity() {
		
	}
		
	/**
	 * Initializes the instance
	 * @param id
	 * @param name
	 * @param description
	 * @param className
	 * @param nextActivity
	 * @param variable
	 */
	public AbstractTSPlanActivity(String id, String name, String description, String className, String nextActivity, String contextVariable, TSPlanConfigOption configuration) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.className = className;
		this.nextActivity = nextActivity;
		this.contextVariable = contextVariable;
		this.configuration = configuration;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getNextActivity() {
		return nextActivity;
	}

	public void setNextActivity(String nextActivity) {
		this.nextActivity = nextActivity;
	}

	public String getContextVariable() {
		return contextVariable;
	}

	public void setContextVariable(String contextVariable) {
		this.contextVariable = contextVariable;
	}

	public TSPlanConfigOption getConfiguration() {
		return configuration;
	}

	public void setConfiguration(TSPlanConfigOption configuration) {
		this.configuration = configuration;
	}

	
}
