package com.mnxfst.testing.plan;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Holds the result of a {@link ITSPlan}
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
	
	private Map<String, Serializable> result = new HashMap<String, Serializable>();

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


	public Map<String, Serializable> getResult() {
		return result;
	}

	public void setResult(Map<String, Serializable> result) {
		this.result = result;
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
				.append("result", this.result).toString();
				
	}
}
