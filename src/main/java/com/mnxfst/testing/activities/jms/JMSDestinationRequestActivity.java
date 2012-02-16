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

package com.mnxfst.testing.activities.jms;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.exception.TSVariableEvaluationFailedException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Activity implementation for sending requests to a jms destination 
 * @author ckreutzfeldt
 *
 */
public class JMSDestinationRequestActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(JMSDestinationRequestActivity.class);
	
	private static final String CFG_OPT_PAYLOAD_TEMPLATE = "jmsPayloadTemplate";
	private static final String CFG_OPT_DESTINATION_NAME = "destinationName";
	
	/** destination name, eg. myTopic or myQueue */
	private String destinationName = null;
	/** jnid context */
	private InitialContext initialJNDIContext = null;
	/** connection factory */
	private ConnectionFactory jmsConnectionFactory = null;
	/** maintains the jms connection */
	private Connection jmsConnection = null;
	/** holds the jms session */
	private Session jmsSession = null;
	/** jms destination used for delivering messages to */
	private Destination jmsDestination = null;
	/** message producer */
	private MessageProducer jmsMessageProducer = null;
	/** payload template */
	private String jmsMessageTemplate = null;
	/** holds the identified payload variables */
	private Map<String, String> jmsPayloadVariables = new HashMap<String, String>();
	
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Missing required configuration options for activity '"+getName()+"'");
		
		this.destinationName = (String)cfgOpt.getOption(CFG_OPT_DESTINATION_NAME);
		if(this.destinationName == null || this.destinationName.isEmpty())
			throw new TSPlanActivityExecutionException("Missing required destination name for activity '"+getName()+"'");
		
		this.jmsMessageTemplate = (String)cfgOpt.getOption(CFG_OPT_PAYLOAD_TEMPLATE);
		if(jmsMessageTemplate == null || jmsMessageTemplate.isEmpty())
			throw new TSPlanActivityExecutionException("Required payload template not provided for activity '"+getName()+"'");		
		
		try {
			// fetches the settings provided via jndi.properties from classpath
			this.initialJNDIContext = new InitialContext();
			this.jmsConnectionFactory = (ConnectionFactory)initialJNDIContext.lookup("ConnectionFactory");
			this.jmsConnection = this.jmsConnectionFactory.createConnection();
			this.jmsSession = this.jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			this.jmsDestination = (Destination)initialJNDIContext.lookup(this.destinationName);
			this.jmsMessageProducer = this.jmsSession.createProducer(this.jmsDestination);
		} catch (NamingException e) {
			logger.error("Error while initializing the naming context. Error: " + e.getMessage(), e);
			throw new TSPlanActivityExecutionException("Failed to set up initial JNDI context. Error: " + e.getMessage(), e);
		} catch (JMSException e) {
			logger.error("Error while sending a JMS message. Error: " + e.getMessage(), e);
			throw new TSPlanActivityExecutionException("Failed to setup jms connection. Error: " + e.getMessage(), e);
		}


	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext)
	 */
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {

		// replace payload variables with values fetched from context
		String payload = new String(this.jmsMessageTemplate);		
		for(String logPattern : jmsPayloadVariables.keySet()) {
			String replacementPattern = jmsPayloadVariables.get(logPattern);
			Object ctxValue = null;
			try {
				ctxValue = ctx.evaluate(logPattern);
			} catch(TSVariableEvaluationFailedException e) {
				throw new TSPlanActivityExecutionException("Failed to evaluate " + logPattern);
			}
			
			if(ctxValue != null)
				payload = payload.replaceAll(replacementPattern, ctxValue.toString());			
		}
		
		try {
			TextMessage jmsMessage = this.jmsSession.createTextMessage(payload.toString());
			this.jmsMessageProducer.send(jmsMessage);
		} catch (JMSException e) {
			logger.error("Failed to send jms message to queue/topic '"+this.destinationName+"'. Error: " + e.getMessage(), e);			
			throw new TSPlanActivityExecutionException("Failed to send jms message to queue/topic '"+this.destinationName+"'. Error: " + e.getMessage(), e);
		}
				
		return ctx;
	}

	/**
	 * @see com.mnxfst.testing.activities.AbstractTSPlanActivity#shutdown()
	 */
	public void shutdown() {
		try {
			this.jmsMessageProducer.close();
			this.jmsSession.close();
			this.jmsConnection.close();		
		} catch (JMSException e) {
			logger.error("Failed to shutdown JMS activity '"+getName()+"'. Error: " + e.getMessage(), e);
		}
	}

}
