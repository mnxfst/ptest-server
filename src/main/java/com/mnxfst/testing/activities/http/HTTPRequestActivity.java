/*
 *  ptest-server  and client provides you with a performance test utility
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
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
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
		
		// receives the http response
		HttpResponse response = null;
		try {		
			
			if(this.method.equalsIgnoreCase("get")) {
				response = sendGETRequest(header);
			} else {
				// TODO
				response = sendPOSTRequest(null, header);
			}
		} catch(ClientProtocolException e) {
			throw new TSPlanActivityExecutionException("Failed to execute '"+this.method+"' request. Error: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new TSPlanActivityExecutionException("Failed to execute '"+this.method+"' request. Error: " + e.getMessage(), e);
		}
		
		try {
			String content = EntityUtils.toString(response.getEntity());
			ctx.addTransientVariable(contextExportVariableResponseContent, content);
		} catch(IOException e) {
			e.printStackTrace(); // TODO handle - we should log this instead of throwing an exception
		} finally {
			try {
				clientConnection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return ctx;
	}

	/**
	 * Sends a POST request to the configured url using the provided {@link HttpEntity entity} as additional payload
	 * @param entity
	 * @param header
	 * @return
	 * @throws IOException
	 */
	protected HttpResponse sendPOSTRequest(HttpEntity entity, Map<String, String> header) throws IOException {

		HttpPost httpPost = new HttpPost(this.destinationURI);
		httpPost.setParams(httpParameters);

		if(header != null && !header.isEmpty()) {
			for(String key : header.keySet())
				httpPost.addHeader(key, header.get(key));
		}
		
		httpPost.setEntity(entity);

		return httpClient.execute(httpPost);		
	}
	
	/**
	 * Sends a GET request to the configured url
	 * @param header
	 * @return
	 * @throws IOException
	 */
	protected HttpResponse sendGETRequest(Map<String, String> header) throws IOException {

		HttpGet httpGet = new HttpGet(this.destinationURI);
		httpGet.setParams(httpParameters);

		if(header != null && !header.isEmpty()) {
			for(String key : header.keySet())
				httpGet.addHeader(key, header.get(key));
		}

		// execute request on selected client connection
		return httpClient.execute(httpGet);
	}
	
}
