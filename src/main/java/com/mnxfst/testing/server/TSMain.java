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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.mnxfst.testing.AbstractTSCommandLineTool;
import com.mnxfst.testing.exception.TSClientConfigurationException;

/**
 * Ramps up the ecommerce services platform test execution server
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSMain extends AbstractTSCommandLineTool {
	
	public static final String CMD_OPT_PORT = "port";
	public static final String CMD_OPT_PORT_SHORT = "p";
	public static final String CMD_OPT_HOSTNAME = "hostname";
	public static final String CMD_OPT_HOSTNAME_SHORT = "h";
	public static final String CMD_OPT_THREAD_POOL_SIZE = "poolSize";
	public static final String CMD_OPT_THREAD_POOL_SIZE_SHORT = "ps";

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new TSMain().execute(args);

	}
	
	public void execute(String[] args) {
		
		Options commandLineOptions = getTSClientCommandLineOptions();
		CommandLine commandLine = null;
		try {
			commandLine = parseCommandline(commandLineOptions, args);
		} catch(ParseException e) {
			System.out.println("Failed to parse command-line");
		}

		String hostname = null;
		try {
			hostname = extractStringValue(commandLine, CMD_OPT_HOSTNAME, CMD_OPT_HOSTNAME_SHORT);
		} catch(TSClientConfigurationException e) {
			printHelp(commandLineOptions, "Failed to parse host name from command-line");
			return;
		}

		int port = -1;
		try {
			port = extractIntValue(commandLine, CMD_OPT_PORT, CMD_OPT_PORT_SHORT);
		} catch (TSClientConfigurationException e) {
			printHelp(commandLineOptions, "Failed to parse port from command-line");
			return;
		}

		if(port < 1) {
			printHelp(commandLineOptions, "Failed to parse port from command-line");
			return;
		}

		int threadPoolSize = -1;
		try {
			threadPoolSize = extractIntValue(commandLine, CMD_OPT_THREAD_POOL_SIZE, CMD_OPT_THREAD_POOL_SIZE_SHORT);
		} catch(TSClientConfigurationException e) {
			threadPoolSize = -1;
		}
		
		System.out.println("ptest-server");
		System.out.println("hostname: " + hostname);
		System.out.println("port: " + port);
		System.out.println("server socket thread pool size: " + threadPoolSize);

		ChannelFactory channelFactory = null;
		if(threadPoolSize > 0)
			channelFactory = new NioServerSocketChannelFactory(Executors.newFixedThreadPool(threadPoolSize), Executors.newFixedThreadPool(threadPoolSize));
		else
			channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		
		ServerBootstrap serverBootstrap = new ServerBootstrap(channelFactory);
		serverBootstrap.setPipelineFactory(new TSPipelineFactory(hostname, port));
		serverBootstrap.setOption("child.tcpNoDelay", true);
		serverBootstrap.setOption("child.keepAlive", true);
		
		serverBootstrap.bind(new InetSocketAddress(port));

		
	}

	/**
	 * Returns the command-line options configured for the client
	 * @return
	 */
	protected Options getTSClientCommandLineOptions() {
		Options options = new Options();
		options.addOption(CMD_OPT_HOSTNAME_SHORT, CMD_OPT_HOSTNAME, true, "Host name to be provided to each test plan context as global variable");
		options.addOption(CMD_OPT_PORT_SHORT, CMD_OPT_PORT, true, "Port to be used for setting up communication");
		options.addOption(CMD_OPT_THREAD_POOL_SIZE_SHORT, CMD_OPT_THREAD_POOL_SIZE, true, "Size used for setting up the server socket thread pool (optional)");
		return options;
	}
	
}
