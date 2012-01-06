package com.mnxfst.testing.server;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.mnxfst.testing.server.handler.TSHttpRequestHandler;

/**
 * Provides a programmatically defined setup for the request handling channel pipeline
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSPipelineFactory implements ChannelPipelineFactory {

	/**
	 * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
	 */
	public ChannelPipeline getPipeline() throws Exception {
		
		ChannelPipeline channelPipeline = Channels.pipeline();
		
		channelPipeline.addLast("decoder", new HttpRequestDecoder());
		channelPipeline.addLast("encoder", new HttpResponseEncoder());
		channelPipeline.addLast("deflater", new HttpContentCompressor());
		channelPipeline.addLast("handler", new TSHttpRequestHandler());
		
		return channelPipeline;
		
	}

}
