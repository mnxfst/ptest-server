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

package com.mnxfst.testing.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.mnxfst.testing.exception.TSClientConfigurationException;
import com.mnxfst.testing.plan.exec.client.TSClient;

/**
 * Ramps up the ecommerce services platform test execution server
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSMain {
	
	public static final String CMD_OPT_PORT = "port";
	public static final String CMD_OPT_PORT_SHORT = "p";

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new TSMain().execute(args);

	}
	
	public void execute(String[] args) {
		
		Options commandLineOptions = getTSClientCommandLineOptions();
		int port = -1;
		try {
			CommandLine commandLine = parseCommandline(commandLineOptions, args);
			port = extractIntValue(commandLine, CMD_OPT_PORT, CMD_OPT_PORT_SHORT);
		} catch(ParseException e) {
			System.out.println("Failed to parse command-line");
		} catch (TSClientConfigurationException e) {
			printHelp(commandLineOptions, "Failed to parse port from command-line");
			return;
		}
		
		if(port < 1) {
			printHelp(commandLineOptions, "Failed to parse port from command-line");
			return;
		}
			

		// TODO fetch thread pool size and protocol from command line or properties
		ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		
		ServerBootstrap serverBootstrap = new ServerBootstrap(channelFactory);
		serverBootstrap.setPipelineFactory(new TSPipelineFactory());
		serverBootstrap.setOption("child.tcpNoDelay", true);
		serverBootstrap.setOption("child.keepAlive", true);
		
		serverBootstrap.bind(new InetSocketAddress(port));

		
	}

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
	 * Extracts a long value from the named command-line option
	 * @param cmd
	 * @param opt
	 * @param shortOpt
	 * @return
	 * @throws TSClientConfigurationException
	 */
	protected int extractIntValue(CommandLine cmd, String opt, String shortOpt) throws TSClientConfigurationException {		
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
	 * Returns the command-line options configured for the client
	 * @return
	 */
	protected Options getTSClientCommandLineOptions() {
		Options options = new Options();
		options.addOption(CMD_OPT_PORT_SHORT, CMD_OPT_PORT, true, "Port to be used for setting up communication");
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
