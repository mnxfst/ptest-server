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

package com.mnxfst.testing.plan.exec.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

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

import com.mnxfst.testing.exception.TSClientExecutionException;

/**
 * Executes the call to a ptest-server instance and returns a key/value pair containing the hostname and 
 * the result identifier of the test plan execution initiated through this callable 
 * @author mnxfst
 * @since 12.02.2012
 */
public class TSClientPlanExecCallable implements Callable<NameValuePair> {

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
	public NameValuePair call() throws Exception {

		try {
			HttpResponse response = httpClient.execute(httpHost, getMethod);
			InputStream responseStream = response.getEntity().getContent();

			StringBuffer responseContent = new StringBuffer();
			int c = 0;
			while((c = responseStream.read()) != -1)
				responseContent.append((char)c);
							
			return new BasicNameValuePair(httpHost.getHostName(), responseContent.toString());
		} catch(ClientProtocolException e) {
			throw new TSClientExecutionException("Failed to call " + httpHost.getHostName() + ":" + httpHost.getPort() + "/"+ getMethod.getURI() + ". Error: " + e.getMessage());
		} catch(IOException e) {
			throw new TSClientExecutionException("Failed to call " + httpHost.getHostName() + ":" + httpHost.getPort() + "/"+ getMethod.getURI() + ". Error: " + e.getMessage());
		}

	}

	
	
}
