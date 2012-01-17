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

package com.mnxfst.testing.activities.http;

import java.net.URI;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Common base for http request activities
 * @author ckreutzfeldt
 *
 */
public abstract class AbstractHTTPRequestActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(AbstractHTTPRequestActivity.class);

	private static final String CFG_OPT_SCHEME = "scheme";
	private static final String CFG_OPT_HOST = "host";
	private static final String CFG_OPT_PORT = "port";
	private static final String CFG_OPT_PATH = "path";
	
	private static final String REQUEST_PARAM_OPTION_PREFIX = "request.param.";

	/////////////////////////////////////////////////////////////////////////////////////////
	// scheme, host, port and path
	
	/** holds the url scheme, eg. http, ftp or https */
	private String scheme = null;
	/** holds the host, eg. www.apache.org */
	private String host = null;
	/** holds the port, eg. 8080 */
	private int port = -1;
	/** holds the path, eg. /cms/content/index.html */
	private String path = null;
	/////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////
	// proxy settings
	
	/** holds the proxy url */
	private String proxyUrl = null;
	/** holds the proxy port */
	private int proxyPort = -1;
	/** holds the proxy user */
	private String proxyUser = null;
	/** holds the proxy password */
	private String proxyPassword = null;
	/////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////
	// variables used for pre-computing required information
	
	/** http client used for establishing connection. using concrete implementation in order to have required signature available */
	private DefaultHttpClient httpClient = null; 
	/** URI */
	private URI destinationURI = null;	
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#initialize(com.mnxfst.testing.plan.config.TSPlanConfigOption)
	 */
	public void initialize(TSPlanConfigOption cfg) throws TSPlanActivityExecutionException {

		if(cfg == null)
			throw new TSPlanActivityExecutionException("Failed to initialize activity '" + this.getClass().getName() + "' due to missing configuration options");

		
		/////////////////////////////////////////////////////////////////////////////////////////
		// fetch scheme, host, port and path

		this.scheme = (String)cfg.getOption(CFG_OPT_SCHEME);
		if(this.scheme == null || this.scheme.isEmpty())
			throw new TSPlanActivityExecutionException("Required config option '"+CFG_OPT_SCHEME+"' missing for activity '"+getName()+"'");
		
		this.host = (String)cfg.getOption(CFG_OPT_HOST);
		if(this.host == null || this.host.isEmpty())
			throw new TSPlanActivityExecutionException("Requied config option '"+CFG_OPT_HOST+"' missing for activity '"+getName()+"'");
		
		this.path = (String)cfg.getOption(CFG_OPT_PATH);
		if(this.path == null || this.path.isEmpty())
			this.path = "/";
		
		String portStr = (String)cfg.getOption(CFG_OPT_PORT);
		if(portStr != null && !portStr.isEmpty()) {
			try {
				this.port = Integer.parseInt(portStr.trim());
			} catch(NumberFormatException e) {
				throw new TSPlanActivityExecutionException("Failed to parse expected numerical value for config option '"+CFG_OPT_PORT+"' for activity '"+getName()+"'");
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////

		
	}

	
	
	
	
	
	
	
}
