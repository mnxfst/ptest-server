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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.mnxfst.testing.plan.exec.TSPlanExecEnvironment;
import com.mnxfst.testing.plan.exec.TSPlanExecutor;

/**
 * Contains the consolidated {@link TSPlanExecutorResult results} of the {@link TSPlanExecutor executors} controlled by 
 * a {@link TSPlanExecEnvironment}.
 * @author ckreutzfeldt
 * @since 09.01.2012
 */
public class TSPlanExecEnvironmentResult implements Serializable {

	private static final long serialVersionUID = -3345396449258114219L;

	private String testPlanName = null;
	private String executionEnvironmentId = null;
	private long startMillis = 0;
	private long endMillis = 0;
	private long averageDurationMillis = 0;
	private long maxDurationMillis = 0;
	private long minDurationMillis = 0;
	private int errors = 0;
	private Set<String> executorIds = new HashSet<String>();

	public TSPlanExecEnvironmentResult(String executionEnvironmentId, String testPlanName) {
		this.executionEnvironmentId = executionEnvironmentId;
		this.testPlanName = testPlanName;
	}
	
	public TSPlanExecEnvironmentResult(String executionEnvironmentId, String testPlanName, long startMillis, long endMillis, long averageDuration, long maxDuration, long minDuration, int errors) {
		this.executionEnvironmentId = executionEnvironmentId;
		this.testPlanName = testPlanName;
		this.startMillis = startMillis;
		this.endMillis = endMillis;
		this.averageDurationMillis = averageDuration;
		this.maxDurationMillis = maxDuration;
		this.minDurationMillis = minDuration;
		this.errors = errors;
	}
	
	public void addExecutorId(String executorId) {
		this.executorIds.add(executorId);
	}
	
	public void removeExecutorId(String executorId) {
		this.executorIds.remove(executorId);
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

	public long getAverageDurationMillis() {
		return averageDurationMillis;
	}

	public void setAverageDurationMillis(long averageDurationMillis) {
		this.averageDurationMillis = averageDurationMillis;
	}

	public long getMaxDurationMillis() {
		return maxDurationMillis;
	}

	public void setMaxDurationMillis(long maxDurationMillis) {
		this.maxDurationMillis = maxDurationMillis;
	}

	public long getMinDurationMillis() {
		return minDurationMillis;
	}

	public void setMinDurationMillis(long minDurationMillis) {
		this.minDurationMillis = minDurationMillis;
	}

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public Set<String> getExecutorIds() {
		return executorIds;
	}

	public void setExecutorIds(Set<String> executorIds) {
		this.executorIds = executorIds;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
				.append("executionEnvironmentId", this.executionEnvironmentId)
				.append("testPlanName", this.testPlanName)
				.append("startMillis", this.startMillis)
				.append("endMillis", this.endMillis)
				.append("averageDurationMillis", this.averageDurationMillis)
				.append("minDurationMillis", this.minDurationMillis)
				.append("maxDurationMillis", this.maxDurationMillis)
				.append("errors", this.errors)
				.append("executorIds", this.executorIds).toString();
				
	}
}
