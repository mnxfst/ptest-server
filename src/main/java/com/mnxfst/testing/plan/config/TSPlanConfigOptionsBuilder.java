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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mnxfst.testing.exception.TSPlanConfigurationFormatException;
import com.mnxfst.testing.plan.TSPlanBuilder;

/**
 * Parses the information contained within the <i>configuration</i> block from
 * the test plan file
 * @author mnxfst
 * @since 20.12.2011
 */
public class TSPlanConfigOptionsBuilder {
	
	/**
	 * Parses a configuration node for its provided settings. The node must neither be null nor must its name be other than 'configuration'.
	 * If the caller provides a vale to <code>defaultName</code> it will be used for {@link TSPlanConfigOption#getName()} in case there is 
	 * no value provided to the <code>name</code> attribute.
	 * @param optionNode
	 * @return
	 * @throws TSPlanConfigurationFormatException
	 */
	public TSPlanConfigOption parseConfigurationNode(Node configurationNode, String defaultName) throws TSPlanConfigurationFormatException {

		// validate provided node
		if(configurationNode == null)
			throw new TSPlanConfigurationFormatException("Missing required configuration node");
		
		// validate name of provided node
		if(!TSPlanBuilder.TEST_PLAN_ACTIVITY_CONFIGURATION_NODE.equalsIgnoreCase(configurationNode.getNodeName()))
			throw new TSPlanConfigurationFormatException("Invalid node nam: " + configurationNode.getNodeName() + ". Expected: " + TSPlanBuilder.TEST_PLAN_ACTIVITY_CONFIGURATION_NODE);
						
		// (1) fetch attribute map
		// (2) get name attribute
		// (3) read out name attribute value if the attribute node is not null, otherwise use the value provided by defaultName
		// (4) if the cfgOptionName is empty, throw an exception
		NamedNodeMap attributes = configurationNode.getAttributes();
		Node nameAttribute = (attributes != null && attributes.getLength() > 0) ? attributes.getNamedItem(TSPlanBuilder.TEST_PLAN_CONFIGURATION_NAME_ATTRIBUTE) : null;
		String cfgOptionName = (nameAttribute != null ? nameAttribute.getNodeValue() : defaultName);
		if(cfgOptionName == null || cfgOptionName.isEmpty())
			throw new TSPlanConfigurationFormatException("The current configuration option either misses the name attribute or the caller did not provide a default value");

		// find child nodes and validate for empty result
		NodeList childNodes = configurationNode.getChildNodes();
		if(childNodes == null || childNodes.getLength() < 1)
			throw new TSPlanConfigurationFormatException("No child nodes found for current configuration element");

		TSPlanConfigOption option = new TSPlanConfigOption();
		option.setName(cfgOptionName.trim());

		// step through child nodes, extracting their contents, storing the results in the option set
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node cn = childNodes.item(i);
			
			// handle node only if it is an element node
			if(cn.getNodeType() == Node.ELEMENT_NODE) {
				
				// fetch the option name => node name
				String optionName = cn.getNodeName().trim();
				
				String optionValue = null;
				
				// fetch the value nodes and check for elements
				NodeList valueNodes = cn.getChildNodes();
				if(valueNodes != null && valueNodes.getLength() > 0) {
					// fetch the first node by default 
					// TODO in the future we could maybe handle elements having more than one child elements themselves or CDATA nodes
					Node vn = valueNodes.item(0);
					
					if(vn != null) {
						if(vn.getNodeType() == Node.TEXT_NODE) {
							optionValue = vn.getNodeValue();
						} else if(vn.getNodeType() == Node.CDATA_SECTION_NODE) {
							optionValue = vn.getNodeValue();
						}
					}
				}
				
				option.addOption(optionName, optionValue);
			}
			
		}
		
		return option;		
	}

	
}
