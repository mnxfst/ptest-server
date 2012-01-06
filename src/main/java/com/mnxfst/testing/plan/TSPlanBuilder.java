package com.mnxfst.testing.plan;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mnxfst.testing.activities.TSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.exception.TSPlanConfigurationFormatException;
import com.mnxfst.testing.exception.TSPlanInstantiationException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.config.TSPlanConfigOptionsBuilder;

/**
 * Parses a provided input source for a {@link TSPlan test plan configuration}
 * and returns a {@link TSPlan} instance
 * @author mnxfst
 * @since 21.21.2011
 */
public class TSPlanBuilder {

	private static final Logger logger = Logger.getLogger(TSPlanBuilder.class);
	
	// XML document nodes
	public static final String TEST_PLAN_ROOT_NODE = "ptestplan";
	
	public static final String TEST_PLAN_GLOBAL_CONFIGURATION_NODE = "globalConfiguration";	
	public static final String TEST_PLAN_CONFIGURATION_NAME_ATTRIBUTE = "name";
	public static final String TEST_PLAN_ACTIVITIES_NODE = "activities";
	public static final String TEST_PLAN_ACTIVITY_CONFIGURATION_NODE = "configuration";
	
	// xpath expressions required for parsing test plan attributes
	private static final String XPATH_EXPRESSION_PLAN_INIT_ACTIVITY = "/ptestplan/initActivity";
	private static final String XPATH_EXPRESSION_PLAN_NAME = "/ptestplan/name";
	private static final String XPATH_EXPRESSION_PLAN_DESCRIPTION = "/ptestplan/description";
	private static final String XPATH_EXPRESSION_PLAN_CREATION_DATE = "/ptestplan/creationDate";
	private static final String XPATH_EXPRESSION_PLAN_CREATED_BY = "/ptestplan/createdBy";
	
	
	private static final String XPATH_EXPRESSION_ALL_GLOBAL_CONFIG_OPTIONS = "/ptestplan/globalConfiguration/*";	
	private static final String XPATH_EXPRESSION_ALL_ACTIVITIES = "/ptestplan/activities/*";
	
	
	
	// xpath expressions required for parsing activities
	private static final String XPATH_EXPRESSION_ACTIVITY_ID_ATTRIBUTE = "@id";
	private static final String XPATH_EXPRESSION_ACTIVITY_NAME_ATTRIBUTE = "@name";
	private static final String XPATH_EXPRESSION_ACTIVITY_USE_GLOBAL_CONFIG_ATTRIBUTE = "@useGlobalConfig";
	private static final String XPATH_EXPRESSION_ACTIVITY_DESCRIPTION_NODE = "description";
	private static final String XPATH_EXPRESSION_ACTIVITY_CLASS_NODE = "class";
	private static final String XPATH_EXPRESSION_ACTIVITY_CONTEXT_VARIABLE_NODE = "contextVariable";
	private static final String XPATH_EXPRESSION_ACTIVITY_CONFIGURATION_NODES = "configuration";
	private static final String XPATH_EXPRESSION_ACTIVITY_NEXT_ACTIVITY = "nextActivity";

	/** required date format */
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/** test plan configuration option parser */
	private TSPlanConfigOptionsBuilder configOptionsParser = new TSPlanConfigOptionsBuilder();
	
	/**
	 * The singleton holder is loaded on the first execution of TSPlanBuilder.getInstance()
	 * or the first access to TSPlanSingletonHolder.instance, not before
	 * @author mnxfst
	 *
	 */
	private static class TSPlanSingletonHolder {
		public static final TSPlanBuilder instance = new TSPlanBuilder();
	}

	/**
	 * Ensure no direct instantiation
	 */
	private TSPlanBuilder() {		
	}
	
	/**
	 * Return the singleton instance of this class
	 * @return
	 */
	public static TSPlanBuilder getInstance() {
		return TSPlanSingletonHolder.instance;
	}
	
	/**
	 * Parses the provided xml document for {@link TSPlan} configuration information and instantiates
	 * a test plan according to the provided information 
	 * @param testPlanConfiguration
	 * @return
	 * @throws TSPlanConfigurationFormatException
	 * @throws TSPlanInstantiationException 
	 * @throws TSPlanActivityExecutionException 
	 */
	public TSPlan buildPlan(Document testPlanConfiguration) throws TSPlanConfigurationFormatException, TSPlanInstantiationException, TSPlanActivityExecutionException {
		
		// check for any document provided
		if(testPlanConfiguration == null)
			throw new TSPlanConfigurationFormatException("No test plan configuration provided");
		
		// find root node
		Node rootNode = testPlanConfiguration.getFirstChild();
		if(rootNode == null)
			throw new TSPlanConfigurationFormatException("No root node contained in test plan configuration");
		
		// validate root node name
		if(rootNode.getNodeName() == null || !rootNode.getNodeName().equalsIgnoreCase(TEST_PLAN_ROOT_NODE)) 
			throw new TSPlanConfigurationFormatException("Root node name either not provided or invalid");

		// look for child nodes
		NodeList childNodes = rootNode.getChildNodes();
		if(childNodes == null || childNodes.getLength() < 1)
			throw new TSPlanConfigurationFormatException("No child nodes found for root node");

		// required temporary variables for storing parsed out information which are known to be existing beforehand (compare to activities!)
		Map<String, TSPlanConfigOption> configurationOptions = parseGlobalConfigurationOptions(testPlanConfiguration);
		Map<String, TSPlanActivity> activities = parseActivities(testPlanConfiguration, configurationOptions);

		String name = null;
		String description = null;
		String createdBy = null;
		Date creationDate = null;
		String initActivity = null;

		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			name = (String) xpath.evaluate(XPATH_EXPRESSION_PLAN_NAME, testPlanConfiguration, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new TSPlanConfigurationFormatException("Failed to parse provided test plan for its name using a xpath expression. Error: " + e.getMessage(), e);
		}
		try {
			description = (String) xpath.evaluate(XPATH_EXPRESSION_PLAN_DESCRIPTION, testPlanConfiguration, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new TSPlanConfigurationFormatException("Failed to parse provided test plan for its description using a xpath expression. Error: " + e.getMessage(), e);
		}
		try {
			createdBy = (String) xpath.evaluate(XPATH_EXPRESSION_PLAN_CREATED_BY, testPlanConfiguration, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new TSPlanConfigurationFormatException("Failed to parse provided test plan for its 'created-by' attribute using a xpath expression. Error: " + e.getMessage(), e);
		}
		try {
			String creationDateStr = (String) xpath.evaluate(XPATH_EXPRESSION_PLAN_CREATION_DATE, testPlanConfiguration, XPathConstants.STRING);
			if(creationDateStr != null && !creationDateStr.isEmpty()) {
				try {
					creationDate = simpleDateFormat.parse(creationDateStr);
				} catch(ParseException e) {
					throw new TSPlanConfigurationFormatException("Failed to parse provided test plan due to an invalid creation date attribute value: " + creationDateStr + ". Expected a date following yyyy-mm-dd");
				}
			}
		} catch (XPathExpressionException e) {
			throw new TSPlanConfigurationFormatException("Failed to parse provided test plan for its creation date using a xpath expression. Error: " + e.getMessage(), e);
		}
		try {
			initActivity = (String) xpath.evaluate(XPATH_EXPRESSION_PLAN_INIT_ACTIVITY, testPlanConfiguration, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new TSPlanConfigurationFormatException("Failed to parse provided test plan for its init activityusing a xpath expression. Error: " + e.getMessage(), e);
		}
		
		// ensure that there is a name, a description, a creation date and a hint on who created the test plan
		if(name == null || name.isEmpty())
			throw new TSPlanConfigurationFormatException("Name element missing or empty");
		if(description == null || description.isEmpty())
			throw new TSPlanConfigurationFormatException("Description element missing or empty");
		if(createdBy == null || createdBy.isEmpty())
			throw new TSPlanConfigurationFormatException("Created-by element missing or empty");
		if(creationDate == null)
			throw new TSPlanConfigurationFormatException("Creation date element missing");
		if(initActivity == null || initActivity.isEmpty())
			throw new TSPlanConfigurationFormatException("Init activity element missing");
		if(activities.get(initActivity) == null)
			throw new TSPlanConfigurationFormatException("No activity found for name of init activity: " + initActivity);
		
		TSPlan testPlan = new TSPlan();
		testPlan.setCreatedBy(createdBy);
		testPlan.setCreationDate(creationDate);
		testPlan.setDescription(description);
		testPlan.setName(name);
		testPlan.setInitActivityName(initActivity);
		testPlan.getConfigurationOptions().putAll(configurationOptions);
		
		testPlan.getActivities().putAll(activities);
		
		if(logger.isDebugEnabled())
			logger.debug("Successfully created test plan instance: " + testPlan);
		
		return testPlan;		
	}
	
	/**
	 * Parses contents for a node which only holds a string value as child element. If there
	 * is no content, the method returns null
	 * @param simpleNode
	 * @return
	 */
	protected String parseSimpleTextNode(Node simpleNode) {
		
		// fetch the value nodes and check for elements
		NodeList valueNodes = simpleNode.getChildNodes();
		if(valueNodes != null && valueNodes.getLength() > 0) {
			// fetch the first node by default 
			Node vn = valueNodes.item(0);
			if(vn != null && vn.getNodeType() == Node.TEXT_NODE) {
				return vn.getNodeValue();
			}
		}
		
		return null;

	}
	
	/**
	 * Parses out the global configuration options. If there are not settings, the result will be an empty (but non-null) map
	 * @param testPlanDocument
	 * @return
	 * @throws TSPlanConfigurationFormatException
	 */
	protected Map<String, TSPlanConfigOption> parseGlobalConfigurationOptions(Document testPlanDocument) throws TSPlanConfigurationFormatException {
	
		Map<String, TSPlanConfigOption> result = new HashMap<String, TSPlanConfigOption>();
		
		// fetch nodes using an xpath expression
		NodeList configurationOptionsNodes = null;
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			configurationOptionsNodes = (NodeList)xpath.evaluate(XPATH_EXPRESSION_ALL_GLOBAL_CONFIG_OPTIONS, testPlanDocument, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new TSPlanConfigurationFormatException("Failed to parse provided test plan using a xpath expression. Error: " + e.getMessage(), e);
		}

		if(configurationOptionsNodes != null && configurationOptionsNodes.getLength() > 0) {
			for(int i = 0; i < configurationOptionsNodes.getLength(); i++) {
				TSPlanConfigOption cfgOption = configOptionsParser.parseConfigurationNode(configurationOptionsNodes.item(i), null);							
				result.put(cfgOption.getName(), cfgOption);
			}
		}
		
		return result;
	}
	
	/**
	 * Parses out the activities. If there are not activities, the result will be an empty (but non-null) map
	 * @param testDocument
	 * @return
	 * @throws TSPlanConfigurationFormatException
	 */
	protected Map<String, TSPlanActivity> parseActivities(Document testPlanDocument, Map<String, TSPlanConfigOption> globalOptions) throws TSPlanConfigurationFormatException {
		
		Map<String, TSPlanActivity> result = new HashMap<String, TSPlanActivity>();

		NodeList activityNodes = null;
		
		// use an xpath expression to parse out all activities below the acitivities tag
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			activityNodes = (NodeList)xpath.evaluate(XPATH_EXPRESSION_ALL_ACTIVITIES, testPlanDocument, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw new TSPlanConfigurationFormatException("Failed to parse provided test plan using a xpath expression. Error: " + e.getMessage(), e);
		} 
		
		if(activityNodes != null && activityNodes.getLength() > 0) {
			
			for(int i = 0; i < activityNodes.getLength(); i++) {

				try {
					String activityId = (String)xpath.evaluate(XPATH_EXPRESSION_ACTIVITY_ID_ATTRIBUTE, activityNodes.item(i), XPathConstants.STRING);
					String activityName = (String)xpath.evaluate(XPATH_EXPRESSION_ACTIVITY_NAME_ATTRIBUTE, activityNodes.item(i), XPathConstants.STRING);
					String activityUseGlobalConfig = (String)xpath.evaluate(XPATH_EXPRESSION_ACTIVITY_USE_GLOBAL_CONFIG_ATTRIBUTE, activityNodes.item(i), XPathConstants.STRING);
					String activityClass = (String)xpath.evaluate(XPATH_EXPRESSION_ACTIVITY_CLASS_NODE, activityNodes.item(i), XPathConstants.STRING);
					String activityDescription = (String)xpath.evaluate(XPATH_EXPRESSION_ACTIVITY_DESCRIPTION_NODE, activityNodes.item(i), XPathConstants.STRING);
					String activityContextVariable = (String)xpath.evaluate(XPATH_EXPRESSION_ACTIVITY_CONTEXT_VARIABLE_NODE, activityNodes.item(i), XPathConstants.STRING);
					String activityNextActivity = (String)xpath.evaluate(XPATH_EXPRESSION_ACTIVITY_NEXT_ACTIVITY,  activityNodes.item(i), XPathConstants.STRING);

					TSPlanConfigOption activityConfigOptions = null;
					
					try {
						NodeList activityNodeConfigOptions= (NodeList)xpath.evaluate(XPATH_EXPRESSION_ACTIVITY_CONFIGURATION_NODES, activityNodes.item(i), XPathConstants.NODESET);

						if(activityNodeConfigOptions.getLength() == 1 ) {
							activityConfigOptions = configOptionsParser.parseConfigurationNode(activityNodeConfigOptions.item(0), activityName);
						}
					} catch (XPathExpressionException e) {
						throw new TSPlanConfigurationFormatException("Failed to parse provided test plan using a xpath expression. Error: " + e.getMessage(), e);
					} 
					
					TSPlanActivity activity = getActivityParserInstance(activityClass);
					activity.setId(activityId);
					activity.setClassName(activityClass);
					activity.setContextVariable(activityContextVariable);
					activity.setDescription(activityDescription);
					activity.setName(activityName);
					activity.setNextActivity(activityNextActivity);
					if(activityUseGlobalConfig != null && !activityUseGlobalConfig.isEmpty()) {
						TSPlanConfigOption cfgOpt = globalOptions.get(activityUseGlobalConfig);
						if(cfgOpt == null)
							throw new TSPlanConfigurationFormatException("No such global configuration option found for name '"+activityUseGlobalConfig+"'");
						activity.setConfiguration(cfgOpt);
					} else {
						activity.setConfiguration(activityConfigOptions);
					}

					
					// TODO insert activity options using reflection 
					
					activity.postInit();
					
					if(logger.isDebugEnabled())
						logger.debug("Parsed activity: [id="+activityId+", name="+activityName+", class="+activityClass+", useGlobalCfg="+activityUseGlobalConfig+", ctxVarName="+activityContextVariable+", nextActivity="+activityNextActivity+", description="+activityDescription+"]");
					
					result.put(activityName, activity);
						
				} catch (XPathExpressionException e) {
					throw new TSPlanConfigurationFormatException("Failed to parse activity using a xpath expression. Error: " + e.getMessage(), e);
				} catch (TSPlanInstantiationException e) {
					throw new TSPlanConfigurationFormatException("Failed to instantiate a referenced activity. Error: " + e.getMessage(), e);				
				} catch (TSPlanActivityExecutionException e) {
					throw new TSPlanConfigurationFormatException("Failed to post initialize activity instance. Error: " + e.getMessage(), e);
				}
				
			}

		}
		
		if(logger.isDebugEnabled())
			logger.debug("Successfully parsed " + result.size() + " activities from the provided test plan");
		
		return result;
		
	}
	
	/**
	 * Returns an instance of type {@link TSPlanActivity activity} 
	 * @param activityParser
	 * @return
	 */
	protected TSPlanActivity getActivityParserInstance(String activityClass) throws TSPlanInstantiationException {
		
		try {
			Class<?> clazz = Class.forName(activityClass);
			return (TSPlanActivity)clazz.newInstance();
		} catch(InstantiationException e) {
			e.printStackTrace();
			throw new TSPlanInstantiationException("Failed to instantiate activity '"+activityClass+"'. Error: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new TSPlanInstantiationException("Failed to instantiate activity '"+activityClass+"'. Error: " + e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new TSPlanInstantiationException("Failed to instantiate activity '"+activityClass+"'. Error: " + e.getMessage(), e);			
		} catch(ClassCastException e) {
			throw new TSPlanInstantiationException("Failed to instantiate activity '"+activityClass+"'. Error: " + e.getMessage(), e);
		}
		
	}
	
}
