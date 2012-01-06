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
