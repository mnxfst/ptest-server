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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
	
	private static final String CFG_OPT_JNDI_CONNECTION_FACTORY_CLASS = "connectionFactoryClass";
	private static final String CFG_OPT_JNDI_CONNECTION_FACTORY_LOOKUP_NAME = "connectionFactoryLookupName";
	private static final String CFG_OPT_JNDI_PROVIDER_URL_LOOKUP_NAME = "providerUrl";
	private static final String CFG_OPT_JNDI_SECURITY_PRINCIPAL_LOOKUP_NAME = "principal";
	private static final String CFG_OPT_JNDI_SECURITY_CREDENTIALS_LOOKUP_NAME = "credentials";
	private static final String CFG_OPT_CLIENT_ID = "clientId";
	private static final String CFG_OPT_JNDI_VENDOR_SPECIFIC_PREFIX = "vendor.config.";
	
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
		
		this.jmsPayloadVariables = getContextVariablesFromString(jmsMessageTemplate);
		
		String connectionFactoryClass = (String)cfgOpt.getOption(CFG_OPT_JNDI_CONNECTION_FACTORY_CLASS);
		if(connectionFactoryClass == null || connectionFactoryClass.isEmpty())
			throw new TSPlanActivityExecutionException("Missing required connection factory class for activity '"+getName()+"'");
		
		String connectionFactoryLookupName = (String)cfgOpt.getOption(CFG_OPT_JNDI_CONNECTION_FACTORY_LOOKUP_NAME);
		if(connectionFactoryLookupName == null || connectionFactoryLookupName.isEmpty())
			throw new TSPlanActivityExecutionException("Missing required connection factory lookup name for activity '"+getName()+"'");
		
		String providerUrl = (String)cfgOpt.getOption(CFG_OPT_JNDI_PROVIDER_URL_LOOKUP_NAME);
		if(providerUrl == null || providerUrl.isEmpty())
			throw new TSPlanActivityExecutionException("Missing required provider url for activity '"+getName()+"'");
		
		String clientId = (String)cfgOpt.getOption(CFG_OPT_CLIENT_ID);		
		String securityPrincipal = (String)cfgOpt.getOption(CFG_OPT_JNDI_SECURITY_PRINCIPAL_LOOKUP_NAME);
		String securityCredentials = (String)cfgOpt.getOption(CFG_OPT_JNDI_SECURITY_CREDENTIALS_LOOKUP_NAME);
		
		Hashtable<String, String> jndiEnvironment = new Hashtable<String, String>();
		jndiEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, connectionFactoryClass);
		jndiEnvironment.put(Context.PROVIDER_URL, providerUrl);
		
		if(securityPrincipal != null && !securityPrincipal.isEmpty())
			jndiEnvironment.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
		if(securityCredentials != null && !securityCredentials.isEmpty())
			jndiEnvironment.put(Context.SECURITY_CREDENTIALS, securityCredentials);

		StringBuffer additionalJndiProps = new StringBuffer();
		// add additional vendor specific configuration options
		for(String cfgKey : cfgOpt.getOptions().keySet()) {
			if(cfgKey.startsWith(CFG_OPT_JNDI_VENDOR_SPECIFIC_PREFIX)) {
				String value = (String)cfgOpt.getOption(cfgKey);
				jndiEnvironment.put(cfgKey.substring(CFG_OPT_JNDI_VENDOR_SPECIFIC_PREFIX.length()), value);
				additionalJndiProps.append(", ").append(cfgKey.substring(CFG_OPT_JNDI_VENDOR_SPECIFIC_PREFIX.length())).append("=").append(value);
			}
		}
		
		logger.info("jms-activity[initialCtxFactory="+connectionFactoryClass+", ctxFactoryLookupName="+connectionFactoryLookupName+", brokerUrl="+providerUrl+", principal="+securityPrincipal+", credentials="+securityCredentials+", destination="+destinationName + additionalJndiProps.toString()+", clientId="+clientId+"]");
		
		try {						
			// fetches the settings provided via jndi.properties from classpath
			this.initialJNDIContext = new InitialContext(jndiEnvironment);
			this.jmsConnectionFactory = (ConnectionFactory)initialJNDIContext.lookup(connectionFactoryLookupName);
			this.jmsConnection = this.jmsConnectionFactory.createConnection();
			if(clientId != null && !clientId.isEmpty()) {
				try {
					this.jmsConnection.setClientID(clientId + "-" + InetAddress.getLocalHost().getHostName());
				} catch(UnknownHostException e) {
					logger.error("Failed to read local host name. Client identifier will not be set for JMS connection. Error: " + e.getMessage());
				}
			}
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
		String payload = null;
		try {
			payload = new String(this.jmsMessageTemplate.getBytes(), "UTF-8");
		} catch(UnsupportedEncodingException e) {
			logger.error("Failed to convert jms message template into UTF-8 string. Error: " + e.getMessage());
			throw new TSPlanActivityExecutionException("Failed to convert jms message template into UTF-8 string. Error: " + e.getMessage());
		}
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
			TextMessage jmsMessage = this.jmsSession.createTextMessage(payload.trim());
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
