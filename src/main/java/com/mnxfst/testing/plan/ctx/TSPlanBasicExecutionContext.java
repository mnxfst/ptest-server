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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides a very basic {@link TSPlanExecutionContext context} implementation which simply keeps
 * all variable values stored in {@link Map maps}. This implementation is by far not thread-safe.
 * @author mnxfst
 * @since 17.01.2012
 */
public class TSPlanBasicExecutionContext implements TSPlanExecutionContext {

	private static final long serialVersionUID = 4045809931388824809L;
	
	/** used for storing all transient variables */
	private Map<String, Serializable> transientVariables = new HashMap<String, Serializable>();
	
	/** used for storing all durable variables */
	private Map<String, Serializable> durableVariables = new HashMap<String, Serializable>();

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#addTransientVariable(java.lang.String, java.io.Serializable)
	 */
	public void addTransientVariable(String variableName, Serializable value) {
		this.transientVariables.put(variableName, value);
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#getTransientVariable(java.lang.String)
	 */
	public Serializable getTransientVariable(String variableName) {
		return this.transientVariables.get(variableName);
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#removeTransientVariable(java.lang.String)
	 */
	public void removeTransientVariable(String variableName) {
		this.transientVariables.remove(variableName);
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#hasTransientVariable(java.lang.String)
	 */
	public boolean hasTransientVariable(String variableName) {
		return this.transientVariables.containsKey(variableName);
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#getTransientVariableNames()
	 */
	public Set<String> getTransientVariableNames() {
		return this.transientVariables.keySet();
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#refreshTransientVariables()
	 */
	public void refreshTransientVariables() {
		this.transientVariables.clear();
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#addDurableVariable(java.lang.String, java.io.Serializable)
	 */
	public void addDurableVariable(String variableName, Serializable value) {
		this.durableVariables.put(variableName, value);
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#getDurableVariable(java.lang.String)
	 */
	public Serializable getDurableVariable(String variableName) {
		return this.durableVariables.get(variableName);
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#removeDurableVariable(java.lang.String)
	 */
	public void removeDurableVariable(String variableName) {
		this.durableVariables.remove(variableName);
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#hasDurableVariable(java.lang.String)
	 */
	public boolean hasDurableVariable(String variableName) {
		return this.durableVariables.containsKey(variableName);
	}

	/**
	 * @see com.mnxfst.testing.plan.ctx.TSPlanExecutionContext#getDurableVariableNames()
	 */
	public Set<String> getDurableVariableNames() {
		return this.durableVariables.keySet();
	}

}
