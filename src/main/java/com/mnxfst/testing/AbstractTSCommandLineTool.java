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

package com.mnxfst.testing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.mnxfst.testing.client.TSClient;
import com.mnxfst.testing.exception.TSClientConfigurationException;
import com.mnxfst.testing.plan.exec.TSPlanRecurrenceType;

/**
 * Provides a common ground for command-line tools being implemented within the ptest-server context
 * @author ckreutzfeldt
 * @since 13.02.2012
 */
public abstract class AbstractTSCommandLineTool {

	/**
	 * Extracts a long value from the named command-line option
	 * @param cmd
	 * @param opt
	 * @param shortOpt
	 * @return
	 * @throws TSClientConfigurationException
	 */
	public long extractLongValue(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {		
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
	 * Extracts a long value from the named command-line option
	 * @param cmd
	 * @param opt
	 * @param shortOpt
	 * @return
	 * @throws TSClientConfigurationException
	 */
	public int extractIntValue(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {		
		String tmp = cmd.getOptionValue(opt);
		if(tmp == null || tmp.isEmpty())
			tmp = cmd.getOptionValue(shortOpt);
		if(tmp == null || tmp.isEmpty())
			throw new TSClientConfigurationException("Missing value for required option '"+opt+"' ('"+shortOpt+"')");
		
		try {
			return Integer.parseInt(tmp.trim());
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
	public String extractStringValue(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {
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
	public String[] extractStringList(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {
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
	 * @param opt
	 * @param shortOpt
	 * @return
	 * @throws TSClientConfigurationException
	 */
	public TSPlanRecurrenceType extractRecurrenceType(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {
		
		String tmp = cmd.getOptionValue(opt);
		if(tmp == null || tmp.isEmpty())
			tmp = cmd.getOptionValue(shortOpt);
		if(tmp == null || tmp.isEmpty())
			throw new TSClientConfigurationException("Missing value for required option '"+opt+"' ('"+shortOpt+"')");
		
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
		
		throw new TSClientConfigurationException("Unknown value '"+tmp+"' provided for required option '"+opt+"' ('"+shortOpt+"')");		
	}

	/**
	 * Reads additional properties from the referenced file
	 * @param additionalPropertiesFile
	 * @return
	 * @throws TSClientConfigurationException
	 */
	public Properties extractAdditionalProperties(String additionalPropertiesFile) throws TSClientConfigurationException {
		
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

	/**
	 * Parses the provided command-line against the given set of options using the posix compatible parser
	 * @param options
	 * @param commandLineArguments
	 * @return
	 * @throws ParseException
	 */
	public CommandLine parseCommandline(Options options, String[] commandLineArguments) throws ParseException {
		CommandLineParser parser = new PosixParser();
		return parser.parse(options, commandLineArguments);		
	}

	/**
	 * Prints out the command-line help 
	 * @param options
	 */
	public void printHelp(Options options, String additionalMessage) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( TSClient.class.getName(), options );
		
		if(additionalMessage != null && !additionalMessage.isEmpty())
			System.out.println("\n" + additionalMessage);
		
	}

	
}
