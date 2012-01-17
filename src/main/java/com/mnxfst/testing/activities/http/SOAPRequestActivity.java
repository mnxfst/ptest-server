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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Sends a SOAP request to a configured destination using a previously defined payload
 * @author ckreutzfeldt
 *
 */
public class SOAPRequestActivity extends AbstractHTTPRequestActivity {

	private static final Logger logger = Logger.getLogger(SOAPRequestActivity.class);	
	
	private String soapAction = null;
	
	// holds the payload which might contain dynamic variables denoted by ${varName} 
	// which will be solved through context lookups
	private String payloadTemplate = null;
	private String payloadEncoding = null;
	private Map<String, String> payloadVariables = new HashMap<String, String>();
	
	/**
	 * @see com.mnxfst.testing.activities.http.AbstractHTTPRequestActivity#activitySpecificPostInit(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	protected void activitySpecificPostInit(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

		/////////////////////////////////////////////////////////////////////////
		// soap specific
		this.soapAction = new StringBuffer("\"").append((String)cfgOpt.getOption("soapAction")).append("\"").toString();
		if(soapAction == null || soapAction.isEmpty())
			throw new TSPlanActivityExecutionException("Required soap action not provided for activity '"+getName()+"'");
		
		this.payloadTemplate = (String)cfgOpt.getOption("payloadTemplate");
		if(payloadTemplate == null || payloadTemplate.isEmpty())
			throw new TSPlanActivityExecutionException("Required payload template not provided for activity '"+getName()+"'");
		
		// TODO test
		
		this.payloadEncoding = (String)cfgOpt.getOption("payloadEncoding");		
		// no payload encoding provided? assume UTF-8 ---  TODO lookup encoding from xml
		if(this.payloadEncoding == null || this.payloadEncoding.isEmpty()) {
			this.payloadEncoding = "UTF-8";
		}		
		/////////////////////////////////////////////////////////////////////////


		/////////////////////////////////////////////////////////////////////////
		// extract variables from payload
		payloadVariables = getContextVariablesFromString(payloadTemplate);
//
//		if(logger.isDebugEnabled())
//			logger.debug("Successfully pre-computet soap request being forwarded to " + httpRequestURI);
//		
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(java.util.Map)
	 */
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {
		
//		HttpPost httpPostRequest = new HttpPost(this.httpRequestURI);
//		httpPostRequest.setHeader("SOAPAction", this.soapAction);
//
//		// replace payload variables with values fetched from context
//		String payload = new String(this.payloadTemplate);
//		for(String contextVariable : payloadVariables.keySet()) {
//			String payloadVariable = payloadVariables.get(contextVariable);
//			Serializable contextValue = input.get(contextVariable);
//			if(contextValue != null)
//				payload = payload.replaceAll(payloadVariable, contextValue.toString());
//		}
//
//		// convert payload into request entity and assign it 
//		try {
//			StringEntity entity = new StringEntity(payload, payloadEncoding);
//			entity.setContentType("text/xml");
//			httpPostRequest.setEntity(entity);
//		} catch(UnsupportedEncodingException e) {
//			throw new TSPlanActivityExecutionException("Failed to assign configured payload to post request. Error: " + e.getMessage(), e);
//		}
//
//		HttpResponse httpResponse = executeRequest(httpPostRequest);
//		input.put(getContextVariable(), new String(getResponseContent(httpResponse)));
		
		return ctx;
	}

}
