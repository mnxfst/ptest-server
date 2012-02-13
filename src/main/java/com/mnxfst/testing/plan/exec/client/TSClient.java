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

package com.mnxfst.testing.plan.exec.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.mnxfst.testing.exception.TSClientConfigurationException;
import com.mnxfst.testing.exception.TSClientExecutionException;
import com.mnxfst.testing.plan.exec.TSPlanRecurrenceType;

/**
 * Provides a command-line client to control the test servers 
 * @author mnxfst
 * @since 10.02.2012
 */
public class TSClient {

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

	public static void main(String[] args) throws ClientProtocolException, IOException, SAXException, ParserConfigurationException, ParseException {

		TSClient client = new TSClient();
		
		for(int i = 0; i < args.length; i++)
			System.out.println(args[i]);
		
		
	
	}
	
	public void executeClient(String[] args) {
		
		Options commandLineOptions = getTSClientCommandLineOptions();
		try {
			CommandLine commandLine = parseCommandline(commandLineOptions, args);
			
			if(!commandLine.hasOption("execute") || !commandLine.hasOption("collect")) {
				printHelp(commandLineOptions, "Please select one mode: test plan execution or result collection");
				return;
			}
			
			if(commandLine.hasOption("execute") && commandLine.hasOption("collect")) {
				printHelp(commandLineOptions, "Please select only one mode: test plan execution or result collection");
				return;
			}
			
			if(commandLine.hasOption("execute")) {
				executeTestPlan(commandLineOptions, commandLine);
			} else {
				// TODO collect
			}
			
		} catch(ParseException e) {
			System.out.println("Failed to parse command-line");
		}
		
	}
	
	///////////////////////////////////////////////// TEST PLAN EXECUTION /////////////////////////////////////////////////
	
	/**
	 * Implements the test plan execution incl. command-line options handling
	 * @param commandLine
	 */
	protected void executeTestPlan(Options options, CommandLine cmd) {
		
		// extract number of threads to start
		long threads = -1;
		try {
			threads = extractLongValue(cmd, CMD_OPT_THREADS, CMD_OPT_THREADS_SHORT);
		} catch(TSClientConfigurationException e) {
		}
		if(threads < 1) {
			printHelp(options, "Please provide a valid number of threads used for test plan execution");
			return;
		}
			
		// extract recurrences information
		long recurrences = -1;
		try {
			recurrences = extractLongValue(cmd, CMD_OPT_RECURRENCES, CMD_OPT_RECURRENCES_SHORT);
		} catch(TSClientConfigurationException e) {			
		}
		if(recurrences < 1) {
			printHelp(options, "Please provide a valid number of test plan recurrences");
			return;
		}
	
		TSPlanRecurrenceType recurrenceType = null;
		try {
			recurrenceType = extractRecurrenceType(cmd);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide a valid recurrence type");
			return;			
		}
		if(recurrenceType == TSPlanRecurrenceType.UNKNOWN) {
			printHelp(options, "Please provide a valid recurrence type");
			return;			
		}
		
		String testPlan = null;
		try {
			testPlan = extractStringValue(cmd, CMD_OPT_TESTPLAN, CMD_OPT_TESTPLAN_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide a valid test plan");
			return;			
		}
		if(testPlan == null || testPlan.isEmpty()) {
			printHelp(options, "Please provide a valid test plan");
			return;			
		}

		String ptestServerHosts[] = null;
		try {
			ptestServerHosts = extractStringList(cmd, CMD_OPT_PTEST_SERVER_HOSTS, CMD_OPT_PTEST_SERVER_HOSTS_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide a list of hosts running a ptest-server instance");
			return;
		}
		if(ptestServerHosts == null || ptestServerHosts.length < 1) {
			printHelp(options, "Please provide a list of hosts running a ptest-server instance");
			return;
		}
		
		long ptestServerPort = -1;
		try {
			ptestServerPort = extractLongValue(cmd, CMD_OPT_PTEST_SERVER_PORT, CMD_OPT_PTEST_SERVER_PORT_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(options, "Please provide port to be used for ptest-server communication");
			return;
		}
		if(ptestServerPort < 0){
			printHelp(options, "Please provide port to be used for ptest-server communication");
			return;
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
				return;
			}
		} else {
			additionalProperties = new Properties();
		}
		
		
	}
	
	private static final String REQUEST_PARAMETER_EXECUTE = "execute";
	private static final String REQUEST_PARAMETER_THREADS = "threads";
	private static final String REQUEST_PARAMETER_RECURRENCES = "recurrences";
	private static final String REQUEST_PARAMETER_RECURRENCE_TYPE = "recurrencetype";
	private static final String REQUEST_PARAMETER_TESTPLAN = "testplan";
	
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
	protected Map<String, String> executeTestplan(String[] hostNames, int port, long threads, long recurrences, TSPlanRecurrenceType recurrenceType, String testplan, Properties additionalParameters, String urlEncoding) throws TSClientConfigurationException, TSClientExecutionException  {
		
//		// build the http hosts for the provided names
//		HttpHost[] ptestServerHosts = new HttpHost[hostNames.length];
//		for(int i = 0; i < hostNames.length; i++) {
//			ptestServerHosts[i] = new HttpHost(hostNames[i], port);
//		}
		
		// the ptest-server understands http get, thus we use it TODO refactor to post and send testplan as well and do not reference it anymore!
		StringBuffer buffer = new StringBuffer("/?");
		buffer.append(REQUEST_PARAMETER_EXECUTE).append("=1");
		buffer.append("&").append(REQUEST_PARAMETER_RECURRENCES).append("=").append(threads);
		buffer.append("&").append(REQUEST_PARAMETER_RECURRENCE_TYPE).append("=").append(recurrenceType.toString());
		buffer.append("&").append(REQUEST_PARAMETER_TESTPLAN).append("=").append(testplan);
		
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
		
		TSClientPlanExecCallable[] testplanCallables = new TSClientPlanExecCallable[hostNames.length];
		for(int i = 0; i < hostNames.length; i++) {
			testplanCallables[i] = new TSClientPlanExecCallable(hostNames[i], port, buffer.toString());
		}
		
		ExecutorService executorService = Executors.newFixedThreadPool(hostNames.length);
		List<Future<NameValuePair>> executionResults = new ArrayList<Future<NameValuePair>>();
		try {
			executionResults = executorService.invokeAll(Arrays.asList(testplanCallables));
		} catch (InterruptedException e) {
			System.out.println("Test execution interrupted: " + e.getMessage());
		}
		
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
		/*
		
		
		
		HttpGet getMethod = new HttpGet("/?execute=1&threads=4&recurrences=1&recurrencetype=times&testplan=AddressIntTestPlan.xml&scenarioId=sec1");
		HttpHost host = new HttpHost("localhost", 9090);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(host, getMethod);
		InputStream s = response.getEntity().getContent();

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(s);
		s.close();
		Node rootNode = doc.getFirstChild();
		System.out.println(rootNode.getNodeName());
		NodeList childs = rootNode.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++) {
			System.out.println(childs.item(i));
		}*/		
	}
	
	///////////////////////////////////////////////// PARSING HELPERS /////////////////////////////////////////////////
	
	/**
	 * Extracts a long value from the named command-line option
	 * @param cmd
	 * @param opt
	 * @param shortOpt
	 * @return
	 * @throws TSClientConfigurationException
	 */
	protected long extractLongValue(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {		
		String tmp = cmd.getOptionValue(opt);
		if(tmp == null || tmp.isEmpty())
			tmp = cmd.getOptionValue(shortOpt);
		if(tmp == null || tmp.isEmpty())
			throw new TSClientConfigurationException("Missing value for required option '"+opt+"' ('"+shortOpt+"')");
		
		try {
			return Long.parseLong(tmp.trim());
		} catch(NumberFormatException e) {
			throw new TSClientConfigurationException("Value for required option '"+opt+"' ('"+shortOpt+"') does not represent a valid numerical value: " + tmp);
		}		
	}
	
	/**
	 * Extracts a string value from the named command-line option
	 * @param cmd
	 * @param opt
	 * @param shortOpt
	 * @return
	 * @throws TSClientConfigurationException
	 */
	protected String extractStringValue(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {
		String tmp = cmd.getOptionValue(opt);
		if(tmp == null || tmp.isEmpty())
			tmp = cmd.getOptionValue(shortOpt);
		if(tmp == null || tmp.isEmpty())
			throw new TSClientConfigurationException("Missing value for required option '"+opt+"' ('"+shortOpt+"')");

		return tmp.trim();		
	}
	
	/**
	 * Extracts a list of comma-separated string values from a given command-line option
	 * @param cmd
	 * @param opt
	 * @param shortOpt
	 * @return
	 * @throws TSClientConfigurationException
	 */
	protected String[] extractStringList(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {
		String tmp = extractStringValue(cmd, opt, shortOpt);
		String[] csvList = tmp.split(",");
		if(csvList == null || csvList.length < 1)
			throw new TSClientConfigurationException("Missing value for required option '"+opt+"' ('"+shortOpt+"')");
			
		for(int i = 0; i < csvList.length; i++)
			csvList[i] = csvList[i].trim();
		return csvList;
	}
	
	/**
	 * Extracts the recurrence type from the command-line
	 * @param cmd
	 * @return
	 * @throws TSClientConfigurationException
	 */
	protected TSPlanRecurrenceType extractRecurrenceType(CommandLine cmd) throws TSClientConfigurationException {
		
		String tmp = cmd.getOptionValue(CMD_OPT_RECURRENCE_TYPE);
		if(tmp == null || tmp.isEmpty())
			tmp = cmd.getOptionValue(CMD_OPT_RECURRENCE_TYPE_SHORT);
		if(tmp == null || tmp.isEmpty())
			throw new TSClientConfigurationException("Missing value for required option '"+CMD_OPT_RECURRENCE_TYPE+"' ('"+CMD_OPT_RECURRENCE_TYPE_SHORT+"')");
		
		tmp = tmp.trim().toLowerCase();
		if(tmp.equalsIgnoreCase(TSPlanRecurrenceType.TIMES.toString()))
			return TSPlanRecurrenceType.TIMES;
		else if(tmp.equalsIgnoreCase(TSPlanRecurrenceType.MILLIS.toString()))
			return TSPlanRecurrenceType.MILLIS;
		else if(tmp.equalsIgnoreCase(TSPlanRecurrenceType.SECONDS.toString()))
			return TSPlanRecurrenceType.SECONDS;
		else if(tmp.equalsIgnoreCase(TSPlanRecurrenceType.MINUTES.toString()))
			return TSPlanRecurrenceType.MINUTES;
		else if(tmp.equalsIgnoreCase(TSPlanRecurrenceType.HOURS.toString()))
			return TSPlanRecurrenceType.HOURS;
		else if(tmp.equalsIgnoreCase(TSPlanRecurrenceType.DAYS.toString()))
			return TSPlanRecurrenceType.DAYS;
		
		throw new TSClientConfigurationException("Unknown value '"+tmp+"' provided for required option '"+CMD_OPT_RECURRENCE_TYPE+"' ('"+CMD_OPT_RECURRENCE_TYPE_SHORT+"')");		
	}
	
	/**
	 * Reads additional properties from the referenced file
	 * @param additionalPropertiesFile
	 * @return
	 * @throws TSClientConfigurationException
	 */
	protected Properties extractAdditionalProperties(String additionalPropertiesFile) throws TSClientConfigurationException {
		
		if(additionalPropertiesFile == null || additionalPropertiesFile.isEmpty() || additionalPropertiesFile.trim().isEmpty())
			throw new TSClientConfigurationException("No file name provided");
		
		try {
			FileInputStream fIn = new FileInputStream(additionalPropertiesFile);
			Properties props = new Properties();
			props.load(fIn);
			return props;
		} catch(FileNotFoundException e) {
			throw new TSClientConfigurationException("No such file '"+additionalPropertiesFile+"'");
		} catch(IOException e) {
			throw new TSClientConfigurationException("Error while reading from '"+additionalPropertiesFile+"': " + e.getMessage());
		}		
		
	}
			
	///////////////////////////////////////////////// COMMAND-LINE PREPARATION /////////////////////////////////////////////////
	
	/**
	 * Parses the provided command-line against the given set of options using the posix compatible parser
	 * @param options
	 * @param commandLineArguments
	 * @return
	 * @throws ParseException
	 */
	protected CommandLine parseCommandline(Options options, String[] commandLineArguments) throws ParseException {
		CommandLineParser parser = new PosixParser();
		return parser.parse(options, commandLineArguments);		
	}
	
	/**
	 * Returns the command-line options configured for the client
	 * @return
	 */
	protected Options getTSClientCommandLineOptions() {
		Options options = new Options();
		options.addOption("execute", false, "Executes a test plan on the ptest-server instance(s)");
		options.addOption("collect", false, "Collects the results from the ptest-server instance(s)");
		options.addOption(CMD_OPT_THREADS_SHORT, CMD_OPT_THREADS, true, "Number of threads used for executing the test case");
		options.addOption(CMD_OPT_RECURRENCES_SHORT, CMD_OPT_RECURRENCES, true, "Number of tesplan recurrences");
		options.addOption(CMD_OPT_RECURRENCE_TYPE_SHORT, CMD_OPT_RECURRENCE_TYPE, true, "Recurrence type (TIMES, MILLIS, SECONDS, MINUTES, HOURS, DAYS)");
		options.addOption(CMD_OPT_TESTPLAN_SHORT, CMD_OPT_TESTPLAN, true, "Names the test plan to execute");
		options.addOption(CMD_OPT_PTEST_SERVER_HOSTS_SHORT, CMD_OPT_PTEST_SERVER_HOSTS, true, "Comma-separated list of hosts running an available ptest-server instance");
		options.addOption(CMD_OPT_PTEST_SERVER_PORT_SHORT, CMD_OPT_PTEST_SERVER_PORT, true, "Names the port to use for communication with the ptest-server instances");
		options.addOption(CMD_OPT_PTEST_SERVER_URL_ENCODING_SHORT, CMD_OPT_PTEST_SERVER_URL_ENCODING, true, "Encoding to be used for url parameters");
		options.addOption(CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE_SHORT, CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE, true, "Path to file which contains additional key/value pairs to be forwared to the ptest-server");
		options.addOption("ri", true, "Response identifier used by the ptest-server to store results");
		return options;
	}
	
	/**
	 * Prints out the command-line help 
	 * @param options
	 */
	protected void printHelp(Options options, String additionalMessage) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( TSClient.class.getName(), options );
		
		if(additionalMessage != null && !additionalMessage.isEmpty())
			System.out.println("\n" + additionalMessage);
		
	}
}
