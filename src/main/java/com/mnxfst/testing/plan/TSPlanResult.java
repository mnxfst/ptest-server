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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Holds the result of a {@link TSPlan} execution. Beside basic timing information the context of a single
 * run is contained as well
 * @author mnxfst
 *
 */
public class TSPlanResult implements Serializable {

	private static final long serialVersionUID = -636459103997670821L;

	private String testPlanName = null;
	private String executionEnvironmentId = null;
	private String planExecutorId = null;

	private long startMillis = 0;
	private long endMillis = 0;
	private long durationMillis = 0;
	
	private int errors = 0;
	private boolean success = false;
	
	private Map<String, Serializable> context = new HashMap<String, Serializable>();

	/**
	 * Initializes the executor result
	 * @param startMillis
	 * @param endMillis
	 * @param durationMillis
	 * @param errors
	 */
	public TSPlanResult(String executionEnvironmentId, String planExecutorId, String testPlanName, long startMillis, long endMillis, long durationMillis, int errors) {
		this.executionEnvironmentId = executionEnvironmentId;
		this.planExecutorId = planExecutorId;
		this.testPlanName = testPlanName;
		this.startMillis = startMillis;
		this.endMillis = endMillis;
		this.durationMillis = durationMillis;
		this.errors = errors;
		this.success = false;
	}
	
	/**
	 * Initializes the executor result
	 * @param startMillis
	 * @param endMillis
	 * @param durationMillis
	 * @param success
	 */
	public TSPlanResult(String executionEnvironmentId, String planExecutorId, String testPlanName, long startMillis, long endMillis, long durationMillis, boolean success) {
		this.executionEnvironmentId = executionEnvironmentId;
		this.planExecutorId = planExecutorId;
		this.testPlanName = testPlanName;
		this.startMillis = startMillis;
		this.endMillis = endMillis;
		this.durationMillis = durationMillis;
		this.errors = 0;
		this.success = success;
	}

	public long getStartMillis() {
		return startMillis;
	}

	public void setStartMillis(long startMillis) {
		this.startMillis = startMillis;
	}

	public long getEndMillis() {
		return endMillis;
	}

	public void setEndMillis(long endMillis) {
		this.endMillis = endMillis;
	}

	public long getDurationMillis() {
		return durationMillis;
	}

	public void setDurationMillis(long durationMillis) {
		this.durationMillis = durationMillis;
	}

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}


	public Map<String, Serializable> getContext() {
		return context;
	}

	public void setContext(Map<String, Serializable> context) {
		this.context = context;
	}

	public String getTestPlanName() {
		return testPlanName;
	}

	public void setTestPlanName(String testPlanName) {
		this.testPlanName = testPlanName;
	}

	public String getExecutionEnvironmentId() {
		return executionEnvironmentId;
	}

	public void setExecutionEnvironmentId(String executionEnvironmentId) {
		this.executionEnvironmentId = executionEnvironmentId;
	}

	public String getPlanExecutorId() {
		return planExecutorId;
	}

	public void setPlanExecutorId(String planExecutorId) {
		this.planExecutorId = planExecutorId;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
				.append("executionEnvironmentId", this.executionEnvironmentId)
				.append("planExecutorId", this.planExecutorId)
				.append("testPlanName", this.testPlanName)
				.append("startMillis", this.startMillis)
				.append("endMillis", this.endMillis)
				.append("durationMillis", this.durationMillis)
				.append("errors", this.errors)
				.append("success", this.success)
				.append("context", this.context).toString();
				
	}
}
