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

package com.mnxfst.testing.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.mnxfst.testing.exception.TSClientExecutionException;

/**
 * Collects the test plan execution results from the server
 * @author ckreutzfeldt
 * @since 13.02.2012
 */
public class TSClientPlanResultCollectCallable implements Callable<TSClientPlanExecutionResult> {

	protected static final String TEST_EXEC_RESPONSE_ROOT = "testExecutionResponse";
	protected static final String TEST_EXEC_RESPONSE_CODE = "/testExecutionResponse/responseCode";
	protected static final String TEST_EXEC_RESULT_IDENTIFIER = "/testExecutionResponse/resultIdentifier";
	protected static final String TEST_EXEC_TEST_PLAN = "/testExecutionResponse/testplan";
	protected static final String TEST_EXEC_ENVIRONMENT = "/testExecutionResponse/executionEnvironment";
	protected static final String TEST_EXEC_START = "/testExecutionResponse/start";
	protected static final String TEST_EXEC_END = "/testExecutionResponse/end";
	protected static final String TEST_EXEC_AVERAGE_DURATION = "/testExecutionResponse/averageDuration";
	protected static final String TEST_EXEC_SINGLE_MIN_DURATION = "/testExecutionResponse/singleMinDuration";
	protected static final String TEST_EXEC_SINGLE_MAX_DURATION = "/testExecutionResponse/singleMaxDuration";
	protected static final String TEST_EXEC_SINGLE_AVERAGE_DURATION = "/testExecutionResponse/singleAverageDuration";
	protected static final String TEST_EXEC_AVERAGE_MEDIAN = "/testExecutionResponse/averageDurationMedian";
	protected static final String TEST_EXEC_ERRORS = "/testExecutionResponse/errors";
	
	private static final int RESPONSE_CODE_EXECUTION_RESULTS_CONTAINED = 2;
	private static final int RESPONSE_CODE_EXECUTION_RESULTS_PENDING = 3;

	private HttpGet getMethod = null;
	private HttpHost httpHost = null;
	private DefaultHttpClient httpClient = null;
	
	public TSClientPlanResultCollectCallable(String hostname, int port, String uri) {
		this.httpHost = new HttpHost(hostname, port);
		this.getMethod = new HttpGet(uri.toString());		

		// TODO setting?
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		
		ThreadSafeClientConnManager threadSafeClientConnectionManager = new ThreadSafeClientConnManager(schemeRegistry);
		threadSafeClientConnectionManager.setMaxTotal(20);
		threadSafeClientConnectionManager.setDefaultMaxPerRoute(20);
		this.httpClient = new DefaultHttpClient(threadSafeClientConnectionManager);	
	}
	
	/**
	 * TODO TEST!!!
	 * @see java.util.concurrent.Callable#call()
	 */
	public TSClientPlanExecutionResult call() throws Exception {
		
		InputStream ptestServerInputStream = null;
		try {
			HttpResponse response = httpClient.execute(httpHost, getMethod);
			ptestServerInputStream = response.getEntity().getContent();
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			Document responseDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ptestServerInputStream);			
			if(response == null)
				throw new TSClientExecutionException("No response document received from " + httpHost.getHostName());
			
			// fetch root node
			Node rootNode = responseDoc.getFirstChild();
			if(rootNode == null)
				throw new TSClientExecutionException("No valid root node found in document received from " + httpHost.getHostName());
			if(rootNode.getNodeName() == null || !rootNode.getNodeName().equalsIgnoreCase(TEST_EXEC_RESPONSE_ROOT))
				throw new TSClientExecutionException("No valid root node found in document received from " + httpHost.getHostName());

			int responseCode = parseIntValue(rootNode, TEST_EXEC_RESPONSE_CODE, xpath);
			String resultIdentifier = parseStringValue(rootNode, TEST_EXEC_RESULT_IDENTIFIER, xpath);
			switch(responseCode) {
				case RESPONSE_CODE_EXECUTION_RESULTS_PENDING: {
					TSClientPlanExecutionResult planExecutionResult = new TSClientPlanExecutionResult();
					planExecutionResult.setResponseCode(RESPONSE_CODE_EXECUTION_RESULTS_PENDING);
					planExecutionResult.setResultIdentifier(resultIdentifier);
					planExecutionResult.setHostName(httpHost.getHostName());
					planExecutionResult.setPort(httpHost.getPort());
					return planExecutionResult;
				}
				case RESPONSE_CODE_EXECUTION_RESULTS_CONTAINED: {

					long averageDuration = parseLongValue(rootNode, TEST_EXEC_AVERAGE_DURATION, xpath);
					double averageDurationMedian = parseDoubleValue(rootNode, TEST_EXEC_AVERAGE_MEDIAN, xpath);
					long endTimestamp = parseLongValue(rootNode, TEST_EXEC_END, xpath);
					long startTimestamp = parseLongValue(rootNode, TEST_EXEC_START, xpath);
					int errors = parseIntValue(rootNode, TEST_EXEC_ERRORS, xpath);
					String executionEnvironmentId = parseStringValue(rootNode, TEST_EXEC_ENVIRONMENT, xpath);
					long singleAverageDuration = parseLongValue(rootNode, TEST_EXEC_SINGLE_AVERAGE_DURATION, xpath);
					long singleMaxDuration = parseLongValue(rootNode, TEST_EXEC_SINGLE_MAX_DURATION, xpath);
					long singleMinDuration = parseLongValue(rootNode, TEST_EXEC_SINGLE_MIN_DURATION, xpath);
					String testPlan = parseStringValue(rootNode, TEST_EXEC_TEST_PLAN, xpath);
					
					TSClientPlanExecutionResult planExecutionResult = new TSClientPlanExecutionResult();
					planExecutionResult.setResponseCode(RESPONSE_CODE_EXECUTION_RESULTS_PENDING);
					planExecutionResult.setResultIdentifier(resultIdentifier);
					planExecutionResult.setHostName(httpHost.getHostName());
					planExecutionResult.setPort(httpHost.getPort());

					planExecutionResult.setAverageDuration(averageDuration);
					planExecutionResult.setAverageDurationMedian(averageDurationMedian);
					planExecutionResult.setEndTimestamp(endTimestamp);
					planExecutionResult.setErrors(errors);
					planExecutionResult.setExecutionEnvironmentId(executionEnvironmentId);
					planExecutionResult.setSingleAverageDuration(singleAverageDuration);
					planExecutionResult.setSingleMaxDuration(singleMaxDuration);
					planExecutionResult.setSingleMinDuration(singleMinDuration);
					planExecutionResult.setStartTimestamp(startTimestamp);
					planExecutionResult.setTestPlan(testPlan);
					
					return planExecutionResult;
				}
				default: {
					throw new TSClientExecutionException("Unexpected response code '"+responseCode+"' received from " + httpHost.getHostName() + ":" + httpHost.getPort());
				}
			}
			
		} catch(ClientProtocolException e) {
			throw new TSClientExecutionException("Failed to call " + httpHost.getHostName() + ":" + httpHost.getPort() + "/"+ getMethod.getURI() + ". Error: " + e.getMessage());
		} catch(IOException e) {
			throw new TSClientExecutionException("Failed to call " + httpHost.getHostName() + ":" + httpHost.getPort() + "/"+ getMethod.getURI() + ". Error: " + e.getMessage());
		} finally {
			if(ptestServerInputStream != null) {
				try {
					ptestServerInputStream.close();
					httpClient.getConnectionManager().shutdown();
					
				} catch(Exception e) {
					System.out.println("Failed to close ptest-server connection");
				} 
			}
		}
	}
	
	/**
	 * Parses the int value referenced by the given query
	 * @param rootNode
	 * @param query
	 * @param xpath
	 * @return
	 * @throws TSClientExecutionException
	 */
	protected int parseIntValue(Node rootNode, String query, XPath xpath) throws TSClientExecutionException {
		
		String tmp = null;
		try {
			tmp = (String) xpath.evaluate(query, rootNode, XPathConstants.STRING);
		} catch(XPathExpressionException e) {
			throw new TSClientExecutionException("Failed to parse out value for '"+query+"' from document received from " + httpHost.getHostName());
		}
		
		if(tmp != null && !tmp.isEmpty()) {
			try {
				return Integer.parseInt(tmp);
			} catch(NumberFormatException e) {
				throw new TSClientExecutionException("Failed to parse the string '"+tmp+"' into a valid numerical value received through query '"+query+"'. Returning host: " + httpHost.getHostName());
			}
		}
		
		throw new TSClientExecutionException("No valid value found for '"+query+"' from document received from " + httpHost.getHostName());		
	}
	
	/**
	 * Parses the long value referenced by the given query
	 * @param rootNode
	 * @param query
	 * @param xpath
	 * @return
	 * @throws TSClientExecutionException
	 */
	protected long parseLongValue(Node rootNode, String query, XPath xpath) throws TSClientExecutionException {
		
		String tmp = null;
		try {
			tmp = (String) xpath.evaluate(query, rootNode, XPathConstants.STRING);
		} catch(XPathExpressionException e) {
			throw new TSClientExecutionException("Failed to parse out value for '"+query+"' from document received from " + httpHost.getHostName());
		}
		
		if(tmp != null && !tmp.isEmpty()) {
			try {
				return Long.parseLong(tmp);
			} catch(NumberFormatException e) {
				throw new TSClientExecutionException("Failed to parse the string '"+tmp+"' into a valid numerical value received through query '"+query+"'. Returning host: " + httpHost.getHostName());
			}
		}
		
		throw new TSClientExecutionException("No valid value found for '"+query+"' from document received from " + httpHost.getHostName());		
	}
	
	/**
	 * Parses the double value referenced by the given query
	 * @param rootNode
	 * @param query
	 * @param xpath
	 * @return
	 * @throws TSClientExecutionException
	 */
	protected double parseDoubleValue(Node rootNode, String query, XPath xpath) throws TSClientExecutionException {
		
		String tmp = null;
		try {
			tmp = (String) xpath.evaluate(query, rootNode, XPathConstants.STRING);
		} catch(XPathExpressionException e) {
			throw new TSClientExecutionException("Failed to parse out value for '"+query+"' from document received from " + httpHost.getHostName());
		}
		
		if(tmp != null && !tmp.isEmpty()) {
			try {
				return Double.parseDouble(tmp);
			} catch(NumberFormatException e) {
				throw new TSClientExecutionException("Failed to parse the string '"+tmp+"' into a valid numerical value received through query '"+query+"'. Returning host: " + httpHost.getHostName());
			}
		}
		
		throw new TSClientExecutionException("No valid value found for '"+query+"' from document received from " + httpHost.getHostName());		
	}

	
	/**
	 * Parses the string value referenced by the given query
	 * @param rootNode
	 * @param query
	 * @param xpath
	 * @return
	 * @throws TSClientExecutionException
	 */
	protected String parseStringValue(Node rootNode, String query, XPath xpath) throws TSClientExecutionException {

		String result = null;
		try {
			result = (String) xpath.evaluate(query, rootNode, XPathConstants.STRING);
		} catch(XPathExpressionException e) {
			throw new TSClientExecutionException("Failed to parse out value for '"+query+"' from document received from " + httpHost.getHostName());
		}

		if(result == null || result.isEmpty())
			throw new TSClientExecutionException("Failed to parse out value for '"+query+"' from document received from " + httpHost.getHostName());
		
		return result;		
		
	}


}
