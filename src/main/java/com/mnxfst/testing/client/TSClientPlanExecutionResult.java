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

package com.mnxfst.testing.client;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Contains the results for a single test plan run on a single host being evaluated on the client
 * @author ckreutzfeldt
 * @since 13.02.2012
 */
public class TSClientPlanExecutionResult implements Serializable {

	private static final long serialVersionUID = -1969243462453213727L;

	/** host name */
	private String hostName = null;
	/** port */
	private int port = -1;
	/** holds the key identifying the result */
	private String resultIdentifier = null;
	/** response code */
	private long responseCode = -1;
	/** name of executed test plan */
	private String testPlan = null;
	/** name of the execution environment */
	private String executionEnvironmentId = null;
	/** start time */
	private long startTimestamp = 0;
	/** end time */
	private long endTimestamp = 0;
	/** average duration */
	private long averageDuration = 0;
	/** single min duration */
	private long singleMinDuration = 0;
	/** single max duration */
	private long singleMaxDuration = 0;
	/** single average duration */
	private long singleAverageDuration = 0;
	/** median */
	private double averageDurationMedian = 0.0;
	/** number of errors */
	private int errors = 0;
	
	public TSClientPlanExecutionResult() {		
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getResultIdentifier() {
		return resultIdentifier;
	}

	public void setResultIdentifier(String resultIdentifier) {
		this.resultIdentifier = resultIdentifier;
	}

	public long getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(long responseCode) {
		this.responseCode = responseCode;
	}

	public String getTestPlan() {
		return testPlan;
	}

	public void setTestPlan(String testPlan) {
		this.testPlan = testPlan;
	}

	public String getExecutionEnvironmentId() {
		return executionEnvironmentId;
	}

	public void setExecutionEnvironmentId(String executionEnvironmentId) {
		this.executionEnvironmentId = executionEnvironmentId;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public long getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public long getAverageDuration() {
		return averageDuration;
	}

	public void setAverageDuration(long averageDuration) {
		this.averageDuration = averageDuration;
	}

	public long getSingleMinDuration() {
		return singleMinDuration;
	}

	public void setSingleMinDuration(long singleMinDuration) {
		this.singleMinDuration = singleMinDuration;
	}

	public long getSingleMaxDuration() {
		return singleMaxDuration;
	}

	public void setSingleMaxDuration(long singleMaxDuration) {
		this.singleMaxDuration = singleMaxDuration;
	}

	public long getSingleAverageDuration() {
		return singleAverageDuration;
	}

	public void setSingleAverageDuration(long singleAverageDuration) {
		this.singleAverageDuration = singleAverageDuration;
	}

	public double getAverageDurationMedian() {
		return averageDurationMedian;
	}

	public void setAverageDurationMedian(double averageDurationMedian) {
		this.averageDurationMedian = averageDurationMedian;
	}

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
				.append("ptestServerHost", this.hostName)
				.append("ptestServerPort", this.port)
				.append("responseCode", this.responseCode)
				.append("resultIdentifier", this.resultIdentifier)
				.append("executionEnvironmentId", this.executionEnvironmentId)
				.append("testPlan", this.testPlan)
				.append("startMillis", this.startTimestamp)
				.append("endMillis", this.endTimestamp)
				.append("averageDurationMillis", this.averageDuration)
				.append("singleRunExecutionDurationAverage", this.singleAverageDuration)
				.append("singleRunExecutionDurationMin", this.singleMinDuration)
				.append("singleRunExecutionDurationMax", this.singleMaxDuration)
				.append("averageDurationMedian", this.averageDurationMedian)
				.append("errors", this.errors).toString();
				
	}

}
