package com.mnxfst.testing.plan.exec;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.mnxfst.testing.exception.TSPlanExecutionFailedException;
import com.mnxfst.testing.exception.TSPlanInstantiationException;
import com.mnxfst.testing.exception.TSPlanMissingException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanResult;

/**
 * Provides a test plan execution environment 
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSPlanExecEnvironment implements Callable<List<TSPlanResult>> {

	private static final Logger logger = Logger.getLogger(TSPlanExecEnvironment.class); 
	
	private int numberOfParallelExecutors = 0;
	private String executionEnvironmentId = null;
	private ExecutorService executorService = null;
	private List<TSPlanExecutor> testPlanExecutors = new ArrayList<TSPlanExecutor>();
	
	public TSPlanExecEnvironment(String executionEnvironmentId, final TSPlan testPlan, int recurrences, TSPlanRecurrenceType recurrenceType, int numOfParallelExecutors) throws TSPlanMissingException, TSPlanInstantiationException {

		if(executionEnvironmentId == null || executionEnvironmentId.isEmpty())
			throw new TSPlanInstantiationException("Failed to instantiate test plan due to missing execution environment identifier");
		
		if(testPlan == null)
			throw new TSPlanMissingException("Missing required test plan");
		
		this.executionEnvironmentId = executionEnvironmentId;
		this.numberOfParallelExecutors = numOfParallelExecutors;
				
		// initialize executor service
		this.executorService = Executors.newFixedThreadPool(this.numberOfParallelExecutors);		
		for(int i = 0; i < this.numberOfParallelExecutors; i++)
			testPlanExecutors.add(new TSPlanExecutor(testPlan, executionEnvironmentId, executionEnvironmentId + "-executor-"+i, recurrences, recurrenceType));
		
		if(logger.isDebugEnabled())
			logger.debug("New test plan execution environment instantiated: [execEnvId="+executionEnvironmentId+", numOfParallelExecutors="+testPlanExecutors.size()+", testPlan="+testPlan.getName()+", recurrences="+recurrences+", recurrenceType="+recurrenceType+"]");
	}
	
	/**
	 * @see java.util.concurrent.Callable#call()
	 */
	public List<TSPlanResult> call() throws Exception {
		
		// overall result set
		List<TSPlanResult> execEnvResults = new ArrayList<TSPlanResult>();
		
		// test plan invocation results
		List<Future<TSPlanResult>> testPlanInvocResults = null;
		try {
			testPlanInvocResults = executorService.invokeAll(testPlanExecutors);			 
		} catch (InterruptedException e) {
			throw new TSPlanExecutionFailedException("Test plan execution interrupted in: " + executionEnvironmentId + ". Error: " + e.getMessage(), e);
		}
		
		// no results found ==> error
		if(testPlanInvocResults == null)
			throw new TSPlanExecutionFailedException("Test plan execution failed for all threads in: " + executionEnvironmentId);		
				
		// iterate through results, extract them, provide missing data, move to overall result set
		for(Future<TSPlanResult> res : testPlanInvocResults) {
			try {
				execEnvResults.add(res.get());
			} catch (InterruptedException e) {
				throw new TSPlanExecutionFailedException("Test plan execution interrupted in: " + executionEnvironmentId + ". Error: " + e.getMessage(), e);
			} catch (ExecutionException e) {
				throw new TSPlanExecutionFailedException("Test plan execution interrupted in: " + executionEnvironmentId + ". Error: " + e.getMessage(), e);
			}
		}
		
		if(logger.isDebugEnabled())
			logger.debug("Received " + execEnvResults.size() + " results from " + testPlanExecutors.size() + " executors for runtime environment '"+executionEnvironmentId+"'");
			
		
		return execEnvResults;
	}


	public int getNumberOfParallelExecutors() {
		return numberOfParallelExecutors;
	}

}
