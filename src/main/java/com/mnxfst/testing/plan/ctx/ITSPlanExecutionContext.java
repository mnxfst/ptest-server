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
import java.util.Set;

import com.mnxfst.testing.exception.TSVariableEvaluationFailedException;
import com.mnxfst.testing.plan.TSPlan;

/**
 * Provides an interface description for all context implementations
 * @author mnxfst
 * @since 17.01.2012
 */
public interface ITSPlanExecutionContext extends Serializable {

	public enum ContextVariableType implements Serializable {		
		TRANSIENT,
		DURABLE,
		BOTH		
	}
	
	/**
	 * Adds the provided key/value pair to the stack of transient variables
	 * which is at least available during a single {@link TSPlan test plan} run 
	 * @param variableName
	 * @param value
	 */
	public void addTransientVariable(String variableName, Serializable value);

	/**
	 * Returns the value for the named transient variable. If there is no such variable,
	 * the method returns null.
	 * @param variableName
	 * @return
	 */
	public Serializable getTransientVariable(String variableName);
	
	/**
	 * Removes the referenced transient variable
	 * @param variableName
	 */
	public void removeTransientVariable(String variableName);
	
	/**
	 * Returns true in case a mapping for the given name exists
	 * @param variableName
	 * @return
	 */
	public boolean hasTransientVariable(String variableName);
	
	/**
	 * Returns the names of all transient variables
	 * @return
	 */
	public Set<String> getTransientVariableNames();
	
	/**
	 * Refreshes the transient variable cache
	 */
	public void refreshTransientVariables();
	
	/**
	 * Adds the provided key/value pair to the stack of durable variables which is
	 * available during a whole {@link TSPlan test plan} execution (including all reiterations).
	 * @param variableName
	 * @param value
	 */
	public void addDurableVariable(String variableName, Serializable value);
	
	/**
	 * Returns the value for the named durable variable. If there is no such variable,
	 * the method returns null.
	 * @param variableName
	 * @return
	 */
	public Serializable getDurableVariable(String variableName);
	
	/**
	 * Removes the referenced durable variable
	 * @param variableName
	 */
	public void removeDurableVariable(String variableName);
	
	/**
	 * Returns true in case a mapping for the given name exists
	 * @param variableName
	 * @return
	 */
	public boolean hasDurableVariable(String variableName);
	
	/**
	 * Returns the names of all durable variables
	 * @return
	 */
	public Set<String> getDurableVariableNames();
	
	/**
	 * Evaluates the given path on the provided object. The path expression starts with the object name
	 * thus the first getter to be called is found right after the first dot, eg. objName.street returns
	 * obj.getStreet and objName returns obj
	 * @param obj
	 * @param pathExpression
	 * @return
	 * @throws TSVariableEvaluationFailedException
	 */
	public Object evaluate(Object obj, String pathExpression) throws TSVariableEvaluationFailedException;
		
	/**
	 * Returns the value for the referenced context variable. Depending on the given type the method either returns
	 * a value from the transient or the durable storage. In case the type is set to both, the method first looks
	 * up the transient storage followed by the durable storage in case the first lookup return null 
	 * @param contextVariable
	 * @param type
	 * @return
	 */
	public Serializable findContextVariable(String contextVariable, ContextVariableType type);
}
