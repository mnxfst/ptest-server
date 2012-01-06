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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.mnxfst.testing.activities.AbstractTSPlanActivity;
import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.exception.TSPlanExecutionFailedException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Common base for http request activities
 * @author ckreutzfeldt
 *
 */
public abstract class AbstractHTTPRequestActivity extends AbstractTSPlanActivity {

	private static final Logger logger = Logger.getLogger(AbstractHTTPRequestActivity.class);
	
	private static final String REQUEST_PARAM_OPTION_PREFIX = "request.param.";
	
	protected static final String HTTP_HEADER_CONTENT_ENCODING = "content-encoding";
	protected static final String CONTENT_ENCODING_GZIP = "gzip";

	// basic information
	private String scheme = null;
	private String host = null;
	private int port = -1;
	private String path = null;
	private int maxConnections = 200;
	private String requestParameterEncoding = null;

	// proxy relevant variables
	private String proxyUrl = null;
	private int proxyPort = -1;
	private String proxyUser = null;
	private String proxyPassword = null;

	// internal variables used for pre-initialization
	protected DefaultHttpClient httpClient = null;	
	protected URI httpRequestURI = null;
	
	protected abstract void activitySpecificPostInit(TSPlanConfigOption cfgOpt) throws TSPlanActivityExecutionException;

	/**
	 * @see com.mnxfst.testing.activities.TSPlanActivity#postInit()
	 */
	public void postInit() throws TSPlanActivityExecutionException {

		// fetch options and transfer them to inner variables
		TSPlanConfigOption cfgOpt = getConfiguration();
		
		/////////////////////////////////////////////////////////////////////////
		// parse basic information
		this.scheme = (String)cfgOpt.getOption("scheme");
		this.host = (String)cfgOpt.getOption("host");
		this.path = (String)cfgOpt.getOption("path");
		this.requestParameterEncoding = (String)cfgOpt.getOption("parameterEncoding");
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

		Credentials proxyCredentials = null;
		if(this.proxyUser != null && !this.proxyUser.isEmpty())
			proxyCredentials = new UsernamePasswordCredentials(this.proxyUser, this.proxyPassword);
		/////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////
		// request parameters
		Map<String, String> requestParameters = new HashMap<String, String>();
		for(String key : cfgOpt.getOptions().keySet()) {
			if(key.startsWith(REQUEST_PARAM_OPTION_PREFIX)) {
				String value = (String)cfgOpt.getOption(key);
				requestParameters.put(key.substring(REQUEST_PARAM_OPTION_PREFIX.length(), key.length()), value);
			}
		}
		/////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////
		// set up http client and request uri
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		
		ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(schemeRegistry);
		ccm.setMaxTotal(maxConnections);
		// since we handle only one connection, the maxConnections value must be applied here too
		ccm.setDefaultMaxPerRoute(maxConnections);
		httpClient = new DefaultHttpClient(ccm);
		
		if(proxyUrl != null && !proxyUrl.isEmpty()) {
			HttpHost proxy = null;
			if(proxyPort != -1)
				proxy = new HttpHost(proxyUrl, proxyPort);
			else
				proxy = new HttpHost(proxyUrl);
			httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
			if(proxyCredentials != null)
				httpClient.getCredentialsProvider().setCredentials(new AuthScope(this.proxyUrl, this.proxyPort), proxyCredentials);
		}
		
		if(logger.isDebugEnabled())
			logger.debug("Successfully initialized client connection manager allowing max. "+ maxConnections + " connections");
		
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if(requestParameters != null && !requestParameters.isEmpty()) {
				for(String key : requestParameters.keySet()) {
					String value = requestParameters.get(key);					
					params.add(new BasicNameValuePair(key, value));
				}
			}
			
			this.httpRequestURI = URIUtils.createURI(scheme, host, port, path, (!params.isEmpty() ? URLEncodedUtils.format(params, requestParameterEncoding) : null), null);
		} catch(URISyntaxException e) {
			throw new TSPlanActivityExecutionException("Invalid URI syntax found for activity element '"+name+"'. Error: " + e.getMessage(), e);
		}	

		activitySpecificPostInit(cfgOpt);
	}
	
	/**
	 * Executes the provided {@link HttpUriRequest} using the previously configured http client
	 * @param request
	 * @return
	 * @throws TSPlanExecutionFailedException
	 */
	protected HttpResponse executeRequest(HttpUriRequest request) throws TSPlanActivityExecutionException{
		try {			
			return httpClient.execute(request);			
		} catch (ClientProtocolException e) {
			throw new TSPlanActivityExecutionException("Failed to access " + request.getURI() + ". Error: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new TSPlanActivityExecutionException("Failed to access " +  request.getURI() + ". Error: " + e.getMessage(), e);
		}
	}

	/**
	 * Fetches the content from the provided {@link HttpResponse response} object
	 * @param response
	 * @return
	 * @throws TSPlanActivityExecutionException
	 */
	protected byte[] getResponseContent(HttpResponse response) throws TSPlanActivityExecutionException {

		// ensure that neither the response object nor its entity attribute is null 
		if(response == null || response.getEntity() == null)
			throw new TSPlanActivityExecutionException("No valid response provided. Either the response is null or its expected content entity");
		
		InputStream responseContentStream = null;
		try {
			// fetch the content length, encoding and type from the response entity
			long contentLength = response.getEntity().getContentLength();
			String contentEncoding = (response.getEntity().getContentEncoding() != null ? response.getEntity().getContentEncoding().getValue() : null);
			String contentType = (response.getEntity().getContentType() != null ? response.getEntity().getContentType().getValue() : null);
						
			if(logger.isDebugEnabled())
				logger.debug("Content-Length: " + contentLength + ", Encoding: " + contentEncoding + ", Type: " + contentType);
			
			// read from gzip encoded stream if necessary
			if(CONTENT_ENCODING_GZIP.equalsIgnoreCase(contentEncoding)) {
				responseContentStream = new GZIPInputStream(response.getEntity().getContent());
			} else {			
				responseContentStream = response.getEntity().getContent();
			}
			
			// write entity content to byte array and return result
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			int readBytes = 0;
			byte[] buffer = new byte[4096];
			while((readBytes = responseContentStream.read(buffer)) != -1) {
				bOut.write(buffer, 0, readBytes);		
			}
						
			return bOut.toByteArray();
		} catch(IOException e) {
			throw new TSPlanActivityExecutionException("Failed to read content from response. Error: " + e.getMessage(), e);
		} finally {
			// ensure closing of response stream
			if(responseContentStream != null) {
				try {
					responseContentStream.close();
				} catch(IOException e) {
					throw new TSPlanActivityExecutionException("Failed to close response stream. Error: " + e.getMessage(), e);
				}
			}
		}
	}

}
