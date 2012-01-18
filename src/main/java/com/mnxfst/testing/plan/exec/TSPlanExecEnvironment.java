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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.mnxfst.testing.exception.TSPlanExecutionFailedException;
import com.mnxfst.testing.exception.TSPlanInstantiationException;
import com.mnxfst.testing.exception.TSPlanMissingException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanExecEnvironmentResult;
import com.mnxfst.testing.plan.TSPlanExecutorResult;

/**
 * Provides an execution environment for parallel {@link TSPlanExecutor test plan executors} being ramped up
 * in a configured number of threads. The <i>exec env</i> ensures that each plan executor receives its own
 * environment as well as it reports back the collected results.  
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSPlanExecEnvironment {

	private static final Logger logger = Logger.getLogger(TSPlanExecEnvironment.class); 
	
	// number of parallel executors (= threads) to use
	private int numberOfParallelExecutors = 0;
	
	// unique identifier for this execution environment - a test server is able to run more than one although it is highly questionable
	private String executionEnvironmentId = null;
	
	// holds the services which controls the executors
	private ExecutorService executorService = null;
	
	// as the environment is being initialized, it fills this list containing a configured number of test plan executors 
	private List<TSPlanExecutor> testPlanExecutors = new ArrayList<TSPlanExecutor>();
	
	// stores the test plan name
	private String testPlanName = null;

	public TSPlanExecEnvironment(String executionEnvironmentId, final TSPlan testPlan, int recurrences, TSPlanRecurrenceType recurrenceType, int numOfParallelExecutors) throws TSPlanMissingException, TSPlanInstantiationException {
		this(executionEnvironmentId, testPlan, recurrences, recurrenceType, numOfParallelExecutors, null);
	}
	
	/**
	 * Initializes the execution environment
	 * @param executionEnvironmentId
	 * @param testPlan
	 * @param recurrences
	 * @param recurrenceType
	 * @param numOfParallelExecutors
	 * @param preconfiguredDurableContextVariables
	 * @throws TSPlanMissingException
	 * @throws TSPlanInstantiationException
	 */
	public TSPlanExecEnvironment(String executionEnvironmentId, final TSPlan testPlan, int recurrences, TSPlanRecurrenceType recurrenceType, int numOfParallelExecutors, Map<String, Serializable> preconfiguredDurableContextVariables) throws TSPlanMissingException, TSPlanInstantiationException {
		// ensure that the provided input is valid
		if(executionEnvironmentId == null || executionEnvironmentId.isEmpty())
			throw new TSPlanInstantiationException("Failed to instantiate test plan due to missing execution environment identifier");
		
		if(testPlan == null)
			throw new TSPlanMissingException("Missing required test plan");
		
		if(recurrences < 1)
			throw new TSPlanInstantiationException("Invalid number of recurrences: " + recurrences + ". Values greater 0 are expected");
		
		if(recurrenceType == null || recurrenceType == TSPlanRecurrenceType.UNKNOWN)
			throw new TSPlanInstantiationException("Recurrence type not provided or unknown: " + recurrenceType);
		
		if(numOfParallelExecutors < 1)
			throw new TSPlanInstantiationException("Failed to instantiate test plan due to an invalid number of parallel executors provided to execution environment '"+executionEnvironmentId+"'");
		
		// assign received values and initialize executor service		
		this.executionEnvironmentId = executionEnvironmentId;
		this.numberOfParallelExecutors = numOfParallelExecutors;			
		this.executorService = Executors.newFixedThreadPool(this.numberOfParallelExecutors);
		this.testPlanName = testPlan.getName();
		
		// instantiate a configured number of test plan executors
		for(int i = 0; i < this.numberOfParallelExecutors; i++)
			testPlanExecutors.add(new TSPlanExecutor(testPlan, executionEnvironmentId, executionEnvironmentId + "-executor-"+i, recurrences, recurrenceType, preconfiguredDurableContextVariables));
		
		if(logger.isDebugEnabled())
			logger.debug("New test plan execution environment instantiated: [execEnvId="+executionEnvironmentId+", testPlan="+testPlan.getName()+", numOfParallelExecutors="+testPlanExecutors.size()+", recurrences="+recurrences+", recurrenceType="+recurrenceType+"]");
	}

	/**
	 * Executes the environment and returns the consolidated results
	 * @return
	 * @throws TSPlanExecutionFailedException 
	 */
	public TSPlanExecEnvironmentResult execute() throws TSPlanExecutionFailedException {
		
		long execStart = System.currentTimeMillis();
		// TODO: check this for large number of executors and large number of recurrences - there were issues the other day in another test environment
		// test plan invocation results
		List<Future<TSPlanExecutorResult>> testPlanInvocResults = null;
		try {
			testPlanInvocResults = executorService.invokeAll(testPlanExecutors);			 
		} catch (InterruptedException e) {
			throw new TSPlanExecutionFailedException("Test plan execution interrupted in: " + executionEnvironmentId + ". Error: " + e.getMessage(), e);
		}
		
		// no results found ==> error
		if(testPlanInvocResults == null)
			throw new TSPlanExecutionFailedException("Test plan execution failed for all threads in: " + executionEnvironmentId);		

		// create new exec env result
		TSPlanExecEnvironmentResult result = new TSPlanExecEnvironmentResult(executionEnvironmentId, testPlanName);
		result.setStartMillis(execStart);
		
		// holds the average duration for all executor threads
		long averageDuration = 0;
		// counts the number of valid responses
		int numOfValidResults = 0;
		// holds the min / max duration
		long maxDuration = 0;
		long minDuration = Long.MAX_VALUE;		
		// error counter
		int errors = 0;
		
		// holds the average, min and max times for a single run within any executor
		long singleRunAvg = 0;
		long singleRunMax = 0;
		long singleRunMin = Long.MAX_VALUE;
		
		
		// iterate through results, extract them, provide missing data, move to overall result set
		for(Future<TSPlanExecutorResult> futureRes : testPlanInvocResults) {
			try {
				TSPlanExecutorResult executorResult = futureRes.get();
				if(executorResult == null) {
					logger.error("Failed to retrieve results from a " + TSPlanExecutor.class.getName());
				} else {
					
					// add plan exec duration to overall duration for calculating the average duration
					averageDuration = averageDuration + executorResult.getDurationMillis();
					numOfValidResults = numOfValidResults + 1;
					
					// validate the min/max runtimes for each executor and re-set the values if necessary
					if(maxDuration < executorResult.getDurationMillis())
						maxDuration = executorResult.getDurationMillis();
					if(minDuration > executorResult.getDurationMillis())
						minDuration = executorResult.getDurationMillis();
					if(executorResult.getErrors() > 0)
						errors = errors + executorResult.getErrors();

					// count up the average runtimes for a single plan exec run. re-set the min/max timers accordingly
					singleRunAvg = singleRunAvg + executorResult.getSingleRunExecutionDurationAverage();
					if(singleRunMax < executorResult.getSingleRunExecutionDurationMax())
						singleRunMax = executorResult.getSingleRunExecutionDurationMax();
					if(singleRunMin > executorResult.getSingleRunExecutionDurationMin())
						singleRunMin = executorResult.getSingleRunExecutionDurationMin();
					
					result.addExecutorId(executorResult.getPlanExecutorId());
				}
			} catch (InterruptedException e) {
				throw new TSPlanExecutionFailedException("Test plan execution interrupted in: " + executionEnvironmentId + ". Error: " + e.getMessage(), e);
			} catch (ExecutionException e) {
				throw new TSPlanExecutionFailedException("Test plan execution interrupted in: " + executionEnvironmentId + ". Error: " + e.getMessage(), e);
			}
		}
		
		long execEnd = System.currentTimeMillis();
		
		// set collected information
		result.setEndMillis(execEnd);
		result.setAverageDurationMillis(averageDuration / numOfValidResults);
		result.setMinDurationMillis(minDuration);
		result.setMaxDurationMillis(maxDuration);
		result.setErrors(errors);
		result.setSingleRunExecutionDurationAverage(singleRunAvg / numOfValidResults);
		result.setSingleRunExecutionDurationMax(singleRunMax);
		result.setSingleRunExecutionDurationMin(singleRunMin);
		
		if(logger.isDebugEnabled())
			logger.debug("[execEnv: " + result.getExecutionEnvironmentId() + ", testPlan: " + result.getTestPlanName() + ", executors: " + result.getExecutorIds().size() + ", avgDuration: " + result.getAverageDurationMillis() + ", minDuration: " + result.getMinDurationMillis()+ ", maxDuration: " + result.getMaxDurationMillis()+", singleRunAvg: " + result.getSingleRunExecutionDurationAverage() + ", singleRunMin: " + result.getSingleRunExecutionDurationMin() + ", singleRunMax: " + result.getSingleRunExecutionDurationMax() + "]");
			
		
		return result;
	}

}
