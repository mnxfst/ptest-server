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

package com.mnxfst.testing.plan.exec;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.TSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.exception.TSPlanExecutionFailedException;
import com.mnxfst.testing.exception.TSPlanMissingException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanExecutorResult;
import com.mnxfst.testing.plan.ctx.ExecutionContextValueType;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Provides a closed runtime environment for a {@link TSPlan}. The results are returned following the {@link TSPlanExecutorResult} structure. 
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSPlanExecutor implements Callable<TSPlanExecutorResult> {

	private static final Logger logger = Logger.getLogger(TSPlanExecutor.class);
	
	/** must be used by {@link TSPlanActivity} to override next activity value and must be removed by the {@link TSPlanExecutor} after forwarding to it */ 
	private static final String NEXT_ACTIVITY_OVERRIDE_ATTRIBUTE = "nextActivity.override";
	
	/** name and identifier of final activity. if this one is reached, the executor stops and returns the result */
	private static final String FINAL_ACTIVITY_NAME = "finish";
	
	private TSPlan testPlan = null;
	private String executionEnvironmentId = null;
	private String planExecutorId = null; 
	private int recurrences = -1;
	private TSPlanRecurrenceType recurrenceType = TSPlanRecurrenceType.UNKNOWN;
	private boolean interrupted = false;
	private TSPlanExecutionContext context = new TSPlanExecutionContext();

	/**
	 * Initializes the executor
	 * @param testPlan
	 * @param executionEnvironmentId
	 * @param planExecutorId
	 * @param recurrences
	 * @param recurrenceType
	 */
	public TSPlanExecutor(TSPlan testPlan, String executionEnvironmentId, String planExecutorId, int recurrences, TSPlanRecurrenceType recurrenceType) throws TSPlanMissingException {
		this(testPlan, executionEnvironmentId, planExecutorId, recurrences, recurrenceType, null);
	}
	
	/**
	 * Initializes the executor
	 * @param testPlan
	 * @param executionEnvironmentId
	 * @param planExecutorId
	 * @param recurrences
	 * @param recurrenceType
	 * @param context
	 */
	public TSPlanExecutor(TSPlan testPlan, String executionEnvironmentId, String planExecutorId, int recurrences, TSPlanRecurrenceType recurrenceType, Map<String, Serializable> preconfiguredDurableContextVariables) throws TSPlanMissingException  {
		
		if(testPlan == null)
			throw new TSPlanMissingException("Missing required test plan");
		
		this.testPlan = testPlan;
		this.executionEnvironmentId = executionEnvironmentId;
		this.planExecutorId = planExecutorId;
		this.recurrences = recurrences;
		this.recurrenceType = recurrenceType;
		
		if(preconfiguredDurableContextVariables != null && !preconfiguredDurableContextVariables.isEmpty()) {
			for(String key : preconfiguredDurableContextVariables.keySet()) {
				context.addContextValue(key, preconfiguredDurableContextVariables.get(key), ExecutionContextValueType.GLOBAL);
			}
		}		
				
		if(logger.isDebugEnabled())
			logger.debug("TSPlanExecutor[testPlan="+testPlan.getName()+", execEnvId="+executionEnvironmentId+", executorId="+planExecutorId+", recurrences="+recurrences+", recType="+recurrenceType+", preconfiguredVars="+context.getContextValueNames(ExecutionContextValueType.GLOBAL).size()+"]");
		
	}
	
	/**
	 * Executes the test plan - if provided. In case there is not test plan,
	 * a {@link TSPlanMissingException} will be thrown
	 * @see java.util.concurrent.Callable#call()
	 */
	public TSPlanExecutorResult call() throws Exception {
		if(testPlan == null)
			throw new TSPlanMissingException("No test plan found");

		// holds a list of already visited activities -- used for avoiding loops
		Set<String> alreadyVisitedActivities = new HashSet<String>();
		
		// start timer
		long overallStart = System.currentTimeMillis();
		
		// TODO support timed recurrences
		if(recurrenceType != TSPlanRecurrenceType.TIMES)
			throw new TSPlanExecutionFailedException("Unsupported recurrence type: " + recurrenceType);

		// counts the errors which occur while executing an activity
		int activityExecutionErrorCount = 0;
		
		// contain the average plan execution for all iterations
		long averagePlanExecDuration = 0;
		
		// start / stop timestamp
		long singleExecStart = 0;
		long singleExecEnd = 0;
		
		// hold the min and max runtimes for a single run for all iterations
		long singleRunMin = Long.MAX_VALUE;
		long singleRunMax = 0;		
		long singleRunDuration = 0;
		
		for(int i = 0; i < recurrences; i++) {
								
			// clear context for each plan execution run
			context.clearTransientValueStore();
			
			// clear set of already visited activities for each execution run
			alreadyVisitedActivities.clear();
			
			// holds the currently visited activity
			TSPlanActivity currentActivity = null;
			
			// fetch the name of the next activity to visit - which is in this case the initial activity
			String nextActivityName = testPlan.getInitActivityName();
		
			// set start timer
			singleExecStart = System.currentTimeMillis();
			
			// as long as the name of the next activity does not equal 'finish' and is not null, execute the next activity
			while(nextActivityName != null && !nextActivityName.equalsIgnoreCase(FINAL_ACTIVITY_NAME)) {
					
				// add the activity to the set of already visited one to raise a base for finding loops
				alreadyVisitedActivities.add(nextActivityName);
					
				// fetch the next activity and validate it against null
				currentActivity = testPlan.getActivity(nextActivityName);
				if(currentActivity == null)
					throw new TSPlanExecutionFailedException("Test plan execution failed. Unknown activity: " + nextActivityName);
					
				try {
					// execute the activity and get the context back
					context = currentActivity.execute(context);
				} catch(TSPlanActivityExecutionException e) {
					activityExecutionErrorCount = activityExecutionErrorCount + 1;
					logger.error("Failed to execute activity: " + currentActivity.getClass().getName() +". Error: " + e.getMessage(), e);
				}
	
				// if the context has a special marker indicating that the "next activity" attribute must be ignored and
				// the additionally provided activity must be executed, fetch the name and remove the special marker
				if(context.hasContextVariable(NEXT_ACTIVITY_OVERRIDE_ATTRIBUTE, ExecutionContextValueType.RUN)) {
					nextActivityName = (String)context.getContextValue(NEXT_ACTIVITY_OVERRIDE_ATTRIBUTE, ExecutionContextValueType.RUN);
					context.removeContextValue(NEXT_ACTIVITY_OVERRIDE_ATTRIBUTE, ExecutionContextValueType.RUN);
					// otherwise: fetch the name of the next activity
				} else {
					nextActivityName = currentActivity.getNextActivity();
				}
					
				// check if the next activity has already been visited during this plan exeuction
				if(alreadyVisitedActivities.contains(nextActivityName))
					throw new TSPlanExecutionFailedException("Test plan execution failed. Loop found for activity: " + nextActivityName);
			}
			
			// set end timer
			singleExecEnd = System.currentTimeMillis();
			
			// calculate duration and match it for min / max runtimes so far
			singleRunDuration = singleExecEnd - singleExecStart;			
			if(singleRunMin > singleRunDuration )
				singleRunMin = singleRunDuration;
			if(singleRunMax < singleRunDuration)
				singleRunMax = singleRunDuration;
			
			// add plan exec duration to average plan duration
			averagePlanExecDuration = averagePlanExecDuration + singleRunDuration;
			
			if(interrupted)
				break;
		}
		
		// set end timer for whole test run
		long overallEnd = System.currentTimeMillis();
		
		// calculate overall duration
		long duration = (overallEnd - overallStart);

		// calculate average plan duration
		averagePlanExecDuration = averagePlanExecDuration / recurrences;
		
		if(logger.isDebugEnabled())
			logger.debug("[execEnv:" + executionEnvironmentId + ", executor: " + planExecutorId + ", recurrences: " + recurrences + ", recType: " + recurrenceType + ", duration: " + duration+", averageRunDuration: "+ averagePlanExecDuration +"]");
		
		return new TSPlanExecutorResult(executionEnvironmentId, planExecutorId, testPlan.getName(), overallStart, overallEnd, duration, singleRunMin, singleRunMax, averagePlanExecDuration, activityExecutionErrorCount);
	}
	
	public void interrupt() {
		this.interrupted = true;
	}
	
}
