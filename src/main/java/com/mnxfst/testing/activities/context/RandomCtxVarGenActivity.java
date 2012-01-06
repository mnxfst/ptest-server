package com.mnxfst.testing.activities.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Generates random values and stores them under the given variable 
 * @author ckreutzfeldt
 *
 */
public class RandomCtxVarGenActivity extends AbstractTSPlanActivity {
	
	private static final Logger logger = Logger.getLogger(RandomCtxVarGenActivity.class);

	private static final String GENERATE_VARIABLE_NAME_PREFIX = "generate.variable.";
	
	private Map<String, String> generateVariables = new HashMap<String, String>(); 
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#postInit()
	 */
	public void postInit() throws TSPlanActivityExecutionException {
		TSPlanConfigOption cfgOpt = getConfiguration();
		
		for(String key : cfgOpt.getOptions().keySet()) {
			if(key.startsWith(GENERATE_VARIABLE_NAME_PREFIX)) {
				String variableType = (String)cfgOpt.getOption(key);
				String variable = key.substring(GENERATE_VARIABLE_NAME_PREFIX.length(), key.length());
				generateVariables.put(variable, variableType);
				
			}
		}		

	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(java.util.Map)
	 */
	public Map<String, Serializable> execute(Map<String, Serializable> input) throws TSPlanActivityExecutionException {
		
		for(String varName : generateVariables.keySet()) {
			input.put(varName, "1");
		}
		
		return input;
	}

}
