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

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Activity implementation for sending requests to a jms destination 
 * @author ckreutzfeldt
 *
 */
public class JMSDestinationRequestActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(JMSDestinationRequestActivity.class);
	
	private enum ConnectionType implements Serializable {
		QUEUE,
		TOPIC
	}
	
	/** connection type: queue or topic */
	private ConnectionType connectionType = ConnectionType.QUEUE;
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
	
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Missing required configuration options for activity '"+getName()+"'");

		String tmp = (String)cfgOpt.getOption("connectionType");
		if(tmp != null && !tmp.isEmpty()) {
			 
			if(tmp.equalsIgnoreCase("queue"))
				this.connectionType = ConnectionType.QUEUE;
			else if(tmp.equalsIgnoreCase("topic"))
				this.connectionType = ConnectionType.TOPIC;
			else
				throw new TSPlanActivityExecutionException("Found invalid connection type ('"+tmp+"') for activity '"+getName()+"'");
			
		} else {
			throw new TSPlanActivityExecutionException("Missing required connection type for activity '"+getName()+"'");
		}
		
		this.destinationName = (String)cfgOpt.getOption("destinationName");
		if(this.destinationName == null || this.destinationName.isEmpty())
			throw new TSPlanActivityExecutionException("Missing required destination name for activity '"+getName()+"'");
		
		try {
			// fetches the settings provided via jndi.properties from classpath
			this.initialJNDIContext = new InitialContext();
			this.jmsConnectionFactory = (ConnectionFactory)initialJNDIContext.lookup("ConnectionFactory");
			this.jmsConnection = this.jmsConnectionFactory.createConnection();
			this.jmsSession = this.jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			this.jmsDestination = (Destination)initialJNDIContext.lookup(this.destinationName);				
			this.jmsMessageProducer = this.jmsSession.createProducer(this.jmsDestination);
		} catch (NamingException e) {
			throw new TSPlanActivityExecutionException("Failed to set up initial JNDI context. Error: " + e.getMessage(), e);
		} catch (JMSException e) {
			throw new TSPlanActivityExecutionException("Failed to setup jms connection. Error: " + e.getMessage(), e);
		}

	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext)
	 */
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {

		try {
			TextMessage jmsMessage = this.jmsSession.createTextMessage("this is a sample text message");
			System.out.println("Sending..");
			this.jmsMessageProducer.send(jmsMessage);
		} catch (JMSException e) {
			throw new TSPlanActivityExecutionException("Failed to send jms message to topic '"+this.destinationName+"'. Error: " + e.getMessage(), e);
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
