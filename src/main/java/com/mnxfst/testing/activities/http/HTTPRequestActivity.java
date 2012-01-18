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

package com.mnxfst.testing.activities.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.util.EntityUtils;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

public class HTTPRequestActivity extends AbstractHTTPRequestActivity {

	private static final String CTX_EXPORT_VAR_RESPONSE_CONTENT = "responseContent";
	
	protected String contextExportVariableResponseContent = null;
	
	/**
	 * @see com.mnxfst.testing.activities.http.AbstractHTTPRequestActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfg) throws TSPlanActivityExecutionException {
		super.initialize(cfg);
		
		this.contextExportVariableResponseContent = getContextExportVariables().get(CTX_EXPORT_VAR_RESPONSE_CONTENT);
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(com.mnxfst.testing.plan.ctx.TSPlanExecutionContext)
	 */
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {
		
		// (re-)establish client connection
		if(!clientConnection.isOpen()) {
			Socket socket;
			try {
				socket = new Socket(httpHost.getHostName(), httpHost.getPort());
				clientConnection.bind(socket, httpParameters);
			} catch (UnknownHostException e) {
				// TODO handle - we should log this instead of throwing an exception
			} catch (IOException e) {
				// TODO handle - we should log this instead of throwing an exception
			}
		}
	
		try {
			HttpResponse response = null;
			if(method.equalsIgnoreCase("post")) {
				response = sendPOSTRequest(null, header);
			} else {
				response = sendGETRequest(header);
			}
			
			String content = EntityUtils.toString(response.getEntity());
			ctx.addTransientVariable(contextExportVariableResponseContent, content);

		} catch(HttpException e) {
			// TODO handle - we should log this instead of throwing an exception 
		} catch(IOException e) {
			// TODO handle - we should log this instead of throwing an exception
		}
		
		return ctx;
	}

	/**
	 * Sends a POST request to the configured url using the provided {@link HttpEntity entity} as additional payload
	 * @param entity
	 * @param header
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	protected HttpResponse sendPOSTRequest(HttpEntity entity, Map<String, String> header) throws IOException, HttpException {

		BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", path);
		request.setEntity(entity);		
		request.setParams(httpParameters);

		if(header != null && !header.isEmpty()) {
			for(String key : header.keySet())
				request.addHeader(key, header.get(key));
		}
		
		// if the request needs to be pre-processed, execute now
		if(hasRequestResponseProcessors)
			httpRequestExecutor.preProcess(request, httpRequestResponseProcessor, httpRequestContext);

		// execute request on selected client connection
		HttpResponse response = httpRequestExecutor.execute(request, clientConnection, httpRequestContext);
		response.setParams(httpParameters);
			
		// if the response needs to be post-processed, execute now
		if(hasRequestResponseProcessors)
			httpRequestExecutor.postProcess(response, httpRequestResponseProcessor, httpRequestContext);
		
		return response;		
	}
	
	/**
	 * Sends a GET request to the configured url
	 * @param header
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	protected HttpResponse sendGETRequest(Map<String, String> header) throws IOException, HttpException {
	
		BasicHttpRequest request = new BasicHttpRequest("GET", path);
		request.setParams(httpParameters);

		if(header != null && !header.isEmpty()) {
			for(String key : header.keySet())
				request.addHeader(key, header.get(key));
		}

		// if the request needs to be pre-processed, execute now
		if(hasRequestResponseProcessors)
			httpRequestExecutor.preProcess(request, httpRequestResponseProcessor, httpRequestContext);

		// execute request on selected client connection
		HttpResponse response = httpRequestExecutor.execute(request, clientConnection, httpRequestContext);
		response.setParams(httpParameters);
			
		// if the response needs to be post-processed, execute now
		if(hasRequestResponseProcessors)
			httpRequestExecutor.postProcess(response, httpRequestResponseProcessor, httpRequestContext);

		return response;
	}
	
}
