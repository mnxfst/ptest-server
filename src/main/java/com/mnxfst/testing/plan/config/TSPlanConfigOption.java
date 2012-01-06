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

package com.mnxfst.testing.plan.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Contains configuration information contained within the <i>configuration</i> section of the test plan xml document
 * @author mnxfst
 * @since 20.12.2011
 */
public class TSPlanConfigOption implements Serializable {
	
	private static final long serialVersionUID = 6394459683258278802L;

	/** holds the assigned option name like 'cassandraStats' */
	private String name = null;
	/** holds the assigned options as mapped values of type 'serializable' - the using activities must ensure the correctness */
	private Map<String, Serializable> options = new HashMap<String, Serializable>();
	
	public TSPlanConfigOption() {
		
	}
	
	public TSPlanConfigOption(String name) {
		this.name = name;
	}
	
	/**
	 * Adds a new option or replaces an existing one
	 * @param name
	 * @param value
	 */
	public void addOption(String name, Serializable value) {
		this.options.put(name, value);
	}
	
	/**
	 * Removes an exiting option
	 * @param name
	 */
	public void removeOption(String name) {
		this.options.remove(name);
	}
	
	/**
	 * Returns the value for a given attribute
	 * @param name
	 * @return
	 */
	public Serializable getOption(String name) {
		return options.get(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Serializable> getOptions() {
		return options;
	}

	public void setOptions(Map<String, Serializable> options) {
		this.options = options;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("name", name);
		
		for(String key : options.keySet())
			builder.append(key, options.get(key));

		return builder.toString();
	}
	
	
}
