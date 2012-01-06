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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

public class HTTPRequestActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(HTTPRequestActivity.class);
	
	private static final String REQUEST_PARAM_OPTION_PREFIX = "request.param.";
	
	private String method = null;
	private String scheme = null;
	private String host = null;
	private int port = -1;
	private String path = null;
	private int maxConnections = 200;
	
	private Map<String, String> requestParameters = new HashMap<String, String>();
	private String proxyUrl = null;
	private int proxyPort = -1;
	private String proxyUser = null;
	private String proxyPassword = null;
	
	private HttpClient httpClient = null;
	private HttpGet cachedGet = null;
	private HttpPost cachedPost = null;
	boolean postMethod = false;
	
	public HTTPRequestActivity()  {
	}

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#postInit()
	 */
	public void postInit() throws TSPlanActivityExecutionException {
		
		// fetch options and transfer them to inner variables
		TSPlanConfigOption cfgOpt = getConfiguration();
		
		/////////////////////////////////////////////////////////////////////////
		// parse out proxy settings
		this.proxyUrl = (String)cfgOpt.getOption("proxyUrl");
		try {
			String proxyPortStr = (String)cfgOpt.getOption("proxyPort");
			if(proxyPortStr != null && !proxyPortStr.isEmpty())
				this.proxyPort = Integer.parseInt(proxyPortStr);
		} catch(NumberFormatException e) {
			throw new TSPlanActivityExecutionException("Failed to parse proxy port");
		}
		this.proxyUser = (String)cfgOpt.getOption("proxyUser");
		this.proxyPassword = (String)cfgOpt.getOption("proxyPassword");
		/////////////////////////////////////////////////////////////////////////
		
		this.method = (String)cfgOpt.getOption("method");
		this.scheme = (String)cfgOpt.getOption("scheme");
		this.host = (String)cfgOpt.getOption("host");
		this.path = (String)cfgOpt.getOption("path");
		try {
			String portStr = (String)cfgOpt.getOption("port");
			if(portStr != null && !portStr.isEmpty())
				this.port = Integer.parseInt(portStr);
		} catch(NumberFormatException e) {
			throw new TSPlanActivityExecutionException("Failed to parse port");
		}
		try {
			String maxConnStr = (String)cfgOpt.getOption("maxConnections");
			if(maxConnStr != null && !maxConnStr.isEmpty())
				this.maxConnections = Integer.parseInt(maxConnStr);
		} catch(NumberFormatException e) {
			throw new TSPlanActivityExecutionException("Failed to parse maxConnections");
		}
			
		/////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////
		// request parameters
		for(String key : cfgOpt.getOptions().keySet()) {
			if(key.startsWith(REQUEST_PARAM_OPTION_PREFIX)) {
				String value = (String)cfgOpt.getOption(key);
				requestParameters.put(key.substring(REQUEST_PARAM_OPTION_PREFIX.length(), key.length()), value);
			}
		}
		/////////////////////////////////////////////////////////////////////////
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		
		if(logger.isDebugEnabled())
			logger.debug("Schema registry successfully initialized for HTTP and HTTPS");
		
		ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(schemeRegistry);
		ccm.setMaxTotal(maxConnections);
		// since we handle only one connection, the maxConnections value must be applied here too
		ccm.setDefaultMaxPerRoute(maxConnections);
		httpClient = new DefaultHttpClient(ccm);
		
		if(logger.isDebugEnabled())
			logger.debug("Successfully initialized client connection manager allowing max. "+ maxConnections + " connections");
		
		if(method.equalsIgnoreCase("get")) {
			
			postMethod = false;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if(requestParameters != null && !requestParameters.isEmpty()) {
				for(String key : requestParameters.keySet()) {
					String value = requestParameters.get(key);					
					params.add(new BasicNameValuePair(key, value));
				}
			}

			try {
				URI uri = URIUtils.createURI(scheme, host, port, path, (!params.isEmpty() ? URLEncodedUtils.format(params, "UTF-8") : null), null);
				this.cachedGet = new HttpGet(uri);
			} catch(URISyntaxException e) {
				throw new TSPlanActivityExecutionException("Invalid URI syntax found for activity element '"+name+"'. Error: " + e.getMessage(), e);
			}			
			
		} else {
			postMethod = true;

			try {
				URI uri = URIUtils.createURI(scheme, host, port, path, null, null);
				this.cachedPost = new HttpPost(uri);
			} catch(URISyntaxException e) {
				throw new TSPlanActivityExecutionException("Invalid URI syntax found for activity element '"+name+"'. Error: " + e.getMessage(), e);
			}			
			
			if(requestParameters != null && !requestParameters.isEmpty()) {
				HttpParams params = new BasicHttpParams();
				for(String key : requestParameters.keySet()) {
					String value = requestParameters.get(key);
					params.setParameter(key, value);					
				}
				this.cachedPost.setParams(params);
				
			}
		}
		
		if(logger.isDebugEnabled())
			logger.debug("Successfully pre-computed http call for type " + method);
		
	}

	/**
	 * @see com.mnxfst.testing.activities.AbstractTSPlanActivity#execute(java.util.Map)
	 */
	public Map<String, Serializable> execute(Map<String, Serializable> input) throws TSPlanActivityExecutionException {
		
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute((postMethod ? this.cachedPost : this.cachedGet));
		} catch (ClientProtocolException e) {
			throw new TSPlanActivityExecutionException("Failed to access " + (postMethod ? this.cachedPost.getURI() : this.cachedGet.getURI()) + ". Error: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new TSPlanActivityExecutionException("Failed to access " + (postMethod ? this.cachedPost.getURI() : this.cachedGet.getURI()) + ". Error: " + e.getMessage(), e);
		}
		
		try {
			StringBuffer result = new StringBuffer();
			InputStream contentStream = httpResponse.getEntity().getContent();
			int c = 0;
			byte[] buffer = new byte[2096]; 
			while((c = contentStream.read(buffer)) != -1) {
				result.append(new String(buffer));
			}
			contentStream.close();
			
			input.put(getContextVariable(), new String(result.toString().getBytes(), "UTF-8"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		

		return input;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void addRequestParameter(String key, String value) {
		this.requestParameters.put(key, value);
	}
	
	public void removeRequestParameter(String key) {
		this.requestParameters.remove(key);
	}
	
	public String getRequestParameter(String key) {
		return this.requestParameters.get(key);
	}

	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map<String, String> requestParameters) {
		this.requestParameters = requestParameters;
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

}
