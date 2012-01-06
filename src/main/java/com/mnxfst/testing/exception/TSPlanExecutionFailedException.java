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
 * Thrown in case the execution of an {@link TSPlan} failed due to
 * a general or unknown reason
 * @author mnxfst
 *
 */
public class TSPlanExecutionFailedException extends Exception {

	private static final long serialVersionUID = -4825237744035123502L;

	public TSPlanExecutionFailedException() {		
	}
	
	public TSPlanExecutionFailedException(String msg) {
		super(msg);
	}
	
	public TSPlanExecutionFailedException(Throwable cause) {
		super(cause);
	}
	
	public TSPlanExecutionFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
