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

package com.mnxfst.testing.exception;

import com.mnxfst.testing.plan.TSPlan;

/**
 * Thrown in case the {@link TSPlan} configuration could not be read
 * @author mnxfst
 *
 */
public class TSPlanConfigurationFormatException extends Exception {

	private static final long serialVersionUID = 553079579146496014L;


	public TSPlanConfigurationFormatException() {		
	}
	
	public TSPlanConfigurationFormatException(String msg) {
		super(msg);
	}
	
	public TSPlanConfigurationFormatException(Throwable cause) {
		super(cause);
	}
	
	public TSPlanConfigurationFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
