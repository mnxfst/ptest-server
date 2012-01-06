package com.mnxfst.testing.plan;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.mnxfst.testing.activities.TSPlanActivity;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

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
	/** holds the test plan configuration options */
	private Map<String, TSPlanConfigOption> configurationOptions = new HashMap<String, TSPlanConfigOption>();
	
	
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
	
	/**
	 * Returns a {@link TSPlanConfigOption} for a given name
	 * @param configOptionName
	 * @return
	 */
	public TSPlanConfigOption getConfigurationOption(String configOptionName) {
		return configurationOptions.get(configOptionName);
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

	public Map<String, TSPlanConfigOption> getConfigurationOptions() {
		return configurationOptions;
	}

	public void setConfigurationOptions(
			Map<String, TSPlanConfigOption> configurationOptions) {
		this.configurationOptions = configurationOptions;
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
				.append("configOptions", configurationOptions)
				.append("activities", activities).toString();
	}	
}
