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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.mnxfst.testing.AbstractTSCommandLineTool;
import com.mnxfst.testing.exception.TSClientConfigurationException;
import com.mnxfst.testing.exception.TSClientExecutionException;
import com.mnxfst.testing.plan.exec.TSPlanRecurrenceType;

/**
 * Provides a command-line client to control the test servers 
 * @author mnxfst
 * @since 10.02.2012
 */
public class TSClient extends AbstractTSCommandLineTool {

	public static final String CMD_OPT_MODE_EXECUTE = "execute";
	public static final String CMD_OPT_MODE_COLLECT = "collect";
	public static final String CMD_OPT_MODE_SATURATION_TEST = "saturationTest";
	public static final String CMD_OPT_THREADS = "threads";
	public static final String CMD_OPT_THREADS_SHORT = "t";
	public static final String CMD_OPT_RECURRENCES = "recurrences";
	public static final String CMD_OPT_RECURRENCES_SHORT = "r";
	public static final String CMD_OPT_RECURRENCE_TYPE = "recurrenceType";
	public static final String CMD_OPT_RECURRENCE_TYPE_SHORT = "rt";
	public static final String CMD_OPT_TESTPLAN = "testPlan";
	public static final String CMD_OPT_TESTPLAN_SHORT = "tp";
	public static final String CMD_OPT_PTEST_SERVER_HOSTS = "ptestHosts";
	public static final String CMD_OPT_PTEST_SERVER_HOSTS_SHORT = "h";
	public static final String CMD_OPT_PTEST_SERVER_PORT = "ptestPort";
	public static final String CMD_OPT_PTEST_SERVER_PORT_SHORT = "p";
	public static final String CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE = "addPropsFile";
	public static final String CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE_SHORT = "apfile";
	public static final String CMD_OPT_PTEST_SERVER_URL_ENCODING = "urlEncoding";
	public static final String CMD_OPT_PTEST_SERVER_URL_ENCODING_SHORT = "uenc";
	public static final String CMD_OPT_RESULT_IDENTIFIER = "resultIdentifier";
	public static final String CMD_OPT_RESULT_IDENTIFIER_SHORT = "rid";
	public static final String CMD_OPT_SAT_TEST_MAX_RUNTIME = "maxRuntime";
	public static final String CMD_OPT_SAT_TEST_MAX_RUNTIME_SHORT = "maxrun";
	public static final String CMD_OPT_SAT_TEST_THREAD_INCREMENT = "threadIncrement";
	public static final String CMD_OPT_SAT_TEST_THREAD_INCREMENT_SHORT = "ti";

	public static final String REQUEST_PARAMETER_EXECUTE = "execute";
	public static final String REQUEST_PARAMETER_COLLECT = "collect";
	public static final String REQUEST_PARAMETER_RESULT_IDENTIFIER = "resultIdentifier";
	public static final String REQUEST_PARAMETER_THREADS = "threads";
	public static final String REQUEST_PARAMETER_RECURRENCES = "recurrences";
	public static final String REQUEST_PARAMETER_RECURRENCE_TYPE = "recurrencetype";
	public static final String REQUEST_PARAMETER_TESTPLAN = "testplan";

	public static void main(String[] args) throws ClientProtocolException, IOException, SAXException, ParserConfigurationException, ParseException {

		TSClient client = new TSClient();
		
		client.executeClient(args);

		System.exit(0);
	// TODO parse error message from result
	}
	
	public void executeClient(String[] args) {
		
		Options commandLineOptions = getTSClientCommandLineOptions();
		try {
			CommandLine commandLine = parseCommandline(commandLineOptions, args);
			
			if(!commandLine.hasOption(CMD_OPT_MODE_EXECUTE) && !commandLine.hasOption(CMD_OPT_MODE_COLLECT) && !commandLine.hasOption(CMD_OPT_MODE_SATURATION_TEST)) {
				printHelp(commandLineOptions, "Please select one mode: test plan execution, saturation load test or result collection");
				return;
			}
			
			int modeCount = 0;
			if(commandLine.hasOption(CMD_OPT_MODE_EXECUTE))
				modeCount = modeCount + 1;
			if(commandLine.hasOption(CMD_OPT_MODE_COLLECT))
				modeCount = modeCount + 1;
			if(commandLine.hasOption(CMD_OPT_MODE_SATURATION_TEST))
				modeCount = modeCount + 1;
			
			if(modeCount > 1) {
				printHelp(commandLineOptions, "Please select only one mode: test plan execution, saturation load test or result collection");
				return;
			}
			
			if(commandLine.hasOption(CMD_OPT_MODE_EXECUTE) || commandLine.hasOption(CMD_OPT_MODE_SATURATION_TEST)) {
				Map<String, String> results = executeTestPlan(commandLineOptions, commandLine);
				if(results != null && !results.isEmpty()) {
					System.out.println("Requested test plan successfully started on referenced hosts. The results contain the host as well as the result identifier:");
					for(String hostName : results.keySet())
						System.out.println("Host: " + hostName + ", Result Identifier: " + results.get(hostName));
				}
			} else if(commandLine.hasOption(CMD_OPT_MODE_COLLECT)){
				
				Set<TSClientPlanExecutionResult> results = executeResultCollection(commandLineOptions, commandLine);
				for(TSClientPlanExecutionResult r : results)
					System.out.println(r);
			}
			
		} catch(ParseException e) {
			System.out.println("Failed to parse command-line. Error: " + e.getMessage());
		}
		
	}
	
	///////////////////////////////////////////////// TEST PLAN EXECUTION /////////////////////////////////////////////////
	
	/**
	 * Executes the result collection
	 * @param options
	 * @param cmd
	 * @return
	 */
	protected Set<TSClientPlanExecutionResult> executeResultCollection(Options options, CommandLine cmd) {

		String ptestServerHosts[] = null;
		try {
			ptestServerHosts = extractStringList(cmd, CMD_OPT_PTEST_SERVER_HOSTS, CMD_OPT_PTEST_SERVER_HOSTS_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide a list of hosts running a ptest-server instance");
			return null;
		}
		if(ptestServerHosts == null || ptestServerHosts.length < 1) {
			printHelp(options, "Please provide a list of hosts running a ptest-server instance");
			return null;
		}		
		if(ptestServerHosts.length > 1) {
			printHelp(options, "Please provide only one host for collecting results at command-line level");
			return null;
		}
		
		int ptestServerPort = -1;
		try {
			ptestServerPort = extractIntValue(cmd, CMD_OPT_PTEST_SERVER_PORT, CMD_OPT_PTEST_SERVER_PORT_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide port to be used for ptest-server communication");
			return null;
		}
		if(ptestServerPort < 0){
			printHelp(options, "Please provide port to be used for ptest-server communication");
			return null;
		}
		
		String resultIdentifier = null;
		try {
			resultIdentifier = extractStringValue(cmd, CMD_OPT_RESULT_IDENTIFIER, CMD_OPT_RESULT_IDENTIFIER_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide a result identifier");
			return null;
		}
		
		Map<String, String> hostResultIdMapping = new HashMap<String, String>();
		hostResultIdMapping.put(ptestServerHosts[0], resultIdentifier);
		
		return collectTestplanResults(hostResultIdMapping, ptestServerPort);
	}
	
	/**
	 * Implements the test plan execution incl. command-line options handling
	 * @param commandLine
	 */
	protected Map<String, String> executeTestPlan(Options options, CommandLine cmd) {
		
		// extract number of threads to start
		int threads = -1;
		try {
			threads = extractIntValue(cmd, CMD_OPT_THREADS, CMD_OPT_THREADS_SHORT);
		} catch(TSClientConfigurationException e) {
		}
		if(threads < 1) {
			printHelp(options, "Please provide a valid number of threads used for test plan execution");
			return null;
		}
			
		// extract recurrences information
		long recurrences = -1;
		try {
			recurrences = extractLongValue(cmd, CMD_OPT_RECURRENCES, CMD_OPT_RECURRENCES_SHORT);
		} catch(TSClientConfigurationException e) {			
		}
		if(recurrences < 1) {
			printHelp(options, "Please provide a valid number of test plan recurrences");
			return null;
		}
	
		TSPlanRecurrenceType recurrenceType = null;
		try {
			recurrenceType = extractRecurrenceType(cmd, CMD_OPT_RECURRENCE_TYPE, CMD_OPT_RECURRENCE_TYPE_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide a valid recurrence type");
			return null;	
		}
		if(recurrenceType == TSPlanRecurrenceType.UNKNOWN) {
			printHelp(options, "Please provide a valid recurrence type");
			return null;	
		}
		
		String testPlan = null;
		try {
			testPlan = extractStringValue(cmd, CMD_OPT_TESTPLAN, CMD_OPT_TESTPLAN_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please reference a valid test plan");
			return null;	
		}
		if(testPlan == null || testPlan.isEmpty()) {
			printHelp(options, "Please reference a valid test plan");
			return null;	
		}

		String ptestServerHosts[] = null;
		try {
			ptestServerHosts = extractStringList(cmd, CMD_OPT_PTEST_SERVER_HOSTS, CMD_OPT_PTEST_SERVER_HOSTS_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide a list of hosts running a ptest-server instance");
			return null;
		}
		if(ptestServerHosts == null || ptestServerHosts.length < 1) {
			printHelp(options, "Please provide a list of hosts running a ptest-server instance");
			return null;
		}
		
		long ptestServerPort = -1;
		try {
			ptestServerPort = extractLongValue(cmd, CMD_OPT_PTEST_SERVER_PORT, CMD_OPT_PTEST_SERVER_PORT_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide port to be used for ptest-server communication");
			return null;
		}
		if(ptestServerPort < 0){
			printHelp(options, "Please provide port to be used for ptest-server communication");
			return null;
		}
		
		String urlEncoding = cmd.getOptionValue(CMD_OPT_PTEST_SERVER_URL_ENCODING);
		if(urlEncoding == null || urlEncoding.isEmpty())
			urlEncoding = cmd.getOptionValue(CMD_OPT_PTEST_SERVER_URL_ENCODING_SHORT);
		if(urlEncoding == null || urlEncoding.isEmpty())
			urlEncoding = "UTF-8";
		
		String additionalPropertiesFile = cmd.getOptionValue(CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE);
		if(additionalPropertiesFile == null || additionalPropertiesFile.isEmpty())
			additionalPropertiesFile = cmd.getOptionValue(CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE_SHORT);
		Properties additionalProperties = null;
		if(additionalPropertiesFile != null && !additionalPropertiesFile.isEmpty()) {
			try {
				additionalProperties = extractAdditionalProperties(additionalPropertiesFile);
			} catch(TSClientConfigurationException e) {
				printHelp(options, "Please provide a valid path to a file containing additional properties");
				return null;
			}
		} else {
			additionalProperties = new Properties();
		}
		
		byte[] testplanContent = null;
		try {
			testplanContent = loadTestplan(testPlan);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Error received while attempting to read test plan file: " + e.getMessage());
			return null;
		}
		
		if(cmd.hasOption(CMD_OPT_MODE_EXECUTE)) {
			try {
				return executeTestPlan(ptestServerHosts, (int)ptestServerPort, threads, recurrences, recurrenceType, testplanContent, additionalProperties, urlEncoding);
			} catch (TSClientConfigurationException e) {
				System.out.println("Error while configuring the client: " + e.getMessage());
			} catch (TSClientExecutionException e) {
				System.out.println("Error while executing the client: " + e.getMessage());
			}
		} else if(cmd.hasOption(CMD_OPT_MODE_SATURATION_TEST)) {

			long maxRuntime = -1;
			try {
				maxRuntime = extractLongValue(cmd, CMD_OPT_SAT_TEST_MAX_RUNTIME, CMD_OPT_SAT_TEST_MAX_RUNTIME_SHORT);
			} catch(TSClientConfigurationException e) {
				printHelp(options, "Please provide a max. runtime allowed for test plan execution");
				return null;
			}
			if(maxRuntime < 0) {
				printHelp(options, "Please provide a max. runtime allowed for test plan execution");
				return null;
			}
			
			int threadIncrement = 1;
			try {
				threadIncrement = extractIntValue(cmd, CMD_OPT_SAT_TEST_THREAD_INCREMENT, CMD_OPT_SAT_TEST_THREAD_INCREMENT_SHORT);
			} catch(TSClientConfigurationException e) {
				System.out.println("No or invalid value provided for '"+CMD_OPT_SAT_TEST_THREAD_INCREMENT+"/"+CMD_OPT_SAT_TEST_THREAD_INCREMENT_SHORT+"'");
				threadIncrement = 1;
			}
			

			StringBuffer hn = new StringBuffer();
			for(int i = 0; i < ptestServerHosts.length; i++) {
				hn.append(ptestServerHosts[i]);
				if(i < ptestServerHosts.length - 1)
					hn.append(", ");
			}

			
			System.out.println("ptest-client");
			System.out.println("test mode:               saturation test");
			System.out.println("ptest-server instances:  " + hn.toString());
			System.out.println("ptest-server port:       " + ptestServerPort);
			System.out.println("max. threads:            " + threads);
			System.out.println("thread increment:        " + threadIncrement);
			System.out.println("allowed max. runtime:    " + maxRuntime);
			System.out.println("recurrences per thread:  " + recurrences);			
			System.out.println("recurrence type:         " + recurrenceType);
			System.out.println("test plan:               " + testPlan);
			System.out.println("url encoding:            " + urlEncoding);
			
			long maxThreads = 0;
			boolean interrupt = false;
			
			try {
				
				if(threadIncrement > threads) {
					threadIncrement = threads;
					System.out.println("\nthread increment refixed to " + threadIncrement + " since it was greater than num. of threads " + threads);
				}
				
				System.out.println("\n\n");
				System.out.println("Saturation test execution:\n");
				
				for(int i = threadIncrement; i <= threads; i = i + threadIncrement) {
					
					System.out.println("Hosts: " + ptestServerHosts.length + ", Threads: " + i + ", Recurrences: " + recurrences + ", Recurrence Type: " + recurrenceType + ", Allowed Max. Runtime: " + maxRuntime);
					System.out.println("Result identifiers (per host):");
					
					Map<String, String> resultIdentifiers = executeTestPlan(ptestServerHosts, (int)ptestServerPort, i, recurrences, recurrenceType, testplanContent, additionalProperties, urlEncoding);
					for(String hostName : resultIdentifiers.keySet())
						System.out.println("\t\t" + hostName + ": " + resultIdentifiers.get(hostName));
					
					// TODO problem with TIMES!!
					long waitMillis = 0;
					
					switch(recurrenceType) {
						case TIMES: {
							waitMillis = recurrences * 500;
						}
						case MILLIS: {
							waitMillis = recurrences;				
							break;
						}
						case SECONDS: {
							waitMillis = recurrences * 1000;
							break;
						}
						case MINUTES: {
							waitMillis = recurrences * 1000 * 60;
							break;
						}
						case HOURS: {
							waitMillis = recurrences * 1000 * 60 * 60;
							break;
						}
						case DAYS: {
							waitMillis = recurrences * 1000 * 60 * 60 * 24;
							break;
						}
					}
					
					waitMillis = waitMillis + 5000; // add 5sec to be sure :-)
					
					System.out.println("Waiting for approx. "+  (waitMillis / 1000) + " seconds before fetching the results");

					try {
						Thread.sleep(waitMillis);
					} catch(InterruptedException e) {
						System.out.println("Interrupted while waiting. " + e.getMessage());
					}
					
					Set<TSClientPlanExecutionResult> results = collectTestplanResults(resultIdentifiers, (int)ptestServerPort);
					System.out.println("Results:");
					for(TSClientPlanExecutionResult r : results)
						System.out.println("\t\tHost: " + r.getHostName() + ", min: " + r.getSingleMinDuration() + "ms, max: " + r.getSingleMaxDuration() + "ms, median: " + r.getAverageDurationMedian() + "ms (surpasses max runtime for median: "+(r.getAverageDurationMedian() >= maxRuntime)+")" );
					
					for(TSClientPlanExecutionResult r : results) {
						if(r.getAverageDurationMedian() >= maxRuntime) {
							maxThreads = i;
							interrupt = true;
							break;
						}
					}
					
					if(interrupt)
						break;
				}
				
				System.out.println("Saturation test finished. Max. number of threads on "+  ptestServerHosts.length+ " hosts the destinations are capable to serve: " + maxThreads);
			} catch (TSClientConfigurationException e) {
				System.out.println("Error while configuring the client: " + e.getMessage());
			} catch (TSClientExecutionException e) {
				System.out.println("Error while executing the client: " + e.getMessage());
			}
			
		}
		
		return new HashMap<String, String>();
	}
	
	
		
	///////////////////////////////////////////////// TEST PLAN RESULT COLLECTION /////////////////////////////////////////////////

	
	///////////////////////////////////////////////// EXECUTE HTTP CALL /////////////////////////////////////////////////
	
	/**
	 * Executes the referenced test plan for all given host names. The result contains a mapping from a host name to the returned result identifier
	 * of that ptest-server instance 
	 * @param hostNames
	 * @param port
	 * @param threads
	 * @param recurrences
	 * @param recurrenceType
	 * @param testplan
	 * @param additionalParameters
	 * @param urlEncoding
	 * @return
	 * @throws TSClientConfigurationException
	 * @throws TSClientExecutionException
	 */
	protected Map<String, String> executeTestPlan(String[] hostNames, int port, long threads, long recurrences, TSPlanRecurrenceType recurrenceType, byte[] testplan, Properties additionalParameters, String urlEncoding) throws TSClientConfigurationException, TSClientExecutionException  {
		
		// the ptest-server understands http get, thus we use it TODO refactor to post and send testplan as well and do not reference it anymore!
		StringBuffer buffer = new StringBuffer("/?");
		buffer.append(REQUEST_PARAMETER_EXECUTE).append("=1");
		buffer.append("&").append(REQUEST_PARAMETER_RECURRENCES).append("=").append(recurrences);
		buffer.append("&").append(REQUEST_PARAMETER_RECURRENCE_TYPE).append("=").append(recurrenceType.toString());
		buffer.append("&").append(REQUEST_PARAMETER_THREADS).append("=").append(threads);
		
		try {
			if(additionalParameters != null && !additionalParameters.isEmpty()) {
				for(Object key : additionalParameters.keySet()) {
					String value = (String)additionalParameters.get(key);
					buffer.append("&").append(key).append("=").append(URLEncoder.encode(value, urlEncoding));
				}
			}
		} catch(UnsupportedEncodingException e) {
			throw new TSClientConfigurationException("Unsupported encoding type: " + urlEncoding + ". Error: " + e.getMessage());
		}

		StringBuffer hn = new StringBuffer();
		for(int i = 0; i < hostNames.length; i++) {
			hn.append(hostNames[i]);
			if(i < hostNames.length - 1)
				hn.append(", ");
		}
		
		System.out.println("Execute testplan:");
		System.out.println("\thostNames: " + hn.toString());
		System.out.println("\tport: " + port);
		System.out.println("\tthreads: " +threads);
		System.out.println("\trecurrences: " + recurrences);
		System.out.println("\trecurrenceType: " + recurrenceType);
		System.out.println("\turl enc: " + urlEncoding);
		System.out.println("\n\turi: "+ buffer.toString());
		
		
		TSClientPlanExecCallable[] testplanCallables = new TSClientPlanExecCallable[hostNames.length];
		for(int i = 0; i < hostNames.length; i++) {
			testplanCallables[i] = new TSClientPlanExecCallable(hostNames[i], port, buffer.toString(), testplan);
		}
		
		ExecutorService executorService = Executors.newFixedThreadPool(hostNames.length);
		List<Future<NameValuePair>> executionResults = new ArrayList<Future<NameValuePair>>();
		try {
			executionResults = executorService.invokeAll(Arrays.asList(testplanCallables));
		} catch (InterruptedException e) {
			System.out.println("Test execution interrupted: " + e.getMessage());
		}
		
		// collect results from callables
		Map<String, String> result = new HashMap<String, String>();
		for(Future<NameValuePair> r : executionResults) {			
			try {
				NameValuePair nvp = r.get();
				result.put(nvp.getName(), nvp.getValue());
			} catch (InterruptedException e) {
				System.out.println("Interrupted while waiting for results. Error: " + e.getMessage());
			} catch (ExecutionException e) {
				System.out.println("Interrupted while waiting for results. Error: " + e.getMessage());
			}			
		}
		
		return result;
	}
	
	/**
	 * Collects results of previously started tests
	 * @param hostResultIdentifierMapping
	 * @param port
	 * @return
	 */
	protected Set<TSClientPlanExecutionResult> collectTestplanResults(Map<String, String> hostResultIdentifierMapping, int port) {
		
		TSClientPlanResultCollectCallable[] callables = new TSClientPlanResultCollectCallable[hostResultIdentifierMapping.size()];
		int count = 0;
		String uri = null;
		for(String host : hostResultIdentifierMapping.keySet()) {
			uri = "/?"+REQUEST_PARAMETER_COLLECT+"=1&"+REQUEST_PARAMETER_RESULT_IDENTIFIER+"="+hostResultIdentifierMapping.get(host);
			callables[count] = new TSClientPlanResultCollectCallable(host, port, uri);
			count = count + 1;
		}
		
		
		
		ExecutorService executorService = Executors.newFixedThreadPool(hostResultIdentifierMapping.size());
		List<Future<TSClientPlanExecutionResult>> collectedResults = new ArrayList<Future<TSClientPlanExecutionResult>>();
		try {
			collectedResults = executorService.invokeAll(Arrays.asList(callables));
		} catch(InterruptedException e) {
			System.out.println("Test execution result collection interrupted: " + e.getMessage());
		}
		
		Set<TSClientPlanExecutionResult> result = new HashSet<TSClientPlanExecutionResult>();
		for(Future<TSClientPlanExecutionResult> r : collectedResults) {
			try {
				result.add(r.get());
			} catch(InterruptedException e) {
				System.out.println("Interrupted while collection results: " + e.getMessage());
			} catch(ExecutionException e) {
				System.out.println("Interrupted while collection results: " + e.getMessage());
			}
		}
		
		return result;		
	}
	
	/**
	 * Reads in a test plan
	 * @param fileName
	 * @return
	 * @throws TSClientConfigurationException
	 */
	protected byte[] loadTestplan(String fileName) throws TSClientConfigurationException {
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(fileName);
			return IOUtils.toByteArray(fin);
		} catch(IOException e) {
			throw new TSClientConfigurationException("Failed to read test plan file '"+fileName+"'. Error: " + e.getMessage());
		} finally {
			if(fin != null) {
				try {
					fin.close();
				} catch(IOException e) {
					System.out.println("Failed to close test plan file '"+fileName+"'. Error: " + e.getMessage());
				}
			}
		}
		
	}
	
			
	///////////////////////////////////////////////// COMMAND-LINE PREPARATION /////////////////////////////////////////////////
	
	
	/**
	 * Returns the command-line options configured for the client
	 * @return
	 */
	protected Options getTSClientCommandLineOptions() {
		Options options = new Options();
		options.addOption(CMD_OPT_MODE_EXECUTE, false, "Executes a test plan on the ptest-server instance(s)");
		options.addOption(CMD_OPT_MODE_COLLECT, false, "Collects the results from the ptest-server instance(s)");
		options.addOption(CMD_OPT_MODE_SATURATION_TEST, false, "Executes a performance test finding the saturation load");
		options.addOption(CMD_OPT_THREADS_SHORT, CMD_OPT_THREADS, true, "Number of threads used for executing the test case. In case of the saturation load this will be used as max, starting with one thread");
		options.addOption(CMD_OPT_RECURRENCES_SHORT, CMD_OPT_RECURRENCES, true, "Number of tesplan recurrences");
		options.addOption(CMD_OPT_RECURRENCE_TYPE_SHORT, CMD_OPT_RECURRENCE_TYPE, true, "Recurrence type (TIMES, MILLIS, SECONDS, MINUTES, HOURS, DAYS)");
		options.addOption(CMD_OPT_TESTPLAN_SHORT, CMD_OPT_TESTPLAN, true, "References the file containing the test plan definition");
		options.addOption(CMD_OPT_PTEST_SERVER_HOSTS_SHORT, CMD_OPT_PTEST_SERVER_HOSTS, true, "Comma-separated list of hosts running an available ptest-server instance");
		options.addOption(CMD_OPT_PTEST_SERVER_PORT_SHORT, CMD_OPT_PTEST_SERVER_PORT, true, "Names the port to use for communication with the ptest-server instances");
		options.addOption(CMD_OPT_PTEST_SERVER_URL_ENCODING_SHORT, CMD_OPT_PTEST_SERVER_URL_ENCODING, true, "Encoding to be used for url parameters");
		options.addOption(CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE_SHORT, CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE, true, "Path to file which contains additional key/value pairs to be forwared to the ptest-server");
		options.addOption(CMD_OPT_RESULT_IDENTIFIER_SHORT, CMD_OPT_RESULT_IDENTIFIER, true, "Key to identify results on the ptest-server (to be used with 'collect' option only)");
		options.addOption(CMD_OPT_SAT_TEST_MAX_RUNTIME_SHORT, CMD_OPT_SAT_TEST_MAX_RUNTIME, true, "Max. average runtime accepted for saturation test before quitting test");
		options.addOption(CMD_OPT_SAT_TEST_THREAD_INCREMENT_SHORT, CMD_OPT_SAT_TEST_THREAD_INCREMENT, true, "Number to increase the running threads by during saturation test (default: 1). The value will be used as initial value.");
		options.addOption("ri", true, "Response identifier used by the ptest-server to store results");
		return options;
	}
	
}
