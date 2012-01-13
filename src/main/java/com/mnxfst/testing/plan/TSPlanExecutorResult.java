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

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Holds the result of a {@link TSPlan} execution. Beside basic timing information the context of a single
 * run is contained as well
 * @author mnxfst
 *
 */
public class TSPlanExecutorResult implements Serializable {

	private static final long serialVersionUID = -636459103997670821L;

	/** holds the name of the test plan that was executed */
	private String testPlanName = null;
	/** name or identifier of surrounding execution environment */
	private String executionEnvironmentId = null;
	/** name or identifier of plan executor */
	private String planExecutorId = null;

	/** timestamp at execution start */
	private long startMillis = 0;
	/** timestamp at execution end */
	private long endMillis = 0;
	/** overall test plan execution duration - for all recurrences/reiterations */
	private long durationMillis = 0;
	/** average duration for a single test plan execution */
	private long singleRunExecutionDurationAverage = 0;
	/** min duration for a single test plan execution */
	private long singleRunExecutionDurationMin = Long.MAX_VALUE;
	/** max duration for a single test plan execution */
	private long singleRunExecutionDurationMax = 0;
	
	private int errors = 0;
	
	/**
	 * Initializes the executor result
	 * @param startMillis
	 * @param endMillis
	 * @param durationMillis
	 * @param errors
	 */
	public TSPlanExecutorResult(String executionEnvironmentId, String planExecutorId, String testPlanName, long startMillis, long endMillis, long durationMillis, long singleRunExecutionDurationMin, long singleRunExecutionDurationMax, long singleRunExecutionDurationAverage, int errors) {
		this.executionEnvironmentId = executionEnvironmentId;
		this.planExecutorId = planExecutorId;
		this.testPlanName = testPlanName;
		this.startMillis = startMillis;
		this.endMillis = endMillis;
		this.durationMillis = durationMillis;
		this.singleRunExecutionDurationMin = singleRunExecutionDurationMin;
		this.singleRunExecutionDurationMax = singleRunExecutionDurationMax;
		this.singleRunExecutionDurationAverage = singleRunExecutionDurationAverage;
		this.errors = errors;
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

	public long getSingleRunExecutionDurationAverage() {
		return singleRunExecutionDurationAverage;
	}

	public void setSingleRunExecutionDurationAverage(
			long singleRunExecutionDurationAverage) {
		this.singleRunExecutionDurationAverage = singleRunExecutionDurationAverage;
	}

	public long getSingleRunExecutionDurationMin() {
		return singleRunExecutionDurationMin;
	}

	public void setSingleRunExecutionDurationMin(long singleRunExecutionDurationMin) {
		this.singleRunExecutionDurationMin = singleRunExecutionDurationMin;
	}

	public long getSingleRunExecutionDurationMax() {
		return singleRunExecutionDurationMax;
	}

	public void setSingleRunExecutionDurationMax(long singleRunExecutionDurationMax) {
		this.singleRunExecutionDurationMax = singleRunExecutionDurationMax;
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
				.append("singleRunExecutionDurationMin", this.singleRunExecutionDurationMin)
				.append("singleRunExecutionDurationMax", this.singleRunExecutionDurationMax)
				.append("singleRunExecutionDurationAverage", this.singleRunExecutionDurationAverage)
				.append("errors", this.errors).toString();
				
	}
}
