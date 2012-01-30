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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	/** contains all replacement patterns available through this context implementation */
	private Map<String, TSPlanExecutionReplacementPattern> replacementPatternMapping = new HashMap<String, TSPlanExecutionReplacementPattern>();

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
	 * Returns the set of keys for the given storage type
	 * @param type
	 * @return
	 */
	public Set<String> getContextValueNames(ExecutionContextValueType type) {
		if(type != null && type == ExecutionContextValueType.GLOBAL)
			return globalValues.keySet();
		return transientRunValues.keySet();
	}
	
	/**
	 * Clears the run/transient store
	 */
	public void clearTransientValueStore() {
		this.transientRunValues.clear();
	}
	
	/**
	 * Returns true in case the given key is contained in the referenced store
	 * @param key
	 * @param type
	 * @return
	 */
	public boolean hasContextVariable(String key, ExecutionContextValueType type) {
		if(type != null && type == ExecutionContextValueType.GLOBAL)
			return globalValues.containsKey(key);
		return transientRunValues.containsKey(key);
		
	}
	
	
	/**
	 * Evaluates the given pattern which either starts with ${global.} or ${run.}. If there is no context variable
	 * that matches the named contained in the pattern, the pattern itself will be returned.
	 * @param replacementPattern
	 * @return
	 * @throws TSVariableEvaluationFailedException
	 */
	public Object evaluate(String replacementPattern) throws TSVariableEvaluationFailedException {

		// validate the replacement pattern
		if(replacementPattern == null || replacementPattern.isEmpty())
			throw new TSVariableEvaluationFailedException("No replacement pattern provided");

		// find replacement pattern in map of previously computed pattern
		TSPlanExecutionReplacementPattern pattern = replacementPatternMapping.get(replacementPattern);
		if(pattern != null) {
			switch(pattern.getVariableStoreType()) {
				case GLOBAL: {
					Serializable variable = globalValues.get(pattern.getName());
					return evaluateObject(variable, pattern.getAccessMethods());
				}
				case RUN: {
					Serializable variable = transientRunValues.get(pattern.getName());
					return evaluateObject(variable, pattern.getAccessMethods());
				}
				default:
					throw new TSVariableEvaluationFailedException("Invalid variable storage type ('"+pattern.getVariableStoreType()+"') found for pattern: " + replacementPattern);
			}
		}
		
		// if the pattern is not contained in the mentioned map, figure out how to evaluate it: for global or transient run variables
		if(replacementPattern.startsWith(REPLACEMENT_PATTERN_PREFIX_GLOBAL) && replacementPattern.endsWith("}")) {
			
			// extract the name of the context variable and try to fetch the associated value. if there is no value, return null
			String ctxVar = extractContextVariableName(replacementPattern, REPLACEMENT_PATTERN_PREFIX_GLOBAL);
			Serializable variable = globalValues.get(ctxVar);
			if(variable == null)
				return null;
			
			// if the variable has been found, try to extract the getter method names along the path described
			String[] getterMethodNames = extractGetterMethodNames(replacementPattern, REPLACEMENT_PATTERN_PREFIX_GLOBAL);
			if(getterMethodNames == null || getterMethodNames.length < 1)
				return variable;
			
			// convert the getter names into method representations
			List<Method> getterMethods = new ArrayList<Method>();
			extractGetterMethods(variable.getClass(), getterMethodNames, getterMethods);
			
			// create entity for previously mentioned association map for pattern information and insert it
			TSPlanExecutionReplacementPattern patternMapping = new TSPlanExecutionReplacementPattern(replacementPattern, ctxVar, ExecutionContextValueType.GLOBAL);
			for(Method m : getterMethods)
				patternMapping.addAccessMethod(m);
			replacementPatternMapping.put(replacementPattern, patternMapping);
			
			// evaluate the getter methods against the variable value
			return evaluateObject(variable, getterMethods);
		} else if(replacementPattern.startsWith(REPLACEMENT_PATTERN_PREFIX_RUN) && replacementPattern.endsWith("}")) {
			
			// extract the name of the context variable and try to fetch the associated value. if there is no value, return null
			String ctxVar = extractContextVariableName(replacementPattern, REPLACEMENT_PATTERN_PREFIX_RUN);
			Serializable variable = transientRunValues.get(ctxVar);
			if(variable == null)
				return null;			
			
			// if the variable has been found, try to extract the getter method names along the path described
			String[] getterMethodNames = extractGetterMethodNames(replacementPattern, REPLACEMENT_PATTERN_PREFIX_RUN);
			if(getterMethodNames == null || getterMethodNames.length < 1)
				return variable;
			
			// convert the getter names into method representations
			List<Method> getterMethods = new ArrayList<Method>();
			extractGetterMethods(variable.getClass(), getterMethodNames, getterMethods);

			// create entity for previously mentioned association map for pattern information and insert it
			TSPlanExecutionReplacementPattern patternMapping = new TSPlanExecutionReplacementPattern(replacementPattern, ctxVar, ExecutionContextValueType.GLOBAL);
			for(Method m : getterMethods)
				patternMapping.addAccessMethod(m);
			replacementPatternMapping.put(replacementPattern, patternMapping);

			// evaluate the getter methods against the variable value
			return evaluateObject(variable, getterMethods);
		}

		throw new TSVariableEvaluationFailedException("Invalid replacement pattern: " + replacementPattern + ". Expected prefix: ${global||run...}");

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
	 * Extracts the  {@link Method method representations} for the given path of getter methods 
	 * @param varType
	 * @param getterMethodNames
	 * @param result
	 * @throws TSVariableEvaluationFailedException
	 */
	protected void extractGetterMethods(Class<?> varType, String[] getterMethodNames, List<Method> result) throws TSVariableEvaluationFailedException {
		
		if(varType != null && getterMethodNames != null && getterMethodNames.length > 0) {

			String nextGetter = getterMethodNames[0];
			try {
				Method getterMethod = varType.getMethod(nextGetter, null);
				result.add(getterMethod);
				extractGetterMethods(getterMethod.getReturnType(), (String[])ArrayUtils.subarray(getterMethodNames, 1, getterMethodNames.length), result);
			} catch(NoSuchMethodException e) {
				throw new TSVariableEvaluationFailedException("No such getter '"+nextGetter+"' for class " + varType.getName());
			}
			
		}

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
	/**
	 * Extracts the context variable name from the given replacement pattern. The provided prefix helps to speed up the
	 * stripping and must be contained in the replacement pattern
	 * @param replacementPattern
	 * @param storageDependentPrefix
	 * @return
	 */
	protected String extractContextVariableName(String replacementPattern, String storageDependentPrefix) {
		
		String tmp = replacementPattern.substring(storageDependentPrefix.length());
		if(tmp.indexOf(".") == -1)
			return tmp.substring(0, tmp.length() - 1);
		return tmp.substring(0, tmp.indexOf('.'));

		
	}
}
