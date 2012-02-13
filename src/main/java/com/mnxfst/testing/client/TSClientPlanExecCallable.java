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
import java.util.ArrayList;
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
import org.apache.http.NameValuePair;
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
import org.w3c.dom.NodeList;

import com.mnxfst.testing.exception.TSClientExecutionException;

/**
 * Executes the call to a ptest-server instance and returns a key/value pair containing the hostname and 
 * the result identifier of the test plan execution initiated through this callable 
 * @author mnxfst
 * @since 12.02.2012
 */
public class TSClientPlanExecCallable implements Callable<NameValuePair> {

	private static final String TEST_EXEC_RESPONSE_ROOT = "testExecutionResponse";
	private static final String TEST_EXEC_RESPONSE_CODE = "/testExecutionResponse/responseCode";
	private static final String TEST_EXEC_RESULT_IDENTIFIER = "/testExecutionResponse/resultIdentifier";
	private static final String TEST_EXEC_ERROR_CODES = "/testExecutionResponse/errorCodes/*";
	private static final String TEST_EXEC_SINGLE_ERROR_CODE = "/errorCode";

	private static final int RESPONSE_CODE_EXECUTION_STARTED = 1;
	private static final int RESPONSE_CODE_ERROR = 4;	

	private HttpGet getMethod = null;
	private HttpHost httpHost = null;
	private DefaultHttpClient httpClient = null;
	
	// TODO test and refactor from name value pair to something different and check the content copy method
	public TSClientPlanExecCallable(String hostname, int port, String uri) {
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
	 * @see java.util.concurrent.Callable#call()
	 */
	@SuppressWarnings("unused")
	public NameValuePair call() throws Exception {

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

			int responseCode = parseResponseCode(rootNode, xpath);
			switch(responseCode) {
				case RESPONSE_CODE_EXECUTION_STARTED: {
					String responseIdentifier = parseResultIdentifier(rootNode, xpath);
					return new BasicNameValuePair(httpHost.getHostName(), responseIdentifier);
				}
				case RESPONSE_CODE_ERROR: {
					List<Long> errorCodes = parseErrorCodes(rootNode, xpath);
					StringBuffer codes = new StringBuffer();
					for(Iterator<Long> iter = errorCodes.iterator(); iter.hasNext();) {
						codes.append(iter.next());
						if(iter.hasNext())
							codes.append(",");
					}
						
					throw new TSClientExecutionException("Failed to execute test plan on " + httpHost.getHostName() + ":" + httpHost.getPort() + ". Error codes: " + codes.toString());
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
	 * Parses the response code from the returned test plan execution result
	 * @param rootNode
	 * @return
	 * @throws TSClientExecutionException
	 */
	protected int parseResponseCode(Node rootNode, XPath xpath) throws TSClientExecutionException {
		
		String responseCode = null;
		try {
			responseCode = (String) xpath.evaluate(TEST_EXEC_RESPONSE_CODE, rootNode, XPathConstants.STRING);
		} catch(XPathExpressionException e) {
			throw new TSClientExecutionException("Failed to parse out response code from document received from " + httpHost.getHostName());
		}
		
		if(responseCode != null && !responseCode.isEmpty()) {
			try {
				return Integer.parseInt(responseCode);
			} catch(NumberFormatException e) {
				throw new TSClientExecutionException("Failed to parse response code '"+responseCode+"' into a valid numerical value. Returning host: " + httpHost.getHostName());
			}
		}
		
		throw new TSClientExecutionException("No valid response code received from " + httpHost.getHostName());		
	}
	
	/**
	 * Parses the result identifier from the returned result
	 * @param rootNode
	 * @return
	 * @throws TSClientExecutionException
	 */
	protected String parseResultIdentifier(Node rootNode, XPath xpath) throws TSClientExecutionException {
		
		String resultIdentifier = null;
		try {
			resultIdentifier = (String) xpath.evaluate(TEST_EXEC_RESULT_IDENTIFIER, rootNode, XPathConstants.STRING);
		} catch(XPathExpressionException e) {
			throw new TSClientExecutionException("Failed to parse out result identifier from document received from " + httpHost.getHostName());
		}

		if(resultIdentifier == null || resultIdentifier.isEmpty())
			throw new TSClientExecutionException("Failed to parse out result identifier from document received from " + httpHost.getHostName());
		
		return resultIdentifier;		
	}

	/**
	 * Returns the error codes
	 * @param rootNode
	 * @param xpath
	 * @return
	 * @throws TSClientExecutionException
	 */
	protected List<Long> parseErrorCodes(Node rootNode, XPath xpath) throws TSClientExecutionException {

		NodeList errorCodeNodes = null;
		try {
			errorCodeNodes = (NodeList)xpath.evaluate(TEST_EXEC_ERROR_CODES, rootNode, XPathConstants.NODESET); 
		} catch(XPathExpressionException e) {
			throw new TSClientExecutionException("Failed to parse out error codes from document received from " + httpHost.getHostName());
		}
		
		List<Long> result = new ArrayList<Long>();
		if(errorCodeNodes != null && errorCodeNodes.getLength() > 0) {
			for(int i = 0; i < errorCodeNodes.getLength(); i++) {
				if(errorCodeNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					try {
						// TODO refactor to xpath
						String errorCodeStr = errorCodeNodes.item(i).getTextContent();//;(String)xpath.evaluate(TEST_EXEC_SINGLE_ERROR_CODE, errorCodeNodes.item(i), XPathConstants.STRING);						
						result.add(Long.parseLong(errorCodeStr.trim()));
					} catch(NumberFormatException e) {
						throw new TSClientExecutionException("Failed to parse error code from document received from " + httpHost.getHostName() + ". Error: " + e.getMessage());
//					} catch(XPathExpressionException e) {
//						throw new TSClientExecutionException("Failed to parse error code from document received from " + httpHost.getHostName() + ". Error: " + e.getMessage());
					}
				}
			}
		}
		return result;		
	}
}
