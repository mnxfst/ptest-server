package com.mnxfst.testing.plan.exec;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;

import com.mnxfst.testing.exception.TSPlanMissingException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanBuilder;
import com.mnxfst.testing.plan.TSPlanResult;

/**
 * Test cases for {@link TSPlanExecEnvironment}
 * @author mnxfst
 * @since 16.12.2011
 */
public class TestTSPlanExecEnvironment {

	@Test
	public void testExecute() throws Exception {
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);

		TSPlanExecEnvironment env = new TSPlanExecEnvironment("test-env", plan, 1, TSPlanRecurrenceType.TIMES, 1);
		//Assert.assertEquals("The number of parallel threads executing the test plan must be 4", 4, env.getNumberOfParallelExecutors());

		List<TSPlanResult> results = env.call();
		Assert.assertNotNull("The result set must not be null", results);
//		Assert.assertEquals("The number of results must be 4", plan.getNumOfExecEnvironments() , results.size());

		try {
			env = new TSPlanExecEnvironment("test-env", null, 2, TSPlanRecurrenceType.TIMES, 2);
			Assert.fail("Missing test plan class");
		} catch(TSPlanMissingException e) {
			//
		}		
	}
	
	@Test
	public void testParallelExecEnvironments() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
		
		ExecutorService execSvc = Executors.newFixedThreadPool(4);
		
		TSPlanExecEnvironment env1 = new TSPlanExecEnvironment("test-env-1", plan, 2, TSPlanRecurrenceType.TIMES, 2);
		TSPlanExecEnvironment env2 = new TSPlanExecEnvironment("test-env-2", plan, 2, TSPlanRecurrenceType.TIMES, 2);
		TSPlanExecEnvironment env3 = new TSPlanExecEnvironment("test-env-3", plan, 2, TSPlanRecurrenceType.TIMES, 2);
		TSPlanExecEnvironment env4 = new TSPlanExecEnvironment("test-env-4", plan, 2, TSPlanRecurrenceType.TIMES, 2);
		
		List<TSPlanExecEnvironment> envs = new ArrayList<TSPlanExecEnvironment>();
		envs.add(env1);
		envs.add(env2);
		envs.add(env3);
		envs.add(env4);
		
		List<Future<List<TSPlanResult>>> results = execSvc.invokeAll(envs);
		Assert.assertNotNull("The result set must not be null", results);
		Assert.assertEquals("The result must contain 4 elements", 4, results.size());
		
		int samples = 0;
		long duration = 0;
		int i = 1;
		for(Future<List<TSPlanResult>> res : results) {
			List<TSPlanResult> envres = res.get();
			for(TSPlanResult er : envres) {
				Assert.assertNotNull("The result must not be null", er);
				Assert.assertEquals("The exec env must be test-env-"+i, "test-env-"+i, er.getExecutionEnvironmentId());
//				System.out.println("Env: " + er.getExecutionEnvironmentId() + ", duration: " + er.getDurationMillis() + "ms");
				duration = duration + er.getDurationMillis();
				samples = samples + 1;
			}
			i = i+1;
		}
		System.out.println("Avg. duration: " + (duration / samples) + "ms. Samples: " + samples);
	}
	
	@Test
	public void testMassiveParallelExec() throws Exception {
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("src/test/resources/sampleTestPlan.xml");		
		TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
		
		ExecutorService service = Executors.newCachedThreadPool();
		TSPlanExecEnvironment env = new TSPlanExecEnvironment("env-1", plan, 5, TSPlanRecurrenceType.TIMES, 8);
		List<TSPlanResult> result = env.call();
		long avg = 0;
		int count = 0;
		for(TSPlanResult r : result) {
			avg = avg + r.getDurationMillis();
			count = count + 1;
			System.out.println("Executor: " + r.getPlanExecutorId() + ", Duration: " + r.getDurationMillis()+ "ms");
		}
		System.out.println("Average: " + (avg / count) + "ms");
		
		
	}
}
