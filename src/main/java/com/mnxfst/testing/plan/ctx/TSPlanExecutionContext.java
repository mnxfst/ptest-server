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

package com.mnxfst.testing.plan.ctx;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.mnxfst.testing.exception.TSVariableEvaluationFailedException;
import com.mnxfst.testing.plan.TSPlan;
import com.mnxfst.testing.plan.exec.TSPlanExecutor;

/**
 * Holds all information and data required for executing a {@link TSPlan test plan}
 * @author mnxfst
 * @since 26.01.2012
 */
public class TSPlanExecutionContext implements Serializable {

	private static final long serialVersionUID = -8239932328234778246L;
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	private static final String REPLACEMENT_PATTERN_PREFIX_GLOBAL = "${global.";
	private static final String REPLACEMENT_PATTERN_PREFIX_RUN = "${run.";
	
	
	/** keeps all values exported by the executed activities in a durable store which is kept throughout the test plan execution */
	private Map<String, Serializable> globalValues = new HashMap<String, Serializable>();
	
	/** keeps all values exported by the executed activities in a transient store which is cleared after each test plan run by the {@link TSPlanExecutor} */
	private Map<String, Serializable> transientRunValues = new HashMap<String, Serializable>();
	
	/** keeps a mapping of all evaluated patterns for global variables to the chain of methods to be executed along the evaluation path */
	private Map<String, List<Method>> globalValueReplacementPatternEvaluationPath = new HashMap<String, List<Method>>();
	
	/** keeps a mapping of all evaluated patterns for transient variables to the chain of methods to be executed along the evaluation path */
	private Map<String, List<Method>> transientValueReplacementPatternEvaluationPath = new HashMap<String, List<Method>>();
	
	
	/**
	 * Adds the provided key/value pair to the referenced store type. If no type is provided,
	 * the default (run/transient) is assumed
	 * @param key
	 * @param value
	 * @param type
	 */
	public void addContextValue(String key, Serializable value, ExecutionContextValueType type) {
		if(type != null && type == ExecutionContextValueType.GLOBAL)
			this.globalValues.put(key, value);
		else
			this.transientRunValues.put(key, value);
	}
	
	/**
	 * Looks up the value associated with the given key from the referenced store. If no store type
	 * is provided the lookup will be directed to the default (run/transient) store. 
	 * @param key
	 * @param type
	 * @return
	 */
	public Serializable getContextValue(String key, ExecutionContextValueType type) {
		if(type != null && type == ExecutionContextValueType.GLOBAL)
			return globalValues.get(key);
		return transientRunValues.get(key);
	}
	
	/**
	 * Removes the value associated with the given key from the referenced store. If no store type
	 * is provided the default (run/transient) store will be assumed and receive the deletion order
	 * @param key
	 * @param type
	 */
	public void removeContextValue(String key, ExecutionContextValueType type) {
		if(type != null && type == ExecutionContextValueType.GLOBAL)
			this.globalValues.remove(key);
		else
			this.transientRunValues.remove(key);
	}
	
	/**
	 * Clears the run/transient store
	 */
	public void clearTransientValueStore() {
		this.transientRunValues.clear();
	}

	/**
	 * Evaluates the provided replacement pattern for the referenced variable. The replacement pattern must
	 * look like: ${global.person.firstname} or ${run.logMessage.timestamp}. If the replacement pattern does
	 * not follow the rule ${<global or run>.<context variable>.<attribute getter>} an {@link TSVariableEvaluationFailedException}
	 * will be thrown. In case the path either contains an attribute having no associated getter or the path 
	 * cannot be evaluated, the method throws an {@link TSVariableEvaluationFailedException exception} as well.
	 * @param contextVariable
	 * @param replacementPattern
	 * @return
	 * @throws TSVariableEvaluationFailedException thrown in case either the replacement pattern is invalid or the evaluation failed in any other way
	 */
	public Object evaluate(String contextVariable, String replacementPattern) throws TSVariableEvaluationFailedException {
		
		// validate the input parameter values
		if(contextVariable == null || contextVariable.isEmpty())
			throw new TSVariableEvaluationFailedException("No context variable name provided");
		
		if(replacementPattern == null || replacementPattern.isEmpty())
			throw new TSVariableEvaluationFailedException("No replacement pattern provided");
		
		// continue here if the replacement pattern starts with ${global. 
		if(replacementPattern.startsWith(REPLACEMENT_PATTERN_PREFIX_GLOBAL) && replacementPattern.endsWith("}")) {

			List<Method> globalEvalPath = globalValueReplacementPatternEvaluationPath.get(replacementPattern);
			
			
		} else if(replacementPattern.startsWith(REPLACEMENT_PATTERN_PREFIX_RUN) && replacementPattern.endsWith("}")) {
			
			// continue here if the replacement pattern starts with ${run.
			List<Method> transientEvalPath = transientValueReplacementPatternEvaluationPath.get(replacementPattern);
			
		} else {
			throw new TSVariableEvaluationFailedException("Invalid replacement pattern: " + replacementPattern + ". Expected prefix: ${global||run...}");
		}
		
		return null;
		
	}
		
	/**
	 * Extracts the getter method names that needs to be executed along the expression path for evaluate
	 * an objects value. The provided prefix helps to speed-up stripping down the storage dependent prefix, eg. ${global.
	 * @param replacementPattern the provided input is assumed to be not null and must contain the provided prefix
	 * @param storageDependentPrefix the provided input is assumed to be not null and not empty
	 * @return
	 */
	protected String[] extractGetterMethodNames(String replacementPattern, String storageDependentPrefix) {

		// strip out the named prefix and the closing brackts
		String[] splittedPath = replacementPattern.substring(storageDependentPrefix.length(), replacementPattern.length() - 1).split("\\.");
		
		if(splittedPath != null && splittedPath.length > 1) {			
			
			List<String> result = new ArrayList<String>();
			
			// iterate through path elements starting with the 2nd element since the first names the variable whereas the second references the first attribute
			// to be accessed via an assigned getter
			for(int i = 1; i < splittedPath.length; i++) {				
				String attrName = splittedPath[i];
				if(attrName != null && !attrName.isEmpty()) {
					result.add("get" + attrName.substring(0, 1).toUpperCase() + attrName.substring(1));					
				}
			}
			
			return (String[])result.toArray(EMPTY_STRING_ARRAY);
		}
		
		return EMPTY_STRING_ARRAY;		
	}
	
	/**
	 * Evaluates the provided array of getter methods on the given input object. Next to the value
	 * evaluation the method writes all {@link Method} instances extracted into a provided output list.
	 * @param input
	 * @param getterMethodNames
	 * @param outputMethodsList
	 * @return
	 * @throws TSVariableEvaluationFailedException
	 */
	protected Object evaluateObject(Object input, String[] getterMethodNames, List<Method> outputMethodsList) throws TSVariableEvaluationFailedException {

		if(input == null)
			return null;
		
		if(getterMethodNames != null && getterMethodNames.length > 0) {
			String nextGetter = getterMethodNames[0];
			Method nextGetterMethod = null;			
			Object result = null;

			try {
				nextGetterMethod = input.getClass().getMethod(nextGetter, null);
				result = nextGetterMethod.invoke(input, null);				
			} catch (IllegalArgumentException e) {
				throw new TSVariableEvaluationFailedException("Failed to evaluate method '"+nextGetter+"' on entity of type " + input.getClass().getName() + ". Error: " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new TSVariableEvaluationFailedException("Failed to evaluate method '"+nextGetter+"' on entity of type " + input.getClass().getName() + ". Error: " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new TSVariableEvaluationFailedException("Failed to evaluate method '"+nextGetter+"' on entity of type " + input.getClass().getName() + ". Error: " + e.getMessage());
			} catch (SecurityException e) {
				throw new TSVariableEvaluationFailedException("Failed to evaluate method '"+nextGetter+"' on entity of type " + input.getClass().getName() + ". Error: " + e.getMessage());
			} catch (NoSuchMethodException e) {
				throw new TSVariableEvaluationFailedException("Failed to evaluate method '"+nextGetter+"' on entity of type " + input.getClass().getName() + ". Error: " + e.getMessage());
			}
			
			outputMethodsList.add(nextGetterMethod);
			if(getterMethodNames.length > 1 && result != null)
				return evaluateObject(result, (String[])ArrayUtils.subarray(getterMethodNames, 1, getterMethodNames.length), outputMethodsList);
			return result;
			
		}
		
		return input;
	}
	
	/**
	 * Evaluates the provided list of methods on the given object and the results of the getter recursively 
	 * @param input
	 * @param getterMethods
	 * @return
	 * @throws TSVariableEvaluationFailedException
	 */
	protected Object evaluateObject(Object input, List<Method> getterMethods) throws TSVariableEvaluationFailedException {
		
		if(input == null)
			return null;
		
		if(getterMethods != null && !getterMethods.isEmpty()) {
			
			Method nextGetterMethod = getterMethods.get(0);
			Object result = null;
			
			try {
				result = nextGetterMethod.invoke(input, null);
			} catch (IllegalArgumentException e) {
				throw new TSVariableEvaluationFailedException("Failed to evaluate method '"+nextGetterMethod.getName()+"' on entity of type " + input.getClass().getName() + ". Error: " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new TSVariableEvaluationFailedException("Failed to evaluate method '"+nextGetterMethod.getName()+"' on entity of type " + input.getClass().getName() + ". Error: " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new TSVariableEvaluationFailedException("Failed to evaluate method '"+nextGetterMethod.getName()+"' on entity of type " + input.getClass().getName() + ". Error: " + e.getMessage());
			}
			
			if(getterMethods.size() > 1 && result != null)
				return evaluateObject(result, getterMethods.subList(1, getterMethods.size()));
			
			return result;
		}
		
		return input;
		
	}
	
}
