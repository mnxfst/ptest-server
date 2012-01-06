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
