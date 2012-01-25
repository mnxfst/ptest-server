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

package com.mnxfst.testing.activities.context;

import java.util.Random;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Random {@link Address address} generator 
 * @author ckreutzfeldt
 * TODO extend to use http://www.geonames.org/ for generating real-world addresses
 */
public class RandomAddressGenerator implements IRandomCtxVarValueGenerator<Address> {

	private Random randomNumberGenerator = new Random();
	/**
	 * @see com.mnxfst.testing.activities.context.IRandomCtxVarValueGenerator#init(com.mnxfst.testing.plan.config.TSPlanConfigOption, java.lang.String)
	 */
	public void init(TSPlanConfigOption cfgOpt, String generatorCfgOptPrefix) throws TSPlanActivityExecutionException {
	}

	/**
	 * @see com.mnxfst.testing.activities.context.IRandomCtxVarValueGenerator#generate()
	 */
	public Address generate() {
				
		// german address
		if(randomNumberGenerator.nextInt(2) == 0) {
			
			// Hamburg
			if(randomNumberGenerator.nextInt(2) == 0) {
				return new Address("Neuer Jungfernstieg", "20354", "Hamburg", "DEU");				
			} else {
				// Berlin
				return new Address("Alexanderplatz", "10178", "Berlin", "DEU");
			}
			
		} else {
			// british address
			
			// London
			if(randomNumberGenerator.nextInt(2) == 0) {
				return new Address("Trafalgar Square", "WC2N 5DN", "London", "GBR");
			} else {
				return new Address("Monks Way", "MK8 8AA", "Milton Keynes", "GBR");
			}
		}
		
	}
	
	public static void main(String[] args) {
		RandomAddressGenerator g = new RandomAddressGenerator();
		for(int i = 0; i <= 20; i++) {
			System.out.println(g.generate());
		}
	}

}
