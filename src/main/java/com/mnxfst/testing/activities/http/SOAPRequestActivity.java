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
		this.payloadTemplate = (String)cfgOpt.getOption("payloadTemplate");
		this.payloadEncoding = (String)cfgOpt.getOption("payloadEncoding");
		
		// no payload encoding provided? assume UTF-8 ---  TODO lookup encoding from xml
		if(this.payloadEncoding == null || this.payloadEncoding.isEmpty()) {
			this.payloadEncoding = "UTF-8";
		}		
		/////////////////////////////////////////////////////////////////////////


		/////////////////////////////////////////////////////////////////////////
		// extract variables from payload
		// TODO verify
		int index = 0;
		while((index < payloadTemplate.length()) && (index != -1)) {
			index = payloadTemplate.indexOf("${", index);
			if(index != -1 && index < payloadTemplate.length()) {
				String payloadVariable = payloadTemplate.substring(index, payloadTemplate.indexOf("}", index+1) + 1);
				if(payloadVariable != null && !payloadVariable.isEmpty()) {
					String contextVariableName = payloadVariable.substring(2, payloadVariable.length() - 1);
					payloadVariable = payloadVariable.replace("$", "\\$");
					payloadVariable = payloadVariable.replace("{", "\\{");
					payloadVariable = payloadVariable.replace("}", "\\}");
					payloadVariables.put(contextVariableName, payloadVariable);
					logger.info("var: " + contextVariableName + " / " + payloadVariable);
				}
				index = index + 1;
			}
			
		}
		
		if(logger.isDebugEnabled())
			logger.debug("Successfully pre-computet soap request being forwarded to " + httpRequestURI);
		
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(java.util.Map)
	 */
	public Map<String, Serializable> execute(Map<String, Serializable> input) throws TSPlanActivityExecutionException {
		
		HttpPost httpPostRequest = new HttpPost(this.httpRequestURI);
		httpPostRequest.setHeader("SOAPAction", this.soapAction);

		// replace payload variables with values fetched from context
		String payload = new String(this.payloadTemplate);
		for(String contextVariable : payloadVariables.keySet()) {
			String payloadVariable = payloadVariables.get(contextVariable);
			String contextValue = (String)input.get(contextVariable);
			if(contextValue != null)
				payload = payload.replaceAll(payloadVariable, contextValue);
		}

		// convert payload into request entity and assign it 
		try {
			StringEntity entity = new StringEntity(payload, payloadEncoding);
			entity.setContentType("text/xml");
			httpPostRequest.setEntity(entity);
		} catch(UnsupportedEncodingException e) {
			throw new TSPlanActivityExecutionException("Failed to assign configured payload to post request. Error: " + e.getMessage(), e);
		}

		HttpResponse httpResponse = executeRequest(httpPostRequest);

		String result = new String(getResponseContent(httpResponse));		
		return input;
	}

}
