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
 * Defines valid appender types
 * @author ckreutzfeldt
 *
 */
public enum ContextLog4jAppenderType implements Serializable {

	FILE ("file"),
	CONSOLE ("console"),
	UNKNOWN ("unknown");
		
	private String appenderType = null;
		
	ContextLog4jAppenderType(String appenderType) {
		this.appenderType = appenderType;
	}
		
	public static ContextLog4jAppenderType fromString(String appenderType) {
		if(appenderType != null) {
			for(ContextLog4jAppenderType type : ContextLog4jAppenderType.values()) {
				if(appenderType.equalsIgnoreCase(type.appenderType))
					return type;
			}
		}
		return null;
	}
		
}
