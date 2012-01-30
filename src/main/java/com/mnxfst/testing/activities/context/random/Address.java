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

package com.mnxfst.testing.activities.context.random;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Contains an address 
 * @author ckreutzfeldt
 * @since 25.01.2012
 */
public class Address implements Serializable {

	private static final long serialVersionUID = 9007764644980328229L;

	private String street = null;
	private String postalCode = null;
	private String city = null;
	private String countryCode = null;
	
	public Address() {		
	}
	
	public Address(String street, String postalCode, String city, String countryCode) {
		this.street = street;
		this.postalCode = postalCode;
		this.city = city;
		this.countryCode = countryCode;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
				.append("street", this.street)
				.append("postalCode", this.postalCode)
				.append("city", this.city)
				.append("countryCode", this.countryCode).toString();
	}
	
	
	
}
