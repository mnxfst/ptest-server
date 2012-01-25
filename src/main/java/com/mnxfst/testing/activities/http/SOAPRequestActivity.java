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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext.ContextVariableType;

/**
 * Sends a SOAP request to a configured destination using a previously defined payload
 * @author ckreutzfeldt
 *
 */
public class SOAPRequestActivity extends HTTPRequestActivity {

	private static final Logger logger = Logger.getLogger(SOAPRequestActivity.class);	
	
	private static final String CFG_OPT_SOAP_ACTION = "soapAction";
	private static final String CFG_OPT_PAYLOAD_TEMPLATE = "soapPayloadTemplate";
	private static final String CFG_OPT_PAYLOAD_ENCODING = "soapPayloadEncoding";
	
	private static final String HTTP_REQUEST_HEADER_SOAP_ACTION = "SOAPAction";

	/** holds the name of the soap action to call */
	private String soapAction = null;
	/** holds the payload which might contain dynamic variables denoted by ${varName} which will be solved through context lookups */
	private String payloadTemplate = null;
	/** holds the payload encoding */
	private String payloadEncoding = null;
	/** holds the identified payload variables */
	private Map<String, String> payloadVariables = new HashMap<String, String>();
	
	/**
	 * @see com.mnxfst.testing.activities.http.HTTPRequestActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfg) throws TSPlanActivityExecutionException {
		super.initialize(cfg);

		/////////////////////////////////////////////////////////////////////////
		// soap specific
		
		String tmpSoapAction = (String)cfg.getOption(CFG_OPT_SOAP_ACTION);
		if(tmpSoapAction != null && !tmpSoapAction.isEmpty())
			this.soapAction = new StringBuffer("\"").append(tmpSoapAction.trim()).append("\"").toString();
		
		this.payloadTemplate = (String)cfg.getOption(CFG_OPT_PAYLOAD_TEMPLATE);
		if(payloadTemplate == null || payloadTemplate.isEmpty())
			throw new TSPlanActivityExecutionException("Required payload template not provided for activity '"+getName()+"'");
		
		this.payloadEncoding = (String)cfg.getOption(CFG_OPT_PAYLOAD_ENCODING);		
		// no payload encoding provided? assume UTF-8 ---  TODO lookup encoding from xml
		if(this.payloadEncoding == null || this.payloadEncoding.isEmpty()) {
			this.payloadEncoding = "UTF-8";
		}		
		
		this.payloadVariables = getContextVariablesFromString(payloadTemplate);
		
		header.put(HTTP_REQUEST_HEADER_SOAP_ACTION, this.soapAction);

		/////////////////////////////////////////////////////////////////////////

		
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(java.util.Map)
	 */
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {

		// replace payload variables with values fetched from context
		String payload = new String(this.payloadTemplate);
		for(String contextVariable : payloadVariables.keySet()) {
			String payloadVariable = payloadVariables.get(contextVariable);
			Serializable contextValue = ctx.findContextVariable(contextVariable, ContextVariableType.BOTH);
						
			if(contextValue != null)
				payload = payload.replaceAll(payloadVariable, contextValue.toString());
			
		}

		// convert payload into request entity and assign it
		StringEntity  entity = null;
		try {
			entity = new StringEntity(payload, payloadEncoding);
			entity.setContentType("text/xml");			
		} catch(UnsupportedEncodingException e) {
			throw new TSPlanActivityExecutionException("Failed to assign configured payload to post request. Error: " + e.getMessage(), e);
		}
		try {
			HttpResponse response = sendPOSTRequest(entity, header);
			ctx.addTransientVariable(contextExportVariableResponseContent, EntityUtils.toString(response.getEntity()));
		} catch (IOException e) {
			// TODO log errors
		}
		
		return ctx;
	}

}
