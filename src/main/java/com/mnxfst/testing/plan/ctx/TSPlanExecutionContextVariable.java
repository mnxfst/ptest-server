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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides additional information for each variable either being referenced
 * within the activity configurations (eg. soap request using variable reference
 * to fill in previously computed information) or being exported by the activities
 * @author mnxfst
 * @since 26.01.2012
 */
public class TSPlanExecutionContextVariable implements Serializable {

	private static final long serialVersionUID = -7265084723025339839L;

	/** variable name used for looking up the value from the context */
	private String name = null;
	/** names all replacement patterns found in any activity configuration and a list of getter methods to be executed along the object path */
	private Map<String, List<Method>> replacementPatternValueEvaluationPath = new HashMap<String, List<Method>>();
	/** 
	 * names all replacement patterns found in any activity configuration. if an item is not found in the replacementPatternValueEvaluationPath map
	 * it must be evaluated during runtime and written back into replacementPatternValueEvaluationPath
	 */
	private Set<String> replacementPatterns = new HashSet<String>();
	
	/**
	 * Default constructor
	 */
	public TSPlanExecutionContextVariable() {
		
	}
	
	/**
	 * Initializes the entity
	 * @param contextVariableName
	 * @param contextStoreType
	 */
	public TSPlanExecutionContextVariable(String contextVariableName) {
		this.name = contextVariableName;
	}
	
	/**
	 * Adds a new replacement pattern
	 * @param replacementPattern
	 */
	public void addReplacementPattern(String replacementPattern) {
		this.replacementPatterns.add(replacementPattern);
	}
	
	/**
	 * Adds the provided mapping to the internal representation and returns true. If the replacement pattern has not been 
	 * recognized by the set holding all replacement patterns, it will be added. The method returns false if either the
	 * replacement pattern is null or empty or if the provided method chain is null. An empty chain indicates that the
	 * value for the pattern must be looked up directly from the context and does not need to evaluated. 
	 * @param replacementPattern
	 * @param methodChain
	 * @return
	 */
	public boolean addReplacementPatternValueEvaluationPath(String replacementPattern, List<Method> methodChain) {
		
		if(replacementPattern == null || replacementPattern.isEmpty())
			return false;
		
		if(methodChain == null)
			return false;
		
		if(!this.replacementPatterns.contains(replacementPattern))
			this.replacementPatterns.add(replacementPattern);
		
		this.replacementPatternValueEvaluationPath.put(replacementPattern, methodChain);
		
		return true;
	}
	
	/**
	 * Returns the chain of methods to be executed on the object and the evaluated attribute values referenced 
	 * by the given pattern 
	 * @param replacementPattern
	 * @return
	 */
	public List<Method> getReplacementPatternValueEvaluationPath(String replacementPattern) {
		return replacementPatternValueEvaluationPath.get(replacementPattern);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the replacementPatterns
	 */
	public Set<String> getReplacementPatterns() {
		return replacementPatterns;
	}
}
