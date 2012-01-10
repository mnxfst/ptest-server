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

import java.util.ArrayList;
import java.util.List;
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
	
	/**
	 * Initializes the execution environment
	 * @param executionEnvironmentId
	 * @param testPlan
	 * @param recurrences
	 * @param recurrenceType
	 * @param numOfParallelExecutors
	 * @throws TSPlanMissingException
	 * @throws TSPlanInstantiationException
	 */
	public TSPlanExecEnvironment(String executionEnvironmentId, final TSPlan testPlan, int recurrences, TSPlanRecurrenceType recurrenceType, int numOfParallelExecutors) throws TSPlanMissingException, TSPlanInstantiationException {

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
			testPlanExecutors.add(new TSPlanExecutor(testPlan, executionEnvironmentId, executionEnvironmentId + "-executor-"+i, recurrences, recurrenceType));
		
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

		TSPlanExecEnvironmentResult result = new TSPlanExecEnvironmentResult(executionEnvironmentId, testPlanName);
		result.setStartMillis(execStart);
		
		long averageDuration = 0;
		int numOfValidResults = 0;
		long maxDuration = 0;
		long minDuration = Long.MAX_VALUE;
		int errors = 0;
		
		// iterate through results, extract them, provide missing data, move to overall result set
		for(Future<TSPlanExecutorResult> futureRes : testPlanInvocResults) {
			try {
				TSPlanExecutorResult executorResult = futureRes.get();
				if(executorResult == null) {
					logger.error("Failed to retrieve results from a " + TSPlanExecutor.class.getName());
				} else {
					averageDuration = averageDuration + executorResult.getDurationMillis();
					numOfValidResults = numOfValidResults + 1;
					
					if(maxDuration < executorResult.getDurationMillis())
						maxDuration = executorResult.getDurationMillis();
					if(minDuration > executorResult.getDurationMillis())
						minDuration = executorResult.getDurationMillis();
					if(executorResult.getErrors() > 0)
						errors = errors + executorResult.getErrors();
					
					result.addExecutorId(executorResult.getPlanExecutorId());
				}
			} catch (InterruptedException e) {
				throw new TSPlanExecutionFailedException("Test plan execution interrupted in: " + executionEnvironmentId + ". Error: " + e.getMessage(), e);
			} catch (ExecutionException e) {
				throw new TSPlanExecutionFailedException("Test plan execution interrupted in: " + executionEnvironmentId + ". Error: " + e.getMessage(), e);
			}
		}
		
		long execEnd = System.currentTimeMillis();
		result.setEndMillis(execEnd);
		result.setAverageDurationMillis(averageDuration / numOfValidResults);
		result.setMinDurationMillis(minDuration);
		result.setMaxDurationMillis(maxDuration);
		result.setErrors(errors);
		
		if(logger.isDebugEnabled())
			logger.debug("[execEnv: " + result.getExecutionEnvironmentId() + ", testPlan: " + result.getTestPlanName() + ", executors: " + result.getExecutorIds().size() + ", avgDuration: " + result.getAverageDurationMillis() + ", minDuration: " + result.getMinDurationMillis()+ ", maxDuration: " + result.getMaxDurationMillis()+"]");
			
		
		return result;
	}

}
