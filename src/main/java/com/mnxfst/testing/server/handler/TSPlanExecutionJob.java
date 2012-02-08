package com.mnxfst.testing.server.handler;

import org.apache.log4j.Logger;

import com.mnxfst.testing.plan.TSPlanExecEnvironmentResult;
import com.mnxfst.testing.plan.exec.TSPlanExecEnvironment;

public class TSPlanExecutionJob implements Runnable {

	private static final Logger logger = Logger.getLogger(TSPlanExecutionJob.class);
	
	private String identifier = null;
	private TSPlanExecEnvironment env = null;
	
	public TSPlanExecutionJob(String identifier, TSPlanExecEnvironment env) {
		this.env = env;
		this.identifier = identifier;
	}
	
	public void run() {

		try {
			TSPlanExecEnvironmentResult result = env.execute();
			TSHttpRequestHandler.addResponse(identifier, result);
		} catch(Exception e) {
			TSPlanExecEnvironmentResult res = new TSPlanExecEnvironmentResult(env.getExecutionEnvironmentId(), env.getTestPlanName());
			res.setErrors(1);
			TSHttpRequestHandler.addResponse(identifier, res);
			logger.error(e.getMessage(), e);
		}		
	}

}
