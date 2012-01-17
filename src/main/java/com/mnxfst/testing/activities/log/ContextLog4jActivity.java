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

package com.mnxfst.testing.activities.log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.TSPlanExecutionContext;

/**
 * Configurable logging activity which uses the log4j subsystem as destination. 
 * The log output could be configured such that context contents are being printed out
 * using named variables.
 * @author ckreutzfeldt
 * @since 09.01.2012
 */
public class ContextLog4jActivity extends AbstractTSPlanActivity {
	
	private static Logger logger = Logger.getLogger(ContextLog4jActivity.class);

	/** holds the appender type named in the configuration options. depending of the type, additional options must be present. for more info, see the appender building methods */  
	private ContextLog4jAppenderType appenderType = ContextLog4jAppenderType.UNKNOWN;
	
	/** holds the log level used for writing out messages and used for catching messages through the configured appender */
	private ContextLog4jLevel logLevel = ContextLog4jLevel.UNKNOWN;
	
	/** holds the log message pattern to be used for formatting the output message */
	private String conversionPattern = null;
	
	/** holds the configured log line pattern containing text and variables being formatted as dollar prefix strings surrounded by curly brackets: ${testVariable} */
	private String logMessage = null;

	/** contains the variable strings formatted for being used as patterns in String.replaceAll. The contents are being extracted during the init phase and stored as contextVariable/replacePattern */
	private Map<String, String> logPatternVariables = null;
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException {

		if(cfgOpt == null)
			throw new TSPlanActivityExecutionException("Missing required configuration options");
				
		String activityName = getName();

		this.conversionPattern = (String)cfgOpt.getOption("conversionPattern");
		if(conversionPattern == null || conversionPattern.isEmpty())
			throw new TSPlanActivityExecutionException("Log message conversion pattern missing for activity '"+activityName+"'");
		
		this.logMessage = (String)cfgOpt.getOption("logMessage");
		if(logMessage == null || logMessage.isEmpty())
			throw new TSPlanActivityExecutionException("Log message missing for activity '"+activityName+"'");
	
		// fetch associated log appender - if it exists, skip the remaining initialization
		Appender activityAppender = logger.getAppender(activityName);
		if(activityAppender == null) {
			
			if(logger.isDebugEnabled())
				logger.debug("No context log appender found for activity: " + activityName + ". Creating new one according to config options");
			
			// fetch and assign appender type
			appenderType = ContextLog4jAppenderType.fromString((String)cfgOpt.getOption("logAppender"));
			if(appenderType == null || appenderType == ContextLog4jAppenderType.UNKNOWN)
				throw new TSPlanActivityExecutionException("No appender type provided or named one is unkown for activity '"+activityName+"'");
			
			switch(appenderType) {
				case CONSOLE: {
					activityAppender = createConsoleAppender(cfgOpt);
					break;
				}
				default: {
					throw new TSPlanActivityExecutionException("Unsupported appender type: " + appenderType);
				}
			}
			logger.addAppender(activityAppender);
			
			// fetch an assign log level
			logLevel = ContextLog4jLevel.fromString((String)cfgOpt.getOption("logLevel"));
			if(logLevel == null || logLevel == ContextLog4jLevel.UNKNOWN)
				throw new TSPlanActivityExecutionException("No log level provided or named one is unknown for activity '"+activityName+"'");
			
			switch(logLevel) {
				case DEBUG: {
					logger.setLevel(Level.DEBUG);
					break;
				}
				case INFO: {
					logger.setLevel(Level.INFO);
					break;
				}
				case WARN: {
					logger.setLevel(Level.WARN);
					break;
				}
				case ERROR: {
					logger.setLevel(Level.ERROR);
					break;
				}
				default: {
					throw new TSPlanActivityExecutionException("Unsupported log level: " + logLevel);
				}
			}
			
			logPatternVariables = getContextVariablesFromString(logMessage);
			
			if(logger.isDebugEnabled())
				logger.debug("ContextLog4jActivity initialized: [activity="+activityName+", appender="+appenderType+", logLevel="+logLevel+"]");
			
		} else {
			if(logger.isDebugEnabled())
				logger.debug("Existing context log appender found for activity: " + activityName);
		}
	
		if(logPatternVariables == null)
			logPatternVariables = new HashMap<String, String>();
		
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(com.mnxfst.testing.plan.ctx.TSPlanExecutionContext)
	 */
	public TSPlanExecutionContext execute(TSPlanExecutionContext ctx) throws TSPlanActivityExecutionException {

		String resultMessage = new String(logMessage);
		
		if(ctx != null) {
			for(String ctxVar : logPatternVariables.keySet()) {
				String replacementPattern = logPatternVariables.get(ctxVar);
				Serializable ctxValue = ctx.getTransientVariable(ctxVar);
	
				if(ctxValue != null)
					resultMessage = resultMessage.replaceAll(replacementPattern, ctxValue.toString());			
			}
		
			switch(logLevel) {
				case DEBUG: {
					logger.debug(resultMessage);
					break;
				}
				case INFO: {
					logger.info(resultMessage);
					break;
				}
				case WARN: {
					logger.warn(resultMessage);
					break;
				}
				case ERROR: {
					logger.error(resultMessage);
					break;
				}
			}
						
			ctx.addTransientVariable(getContextVariable(), resultMessage);

		} else {
			throw new TSPlanActivityExecutionException("No context provided to activity '"+getName()+"'");
		}
		
		return ctx;
	}

	/**
	 * Returns a {@link ConsoleAppender} configured according to provided settings
	 * @param cfgOpt
	 * @return
	 */
	private Appender createConsoleAppender(TSPlanConfigOption cfgOpt) {		
		return new ConsoleAppender(new PatternLayout(conversionPattern));
	}
	
}
