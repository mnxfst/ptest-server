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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.TSPlanActivity;
import com.mnxfst.testing.exception.TSPlanExecutionFailedException;
import com.mnxfst.testing.exception.TSPlanMissingException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanResult;

/**
 * Provides a closed runtime environment for a {@link TSPlan} 
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSPlanExecutor implements Callable<TSPlanResult> {

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

	
	/**
	 * Initializes the executor
	 * @param testPlan
	 */
	public TSPlanExecutor(TSPlan testPlan, String executionEnvironmentId, String planExecutorId, int recurrences, TSPlanRecurrenceType recurrenceType) {
		this.testPlan = testPlan;
		this.executionEnvironmentId = executionEnvironmentId;
		this.planExecutorId = planExecutorId;
		this.recurrences = recurrences;
		this.recurrenceType = recurrenceType;
		
		if(logger.isDebugEnabled())
			logger.debug("Recurrence type: " + recurrenceType + ", recurrences: " + recurrences);
		
	}
	
	/**
	 * Executes the test plan - if provided. In case there is not test plan,
	 * a {@link TSPlanMissingException} will be thrown
	 * @see java.util.concurrent.Callable#call()
	 */
	public TSPlanResult call() throws Exception {
		if(testPlan == null)
			throw new TSPlanMissingException("No test plan found");

		boolean success = true;
		
		// holds a list of already visited activities -- used for avoiding loops
		Set<String> alreadyVisitedActivities = new HashSet<String>();
		
		// start timer
		long overallStart = System.currentTimeMillis();
		
		// create empty input
		Map<String, Serializable> context = new HashMap<String, Serializable>();
		
		// TODO support timed recurrences
		if(recurrenceType != TSPlanRecurrenceType.TIMES)
			throw new TSPlanExecutionFailedException("Unsupported recurrence type: " + recurrenceType);

		for(int i = 0; i < recurrences; i++) {
		
			// clear context for each plan execution run
			context.clear();
			
			// clear set of already visited activities for each execution run
			alreadyVisitedActivities.clear();
			
			// holds the currently visited activity
			TSPlanActivity currentActivity = null;
			
			// fetch the name of the next activity to visit - which is in this case the initial activity
			String nextActivityName = testPlan.getInitActivityName();
			try {
				
				// as long as the name of the next activity does not equal 'finish' and is not null, execute the next activity
				while(nextActivityName != null && !nextActivityName.equalsIgnoreCase(FINAL_ACTIVITY_NAME)) {
					
					// add the activity to the set of already visited one to raise a base for finding loops
					alreadyVisitedActivities.add(nextActivityName);
					
					// fetch the next activity and validate it against null
					currentActivity = testPlan.getActivity(nextActivityName);
					if(currentActivity == null)
						throw new TSPlanExecutionFailedException("Test plan execution failed. Unknown activity: " + nextActivityName);
					
					// execute the activity and get the context back
					context = currentActivity.execute(context);
	
					// if the context has a special marker indicating that the "next activity" attribute must be ignored and
					// the additionally provided activity must be executed, fetch the name and remove the special marker
					if(context.containsKey(NEXT_ACTIVITY_OVERRIDE_ATTRIBUTE)) {
						nextActivityName = (String)context.get(NEXT_ACTIVITY_OVERRIDE_ATTRIBUTE);
						context.remove(NEXT_ACTIVITY_OVERRIDE_ATTRIBUTE);
					// otherwise: fetch the name of the next activity
					} else {
						nextActivityName = currentActivity.getNextActivity();
					}
					
					// check if the next activity has already been visited during this plan exeuction
					if(alreadyVisitedActivities.contains(nextActivityName))
						throw new TSPlanExecutionFailedException("Test plan execution failed. Loop found for activity: " + nextActivityName);
					
				}
			} catch(TSPlanExecutionFailedException e) {
				logger.error(e.getMessage(), e);
				success = false;
			}
			
			if(interrupted)
				break;
		}
		
		long overallEnd = System.currentTimeMillis();

		logger.info("Duration: " + (overallEnd-overallStart));
		
		TSPlanResult result = null;
		if(success) {
			result = new TSPlanResult(executionEnvironmentId, planExecutorId, testPlan.getName(), overallStart, overallEnd, (overallEnd-overallStart), true);
			result.getContext().putAll(context);
		} else {
			result = new TSPlanResult(executionEnvironmentId, planExecutorId, testPlan.getName(), overallStart, overallEnd, (overallEnd-overallStart), 1);
			result.getContext().putAll(context);
		}
		
		return result;
	}
	
	public void interrupt() {
		this.interrupted = true;
	}
	
}
