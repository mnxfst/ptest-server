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

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.w3c.dom.Document;

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

	private static final Logger logger = Logger.getLogger(TSHttpRequestHandler.class);
	
	private static ConcurrentMap<String, TSPlanExecEnvironmentResult> testPlanExecutionResultCache = new ConcurrentHashMap<String, TSPlanExecEnvironmentResult>();
	private static ExecutorService testPlanExecutorService = Executors.newCachedThreadPool();
	
	private static final String REQUEST_PARAM_EXECUTE_TESTPLAN = "execute";
	private static final String REQUEST_PARAM_COLLECT_EXECUTION_RESULTS = "collect";
	private static final String REQUEST_PARAM_THREADS = "threads";
	private static final String REQUEST_PARAM_RECURRENCES = "recurrences";
	private static final String REQUEST_PARAM_RECURRENCE_TYPE = "recurrencetype";
	private static final String REQUEST_PARAM_TESTPLAN = "testplan";
	private static final String REQUEST_PARAM_TESTPLAN_RESULT_ID = "resultIdentifier";
	
	
	private static final int RESPONSE_CODE_EXECUTION_STARTED = 1;
	private static final int RESPONSE_CODE_EXECUTION_RESULTS_CONTAINED = 2;
	private static final int RESPONSE_CODE_EXECUTION_RESULTS_PENDING = 3;
	private static final int RESPONSE_CODE_ERROR = 4;	
	private static final int ERROR_CODE_INVALID_OPTION_CODE = 1;
	private static final int ERROR_CODE_THREADS_MISSING_OR_INVALID = 2; 
	private static final int ERROR_CODE_RECURRENCES_MISSING_OR_INVALID = 3; 
	private static final int ERROR_CODE_RECURRENCE_TYPE_MISSING_OR_INVALID = 4; 
	private static final int ERROR_CODE_TESTPLAN_MISSING = 5; 
	private static final int ERROR_CODE_TESTPLAN_PROCESSING_ERROR = 6;
	private static final int ERROR_CODE_RESULT_ID_MISSING = 7;
	
	private static final String SERVER_SIDE_CONST_VAR_HOSTNAME = "ptestServerHostname";
	private static final String SERVER_SIDE_CONST_VAR_PORT = "ptestServerPort";
	
	private String hostname = null;
	private int port = 0;
	
	public TSHttpRequestHandler(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;

	}
	
	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {

		// extract http request from incoming message, get keep alive attribute as it will be transferred to response and decode query string 		
		HttpRequest httpRequest = (HttpRequest)event.getMessage();
		
		boolean keepAlive = HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(httpRequest.getHeader(HttpHeaders.Names.CONNECTION));		
		QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.getUri());

		// fetch query parameters
		Map<String, List<String>> queryParams = decoder.getParameters();
		
		// handle post request
		if(httpRequest.getMethod() == HttpMethod.POST) {
			decoder = new QueryStringDecoder("?" + httpRequest.getContent().toString(CharsetUtil.UTF_8));
			queryParams.putAll(decoder.getParameters());
		}
				
		if(queryParams.containsKey(REQUEST_PARAM_EXECUTE_TESTPLAN)) {
			executeTestplan(queryParams, keepAlive, event);
		} else if(queryParams.containsKey(REQUEST_PARAM_COLLECT_EXECUTION_RESULTS)) {
			collectTestplanResults(queryParams, keepAlive, event);
		} else {
			List<Integer> codes = new ArrayList<Integer>();
			codes.add(ERROR_CODE_INVALID_OPTION_CODE);
			sendResponse(generateErrorMessage(codes, ""), keepAlive, event);
		}
	}
	
	/**
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		super.exceptionCaught(ctx, e);
		logger.error("Failed to process incoming request. Error: " + e.getCause().getMessage(), e.getCause());
	}

	/**
	 * Executes a test plan using the provided parameters and sends a response to the calling client
	 * @param queryParams
	 */
	private boolean executeTestplan(Map<String, List<String>> queryParams, boolean keepAlive, MessageEvent event) {
		
		int errors = 0;		
		
		boolean threadsValid = true;
		Integer numOfThreads = parseSingleIntValue(queryParams.get(REQUEST_PARAM_THREADS));
		if(numOfThreads == null || numOfThreads.intValue() < 1) {
			errors = errors + 1;
			threadsValid = false;
		}
		
		boolean recurrencesValid = true;
		Integer numOfRecurrences = parseSingleIntValue(queryParams.get(REQUEST_PARAM_RECURRENCES));
		if(numOfRecurrences == null || numOfRecurrences.intValue() < 1) {
			errors = errors + 1;
			recurrencesValid = false;
		}
		
		boolean recurrencesTypeValid = true;
		TSPlanRecurrenceType recurrenceType = parseSingleRecurrenceType(queryParams.get(REQUEST_PARAM_RECURRENCE_TYPE));
		if(recurrenceType == null || recurrenceType == TSPlanRecurrenceType.UNKNOWN) {
			errors = errors + 1;
			recurrencesTypeValid = false;
		}		
		
		boolean testPlanValid = true;
		List<String> values = queryParams.get(REQUEST_PARAM_TESTPLAN);
		String testPlan = (values != null && values.size() > 0 ? values.get(0): null);
		if(testPlan == null || testPlan.isEmpty()) {
			errors = errors + 1;
			testPlanValid = false;
		}
		
		Map<String, Serializable> testPlanVars = new HashMap<String, Serializable>();
		for(String key : queryParams.keySet()) {
			List<String> additionalValues = queryParams.get(key);
			if(additionalValues != null && !additionalValues.isEmpty()) {
				
				// TODO replace by var converter imposed by test plan
				if(key.equalsIgnoreCase("waittime")) {
					try {
						testPlanVars.put(key, Long.parseLong(additionalValues.get(0)));
					} catch(NumberFormatException e) {
						logger.error("Failed to parse waittime: " + e.getMessage());
						testPlanVars.put(key, additionalValues.get(0));		
					}
				} else {
					testPlanVars.put(key, additionalValues.get(0));
				}
			}
		}
		testPlanVars.put(SERVER_SIDE_CONST_VAR_HOSTNAME, hostname);
		testPlanVars.put(SERVER_SIDE_CONST_VAR_PORT, Integer.valueOf(port));
		
		if(errors > 0) {

			List<Integer> codes = new ArrayList<Integer>();

			if(!threadsValid) {
				codes.add(ERROR_CODE_THREADS_MISSING_OR_INVALID);
			}
			if(!recurrencesValid) {
				codes.add(ERROR_CODE_RECURRENCES_MISSING_OR_INVALID);
			}
			if(!recurrencesTypeValid) {
				codes.add(ERROR_CODE_RECURRENCE_TYPE_MISSING_OR_INVALID);
			}
			if(!testPlanValid) {
				codes.add(ERROR_CODE_TESTPLAN_MISSING);
			}
			
			sendResponse(generateErrorMessage(codes, ""), keepAlive, event);
			return false;
		} else {
						
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(testPlan.getBytes()));
				TSPlan plan = TSPlanBuilder.getInstance().buildPlan(doc);
				TSPlanExecEnvironment env = new TSPlanExecEnvironment("ptest-server", plan, numOfRecurrences, recurrenceType, numOfThreads, testPlanVars);
				UUID resultIdentifier = UUID.fromString(new com.eaio.uuid.UUID().toString());
				testPlanExecutorService.execute(new TSPlanExecutionJob(resultIdentifier.toString(), env));
				sendResponse(generateExecutionStartedMessage(resultIdentifier.toString()), keepAlive, event);
				return true;
			} catch(Exception e) {
				logger.error("Failed to parse testplan. Error: " + e.getMessage(), e);
				List<Integer> codes = new ArrayList<Integer>();
				codes.add(ERROR_CODE_TESTPLAN_PROCESSING_ERROR);
				sendResponse(generateErrorMessage(codes, e.getMessage()), keepAlive, event);
				return false;
			}
		}
	}
	
	private boolean collectTestplanResults(Map<String, List<String>> queryParams, boolean keepAlive, MessageEvent event) {
		
		List<String> values = queryParams.get(REQUEST_PARAM_TESTPLAN_RESULT_ID);
		String testResultIdentifier = (values != null && values.size() > 0 ? values.get(0): null);
		if(testResultIdentifier == null || testResultIdentifier.isEmpty()) {
			List<Integer> codes = new ArrayList<Integer>();
			codes.add(ERROR_CODE_RESULT_ID_MISSING);
			sendResponse(generateErrorMessage(codes, ""), keepAlive, event);
			return false;
		}
		
		sendResponse(generateExecutionResultMessage(testPlanExecutionResultCache.get(testResultIdentifier), testResultIdentifier), keepAlive, event);
		return true;
	}
	
	/**
	 * Parses out a single int value from the provided list of values. If the result is null, the list did not contain any value
	 * or the value could not be parsed into a integer object   
	 * @param values
	 * @return
	 */
	private Integer parseSingleIntValue(List<String> values)  {
		
		if(values == null)
			return null;
		
		String tmp = values.get(0);
		if(tmp == null || tmp.isEmpty())
			return null;

		try {
			return Integer.valueOf(values.get(0));
		} catch(NumberFormatException e) {
			
		}
		
		return null;
	}
	
	/**
	 * Parses out a single recurrences type from the provided list of values. If the result is null, the list did not contain any value
	 * or the value could not be parsed into a integer object   
	 * @param values
	 * @return
	 */
	private TSPlanRecurrenceType parseSingleRecurrenceType(List<String> values) {
		if(values == null)
			return null;
		
		String tmp = values.get(0);
		if(tmp == null || tmp.isEmpty())
			return null;
		
		if(tmp.equalsIgnoreCase("TIMES"))
			return TSPlanRecurrenceType.TIMES;
		else if(tmp.equalsIgnoreCase("MILLIS"))
			return TSPlanRecurrenceType.MILLIS;
		else if(tmp.equalsIgnoreCase("SECONDS"))
			return TSPlanRecurrenceType.SECONDS;
		else if(tmp.equalsIgnoreCase("MINUTES"))
			return TSPlanRecurrenceType.MINUTES;
		else if(tmp.equalsIgnoreCase("HOURS"))
			return TSPlanRecurrenceType.HOURS;
		else if(tmp.equalsIgnoreCase("DAYS"))
			return TSPlanRecurrenceType.DAYS;
		
		return null;
	}
	
	/**
	 * Generates an error message and inserts the provided information
	 * @param errorCode
	 * @return
	 */
	private String generateErrorMessage(List<Integer> errorCodes, String errorMessage) {
		StringBuffer buf = new StringBuffer("<testExecutionResponse>");
		buf.append("<responseCode>").append(RESPONSE_CODE_ERROR).append("</responseCode>");
		buf.append("<errorCodes>");		
		if(errorCodes != null && !errorCodes.isEmpty()) {
			for(Integer e : errorCodes)
				buf.append("<errorCode>").append(e.intValue()).append("</errorCode>");
		}
		buf.append("</errorCodes>");
		// TODO provide code specific mesages instead of a global one
		buf.append("<errorMessage>").append(errorMessage).append("</errorMessage>");
		buf.append("</testExecutionResponse>");
		return buf.toString();		
	}
	
	/**
	 * Generates a response message stating that the execution has been started 
	 * @param resultIdentifier
	 * @return
	 */
	private String generateExecutionStartedMessage(String resultIdentifier) {
		StringBuffer buf = new StringBuffer("<testExecutionResponse>");
		buf.append("<responseCode>").append(RESPONSE_CODE_EXECUTION_STARTED).append("</responseCode>");
		buf.append("<resultIdentifier>").append(resultIdentifier).append("</resultIdentifier>");
		buf.append("</testExecutionResponse>");
		return buf.toString();
	}
	
	private String generateExecutionResultMessage(TSPlanExecEnvironmentResult tsResult, String resultIdentifier) {
		StringBuffer buf = new StringBuffer("<testExecutionResponse>");
		buf.append("<resultIdentifier>").append(resultIdentifier).append("</resultIdentifier>");
		if(tsResult == null)
			buf.append("<responseCode>").append(RESPONSE_CODE_EXECUTION_RESULTS_PENDING).append("</responseCode>");
		else {
			buf.append("<responseCode>").append(RESPONSE_CODE_EXECUTION_RESULTS_CONTAINED).append("</responseCode>");
			buf.append("<testplan>").append(tsResult.getTestPlanName()).append("</testplan>");
			buf.append("<executionEnvironment>").append(tsResult.getExecutionEnvironmentId()).append("</executionEnvironment>");
			buf.append("<start>").append(tsResult.getStartMillis()).append("</start>");
			buf.append("<end>").append(tsResult.getEndMillis()).append("</end>");
			buf.append("<averageDuration>").append(tsResult.getAverageDurationMillis()).append("</averageDuration>");
			buf.append("<singleMinDuration>").append(tsResult.getSingleRunExecutionDurationMin()).append("</singleMinDuration>");
			buf.append("<singleMaxDuration>").append(tsResult.getSingleRunExecutionDurationMax()).append("</singleMaxDuration>");
			buf.append("<singleAverageDuration>").append(tsResult.getSingleRunExecutionDurationAverage()).append("</singleAverageDuration>");
			buf.append("<averageDurationMedian>").append(tsResult.getAverageDurationMedian()).append("</averageDurationMedian>");
			buf.append("<errors>").append(tsResult.getErrors()).append("</errors>");
		}
		buf.append("</testExecutionResponse>");
		return buf.toString();
		
	}
	
	/**
	 * Sends a response containing the given message to the calling client
	 * @param responseMessage
	 * @param keepAlive
	 * @param event
	 */
	private void sendResponse(String responseMessage, boolean keepAlive, MessageEvent event) {
		HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		
		httpResponse.setContent(ChannelBuffers.copiedBuffer(responseMessage, CharsetUtil.UTF_8));
		httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		
		if(keepAlive)
			httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.getContent().readableBytes());
		
		ChannelFuture future = event.getChannel().write(httpResponse);
		if(!keepAlive)
			future.addListener(ChannelFutureListener.CLOSE);
	}
	
	protected static void addResponse(String identifier, TSPlanExecEnvironmentResult result) {
		testPlanExecutionResultCache.put(identifier, result);
	}
	
	
	
}
