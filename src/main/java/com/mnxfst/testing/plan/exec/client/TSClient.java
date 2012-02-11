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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import com.mnxfst.testing.exception.TSClientConfigurationExeception;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
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

	public static void main(String[] args) throws ClientProtocolException, IOException, SAXException, ParserConfigurationException, ParseException {

		TSClient client = new TSClient();
		
		for(int i = 0; i < args.length; i++)
			System.out.println(args[i]);
		
		
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
		} catch(TSClientConfigurationExeception e) {
		}
		if(threads < 1) {
			printHelp(options, "Please provide a valid number of threads used for test plan execution");
			return;
		}
			
		// extract recurrences information
		long recurrences = -1;
		try {
			recurrences = extractLongValue(cmd, CMD_OPT_RECURRENCES, CMD_OPT_RECURRENCES_SHORT);
		} catch(TSClientConfigurationExeception e) {			
		}
		if(recurrences < 1) {
			printHelp(options, "Please provide a valid number of test plan recurrences");
			return;
		}
	
		
		
	}
	
	///////////////////////////////////////////////// PARSING HELPERS /////////////////////////////////////////////////
	
	/**
	 * Extracts a long value from the named command-line option
	 * @param cmd
	 * @param opt
	 * @param shortOpt
	 * @return
	 * @throws TSClientConfigurationExeception
	 */
	protected long extractLongValue(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationExeception {		
		String tmp = cmd.getOptionValue(opt);
		if(tmp == null || tmp.isEmpty())
			tmp = cmd.getOptionValue(shortOpt);
		if(tmp == null || tmp.isEmpty())
			throw new TSClientConfigurationExeception("Missing value for required option '"+opt+"' ('"+shortOpt+"')");
		
		try {
			return Long.parseLong(tmp.trim());
		} catch(NumberFormatException e) {
			throw new TSClientConfigurationExeception("Value for required option '"+opt+"' ('"+shortOpt+"') does not represent a valid numerical value: " + tmp);
		}		
	}
	
	/**
	 * Extracts the recurrence type from the command-line
	 * @param cmd
	 * @return
	 * @throws TSClientConfigurationExeception
	 */
	protected TSPlanRecurrenceType extractRecurrenceType(CommandLine cmd) throws TSClientConfigurationExeception {
		
		String tmp = cmd.getOptionValue(CMD_OPT_RECURRENCE_TYPE);
		if(tmp == null || tmp.isEmpty())
			tmp = cmd.getOptionValue(CMD_OPT_RECURRENCE_TYPE_SHORT);
		if(tmp == null || tmp.isEmpty())
			throw new TSClientConfigurationExeception("Missing value for required option '"+CMD_OPT_RECURRENCE_TYPE+"' ('"+CMD_OPT_RECURRENCE_TYPE_SHORT+"')");
		
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
		
		throw new TSClientConfigurationExeception("Unknown value '"+tmp+"' provided for required option '"+CMD_OPT_RECURRENCE_TYPE+"' ('"+CMD_OPT_RECURRENCE_TYPE_SHORT+"')");
		
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
		options.addOption("rt", "recurrenceType", true, "Recurrence type (TIMES, MILLIS, SECONDS, MINUTES, HOURS, DAYS)");
		options.addOption("p", "testPlan", true, "Names the test plan to execute");
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
