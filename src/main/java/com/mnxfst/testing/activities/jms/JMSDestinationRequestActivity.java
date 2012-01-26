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

package com.mnxfst.testing.activities.jms;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;
import com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext;

/**
 * Activity implementation for sending requests to a jms destination 
 * @author ckreutzfeldt
 *
 */
public class JMSDestinationRequestActivity extends AbstractTSPlanActivity {

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfgOpt)
			throws TSPlanActivityExecutionException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#execute(com.mnxfst.testing.plan.ctx.ITSPlanExecutionContext)
	 */
	public ITSPlanExecutionContext execute(ITSPlanExecutionContext ctx)
			throws TSPlanActivityExecutionException {
		// TODO Auto-generated method stub
		return null;
	}


}
