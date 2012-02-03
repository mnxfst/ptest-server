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

package com.mnxfst.testing.server.handler;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpServerCodec;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.w3c.dom.Document;

import com.mnxfst.testing.exception.TSPlanServerConfigurationException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.TSPlanBuilder;
import com.mnxfst.testing.plan.TSPlanExecEnvironmentResult;
import com.mnxfst.testing.plan.exec.TSPlanExecEnvironment;
import com.mnxfst.testing.plan.exec.TSPlanRecurrenceType;

/**
 * Provides a handler for incoming HTTP requests
 * @author mnxfst
 * @since 16.12.2011
 */
public class TSHttpRequestHandler extends SimpleChannelUpstreamHandler {

	/**
	 * Executed for every incoming HTTP request
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
				
		HttpRequest httpRequest = (HttpRequest)event.getMessage();		
		boolean keepAlive = HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(httpRequest.getHeader(HttpHeaders.Names.CONNECTION));		
		QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.getUri());	
		
		Map<String, List<String>> params = decoder.getParameters();

		int threads = -1;
		int recurrences = -1;
		TSPlanRecurrenceType recurrenceType = TSPlanRecurrenceType.TIMES;
		String testPlanPath = null;
		HttpResponseStatus status = HttpResponseStatus.OK;
		String responseMessage = "OK";
		Map<String, String> testPlanVars = new HashMap<String, String>();

		// TODO enhance :-)
		for(String key : params.keySet()) {
			List<String> values = params.get(key);
			
			if(key.equalsIgnoreCase("threads")) {
				if(values != null && values.size() > 0) {
					try {
						threads = Integer.parseInt(values.get(0));
					} catch(NumberFormatException e) {
						status = HttpResponseStatus.BAD_REQUEST;
						responseMessage = "Invalid value provided for parameter 'threads'";
					}
				} else {
					status = HttpResponseStatus.BAD_REQUEST;
					responseMessage = "Required parameter 'threads' missing";
				}					
			} else if(key.equalsIgnoreCase("recurrences")) {
				if(values != null && values.size() > 0) {
					try {
						recurrences = Integer.parseInt(values.get(0));
					} catch(NumberFormatException e) {
						status = HttpResponseStatus.BAD_REQUEST;
						responseMessage = "Invalid value provided for parameter 'recurrences'";
					}
				} else {
					status = HttpResponseStatus.BAD_REQUEST;
					responseMessage = "Required parameter 'recurrences' missing";
				}	
			} else if(key.equalsIgnoreCase("recurrencetype")) {
				if(values != null && values.size() > 0) {
					String tmp = values.get(0);
					if(tmp.equalsIgnoreCase("TIMES"))
						recurrenceType = TSPlanRecurrenceType.TIMES;
					else if(tmp.equalsIgnoreCase("MILLIS"))
						recurrenceType = TSPlanRecurrenceType.MILLIS;
					else if(tmp.equalsIgnoreCase("SECONDS"))
						recurrenceType = TSPlanRecurrenceType.SECONDS;
					else if(tmp.equalsIgnoreCase("MINUTES"))
						recurrenceType = TSPlanRecurrenceType.MINUTES;
					else if(tmp.equalsIgnoreCase("HOURS"))
						recurrenceType = TSPlanRecurrenceType.HOURS;
					else if(tmp.equalsIgnoreCase("DAYS"))
						recurrenceType = TSPlanRecurrenceType.DAYS;
					else {
						status = HttpResponseStatus.BAD_REQUEST;
						responseMessage = "Required parameter 'recurrenceType' contains an invalid type: " + tmp;
					}
				}
			} else if(key.equalsIgnoreCase("testplanpath")) {
				if(values != null && values.size() > 0) {
					testPlanPath = values.get(0);
				} else {
					status = HttpResponseStatus.BAD_REQUEST;
					responseMessage = "Required parameter 'testPlanPath' missing";
				}
			} else {
				if(values != null && values.size() > 0)
					testPlanVars.put(key, values.get(0));
			}			
		}
		
		if(threads == -1) {
			status = HttpResponseStatus.BAD_REQUEST;
			responseMessage = "Required parameter 'threads' missing";
		} else if(recurrences == -1) {
			status = HttpResponseStatus.BAD_REQUEST;
			responseMessage = "Required parameter 'recurrences' missing";
		} else if(testPlanPath == null || testPlanPath.isEmpty()) {
			status = HttpResponseStatus.BAD_REQUEST;
			responseMessage = "Required parameter 'testPlanPath' missing";
		}
		
		
		if(status == HttpResponseStatus.OK) {
			
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(testPlanPath);
				TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
				TSPlanExecEnvironment env = new TSPlanExecEnvironment("ptest-server", plan, recurrences, recurrenceType, threads);
				TSPlanExecEnvironmentResult e = env.execute();
				
				StringBuffer r = new StringBuffer();
				r.append("Test plan: " + e.getTestPlanName());
				r.append("Execution environment: " + e.getExecutionEnvironmentId());
				r.append("Start: " + new Date(e.getStartMillis()));
				r.append("End: " + new Date(e.getEndMillis()));
				r.append("Duration: " + (e.getEndMillis() - e.getStartMillis()) + "ms");
				r.append("Average duration: " + e.getAverageDurationMillis() + "ms");
				r.append("Max. duration (single run): " + e.getSingleRunExecutionDurationMax() + "ms");
				r.append("Min. duration (single run): " + e.getSingleRunExecutionDurationMin() + "ms");
				r.append("Avg. duration (single run): " + e.getSingleRunExecutionDurationAverage() + "ms");
				r.append("Errors: " + e.getErrors());
				responseMessage = r.toString();
			} catch(Exception e) {
				status = HttpResponseStatus.BAD_REQUEST;
				responseMessage = "Failed to process test plan '"+testPlanPath+"'. Error: " + e.getMessage();
			}
		}
		
		HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		httpResponse.setContent(ChannelBuffers.copiedBuffer(responseMessage, CharsetUtil.UTF_8));
		httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		
		if(keepAlive)
			httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.getContent().readableBytes());
		
		ChannelFuture future = event.getChannel().write(httpResponse);
		if(!keepAlive)
			future.addListener(ChannelFutureListener.CLOSE);
		
	}
	
	/*
		
		TSPlanExecEnvironment env = new TSPlanExecEnvironment("exec-1", plan, recurrences, TSPlanRecurrenceType.TIMES, threads, vars);
		TSPlanExecEnvironmentResult e = env.execute();
*/
	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		
	}

	
	
}
