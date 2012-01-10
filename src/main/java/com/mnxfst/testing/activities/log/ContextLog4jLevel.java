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

package com.mnxfst.testing.activities.log;

import java.io.Serializable;

/**
 * Defines valid log levels 
 * @author ckreutzfeldt
 *
 */
public enum ContextLog4jLevel implements Serializable {

	
	DEBUG ("debug"),
	INFO ("info"),
	WARN ("warn"),
	ERROR ("error"),
	UNKNOWN ("unknown");
		
	private String logLevel = null;
		
	ContextLog4jLevel(String logLevel) {
		this.logLevel = logLevel;
	}
		
	public static ContextLog4jLevel fromString(String logLevel) {
		if(logLevel != null) {
			for(ContextLog4jLevel level : ContextLog4jLevel.values()) {
				if(logLevel.equalsIgnoreCase(level.logLevel))
					return level;
			}
		}
		return null;
	}

}
