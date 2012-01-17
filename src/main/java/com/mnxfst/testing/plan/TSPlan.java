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

package com.mnxfst.testing.plan;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.mnxfst.testing.activities.TSPlanActivity;

/**
 * Contains a whole test plan
 * @author mnxfst
 * @since 20.12.2011
 */
public class TSPlan {

	/** test plan name */
	private String name = null;
	/** test plan description */
	private String description = null;
	/** date of creation */
	private Date creationDate = null;
	/** created by */
	private String createdBy = null;	
	/** initial activity name */
	private String initActivityName = null;
	
	/** holds the ordered test plan */
	private Map<String, TSPlanActivity> activities = new HashMap<String, TSPlanActivity>();
	
	
	public TSPlan() {
		
	}

	/**
	 * Adds an activity for the given name
	 * @param activityName
	 * @param activity
	 */
	public void addActivity(String activityName, TSPlanActivity activity) {
		this.activities.put(activityName, activity);
	}
	
	/**
	 * Removes the activity referenced by the given name
	 * @param activityName
	 */
	public void removeActivity(String activityName) {
		this.activities.remove(activityName);
	}
	
	/**
	 * Returns the referenced activity
	 * @param activityName
	 * @return
	 */
	public TSPlanActivity getActivity(String activityName) {
		return this.activities.get(activityName);
	}

	public String getInitActivityName() {
		return initActivityName;
	}

	public void setInitActivityName(String initActivityName) {
		this.initActivityName = initActivityName;
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Map<String, TSPlanActivity> getActivities() {
		return activities;
	}

	public void setActivities(Map<String, TSPlanActivity> activities) {
		this.activities = activities;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
				.append("name", name)
				.append("description", description)
				.append("creationDate", creationDate)
				.append("createdBy", createdBy)
				.append("initActivityName", initActivityName)
				.append("activities", activities).toString();
	}	
}
