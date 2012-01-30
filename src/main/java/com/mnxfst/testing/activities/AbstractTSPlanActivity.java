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

package com.mnxfst.testing.activities;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a common parent to single steps within a {@link ITSPlan test plan}.
 * These steps a so-called <i>activities</i>
 * @author mnxfst
 * @since 20.12.2011
 */
public abstract class AbstractTSPlanActivity implements TSPlanActivity {
	
	/** common identifier to all activities of the same type */
	public String id = null;
	/** unique activity name */
	public String name = null;
	/** test plan dependent activity description */
	public String description = null;
	/** activity class name */
	public String className = null;
	/** name of next activity */
	public String nextActivity = null;
	/** variable used for storing results etc */
	private Map<String, String> contextExportVariables = null;
	
	public AbstractTSPlanActivity() {
		
	}
		
	/**
	 * Returns a map containing the name of context variables associated with their pattern to be applied via String.replaceAll which 
	 * all show up in the provided input string
	 * @param input
	 * @return
	 */
	public Map<String, String> getContextVariablesFromString2(String input) {
		
		Map<String, String> variables = new HashMap<String, String>();
		
		if(input != null && !input.isEmpty()) {
			int index = 0;
			while((index < input.length()) && (index != -1)) {
				index = input.indexOf("${", index);
				if(index != -1 && index < input.length()) {
					String payloadVariable = input.substring(index, input.indexOf("}", index+1) + 1);
					if(payloadVariable != null && !payloadVariable.isEmpty()) {
						String contextVariableName = payloadVariable.substring(2, payloadVariable.length() - 1);
						payloadVariable = payloadVariable.replace("$", "\\$");
						payloadVariable = payloadVariable.replace("{", "\\{");
						payloadVariable = payloadVariable.replace("}", "\\}");
						
						variables.put(contextVariableName, payloadVariable);
					}
					index = index + 1;
				}			
			}
		}
		
		return variables;
	}
	
	public Map<String, String> getContextVariablesFromString(String input) {
		
		Map<String, String> variables = new HashMap<String, String>();
		
		if(input != null && !input.isEmpty()) {
			int index = 0;
			while((index < input.length()) && (index != -1)) {
				index = input.indexOf("${", index);
				if(index != -1 && index < input.length()) {
					String payloadVariable = input.substring(index, input.indexOf("}", index+1) + 1);
					if(payloadVariable != null && !payloadVariable.isEmpty()) {
						String pattern = new String(payloadVariable);
						pattern = pattern.replace("$", "\\$");
						pattern = pattern.replace("{", "\\{");
						pattern = pattern.replace("}", "\\}");
						
						variables.put(payloadVariable, pattern);
					}
					index = index + 1;
				}			
			}
		}
		
		return variables;
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getNextActivity() {
		return nextActivity;
	}

	public void setNextActivity(String nextActivity) {
		this.nextActivity = nextActivity;
	}

	/**
	 * @return the contextExportVariables
	 */
	public Map<String, String> getContextExportVariables() {
		return contextExportVariables;
	}

	/**
	 * @param contextExportVariables the contextExportVariables to set
	 */
	public void setContextExportVariables(Map<String, String> contextExportVariables) {
		this.contextExportVariables = contextExportVariables;
	}


	
}
