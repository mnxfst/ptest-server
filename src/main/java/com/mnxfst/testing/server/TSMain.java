package com.mnxfst.testing.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * Ramps up the ecommerce services platform test execution server
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO fetch thread pool size and protocol from command line or properties
		ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		
		ServerBootstrap serverBootstrap = new ServerBootstrap(channelFactory);
		serverBootstrap.setPipelineFactory(new TSPipelineFactory());
		serverBootstrap.setOption("child.tcpNoDelay", true);
		serverBootstrap.setOption("child.keepAlive", true);
		
		serverBootstrap.bind(new InetSocketAddress(9090));

	}

}
