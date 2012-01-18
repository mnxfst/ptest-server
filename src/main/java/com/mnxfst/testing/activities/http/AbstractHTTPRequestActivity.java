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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
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

	private static final HttpRequestInterceptor[] EMPTY_HTTP_REQUEST_INTERCEPTOR_ARRAY = new HttpRequestInterceptor[0];
	
	private static final String CFG_OPT_SCHEME = "scheme";
	private static final String CFG_OPT_HOST = "host";
	private static final String CFG_OPT_PORT = "port";
	private static final String CFG_OPT_PATH = "path";
	private static final String CFG_OPT_MAX_CONNECTIONS = "maxConnections";
		
	private static final String CFG_OPT_PROXY_URL = "proxyUrl";
	private static final String CFG_OPT_PROXY_PORT = "proxyPort";
	private static final String CFG_OPT_PROXY_USER = "proxyUser";
	private static final String CFG_OPT_PROXY_PASSWORD = "proxyPassword";
	
	private static final String CFG_OPT_USER_AGENT = "userAgent";
	private static final String CFG_OPT_HTTP_PROTOCOL_VERSION = "httpProtocolVersion"; 
	private static final String CFG_OPT_CONTENT_CHARSET = "contentCharset";
	private static final String CFG_OPT_EXPECT_CONTINUE = "expectContinue";
	private static final String CFG_OPT_HTTP_REQUEST_PROCESSORS = "httpRequestProcessors";
	private static final String CFG_OPT_METHOD = "method";
	
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
	protected String path = null;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// protocol settings

	/** holds the user agent name */
	private String userAgent = null;
	/** http version to be used - default: 1.1*/
	private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
	/** content charset - default: UTF-8 */
	private String contentChartset = "UTF-8";
	/** expect continue setting - default: false */
	private boolean expectContinue = false;
	/** method - default: GET */
	protected String method = "GET";

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
	// request parameter
	
	/** holds the request parameters as key-value pairs of strings */
	protected HttpParams httpParameters = new SyncBasicHttpParams();

	/////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////
	// variables used for pre-computing required information
	
	/** http client used for establishing connection. using concrete implementation in order to have required signature available */
	private DefaultHttpClient httpClient = null; 
	/** URI */
	private URI destinationURI = null;
	/** max connections allowed per client */
	private int maxConnections = 200;
	/** http request / response processors */
	protected HttpProcessor httpRequestResponseProcessor = null;
	/** has http processors */
	protected boolean hasRequestResponseProcessors = false;
	/** request executor */
	protected HttpRequestExecutor httpRequestExecutor = new HttpRequestExecutor();
	/** request context */
	protected HttpContext httpRequestContext = new BasicHttpContext(null);
	/** http host information */
	protected HttpHost httpHost = null;
	/** client connection */
	protected DefaultHttpClientConnection clientConnection = new DefaultHttpClientConnection();
	/** connection strategy */
    protected ConnectionReuseStrategy httpConnectionStrategy = new DefaultConnectionReuseStrategy();
    /** additional header information */
    protected Map<String, String> header = new HashMap<String, String>();
    
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
		
		String portStr = (String)cfg.getOption(CFG_OPT_PORT);
		if(portStr != null && !portStr.isEmpty()) {
			try {
				this.port = Integer.parseInt(portStr.trim());
			} catch(NumberFormatException e) {
				throw new TSPlanActivityExecutionException("Failed to parse expected numerical value for config option '"+CFG_OPT_PORT+"' for activity '"+getName()+"'");
			}
		}

		this.path = (String)cfg.getOption(CFG_OPT_PATH);
		if(this.path == null || this.path.isEmpty())
			this.path = "/";

		String maxConnStr = (String)cfg.getOption(CFG_OPT_MAX_CONNECTIONS);
		if(maxConnStr != null && !maxConnStr.isEmpty()) {
			try {			
				this.maxConnections = Integer.parseInt(maxConnStr);
			} catch(NumberFormatException e) {
				throw new TSPlanActivityExecutionException("Failed to parse expected numerical value for config option '"+CFG_OPT_MAX_CONNECTIONS+"' for activity '"+getName()+"'");
			}
		}		
		
		// initialize http host and context
		if(port > 0)
			this.httpHost = new HttpHost(this.host, this.port);
		else
			this.httpHost = new HttpHost(this.host);
		
        httpRequestContext.setAttribute(ExecutionContext.HTTP_CONNECTION, clientConnection);
        httpRequestContext.setAttribute(ExecutionContext.HTTP_TARGET_HOST, httpHost);
		
		
		if(logger.isDebugEnabled())
			logger.debug("activity[name="+getName()+", id="+getId()+", maxConnections="+maxConnections+", scheme="+scheme+", host="+host+", port="+port+", path="+path+"]");
		
		/////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////////
		// protocol settings

		this.userAgent = (String)cfg.getOption(CFG_OPT_USER_AGENT);
		if(this.userAgent == null || this.userAgent.isEmpty())
			this.userAgent = "ptest-server";

		String protocolVersion = (String)cfg.getOption(CFG_OPT_HTTP_PROTOCOL_VERSION);
		if(protocolVersion != null && !protocolVersion.isEmpty()) {
			if(protocolVersion.equalsIgnoreCase("0.9"))
				this.httpVersion = HttpVersion.HTTP_0_9;
			else if(protocolVersion.equalsIgnoreCase("1.0"))
				this.httpVersion = HttpVersion.HTTP_1_0;
			else if(protocolVersion.equalsIgnoreCase("1.1"))
				this.httpVersion = HttpVersion.HTTP_1_1;
			else
				throw new TSPlanActivityExecutionException("Failed to parse http protocol version '"+protocolVersion+"'. Valid value: 0.9, 1.0 and 1.1");
		}
		
		this.contentChartset = (String)cfg.getOption(CFG_OPT_CONTENT_CHARSET);
		if(this.contentChartset == null || this.contentChartset.isEmpty())
			this.contentChartset = "UTF-8";
		
		String expectContStr = (String)cfg.getOption(CFG_OPT_EXPECT_CONTINUE);
		if(expectContStr != null && !expectContStr.isEmpty()) {
			this.expectContinue = Boolean.parseBoolean(expectContStr.trim());
		}
		
		HttpProtocolParams.setUserAgent(httpParameters, userAgent);
		HttpProtocolParams.setVersion(httpParameters, httpVersion);
		HttpProtocolParams.setContentCharset(httpParameters, contentChartset);
		HttpProtocolParams.setUseExpectContinue(httpParameters, expectContinue);
		
		String httpProcStr = (String)cfg.getOption(CFG_OPT_HTTP_REQUEST_PROCESSORS);
		if(httpProcStr != null && !httpProcStr.isEmpty()) {
			List<HttpRequestInterceptor> interceptors = new ArrayList<HttpRequestInterceptor>();
			String[] procClasses = httpProcStr.split(",");
			if(procClasses != null && procClasses.length > 0) {
				for(int i = 0; i < procClasses.length; i++) {
					try {
						Class<?> clazz = Class.forName(procClasses[i]);
						interceptors.add((HttpRequestInterceptor)clazz.newInstance());
						
						if(logger.isDebugEnabled())
							logger.debug("activity[name="+getName()+", id="+getId()+ ", httpRequestInterceptor="+procClasses[i]+"]");
					} catch(Exception e) {
						throw new TSPlanActivityExecutionException("Failed to instantiate http interceptor '"+procClasses[i]+"' for activity '"+getName()+"'. Error: " + e.getMessage());
					}
				}
			}

			this.httpRequestResponseProcessor = new ImmutableHttpProcessor((HttpRequestInterceptor[])interceptors.toArray(EMPTY_HTTP_REQUEST_INTERCEPTOR_ARRAY));
			this.hasRequestResponseProcessors = true;
		}
		
		this.method = (String)cfg.getOption(CFG_OPT_METHOD);
		if(method == null || method.isEmpty())
			this.method = "GET";
		if(!method.equalsIgnoreCase("get") && !method.equalsIgnoreCase("post"))
			throw new TSPlanActivityExecutionException("Invalid method '"+method+"' found for activity '"+getName()+"'");
				
		if(logger.isDebugEnabled())
			logger.debug("activity[name="+getName()+", id="+getId()+", method="+method+", user-agent="+userAgent+", httpVersion="+httpVersion+", contentCharset="+contentChartset+", expectContinue="+expectContinue+"]");
		
		/////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////////
		// fetch proxy settings
		
		this.proxyUrl = (String)cfg.getOption(CFG_OPT_PROXY_URL);
		
		String proxyPortStr = (String)cfg.getOption(CFG_OPT_PROXY_PORT);
		if(proxyPortStr != null && !proxyPortStr.isEmpty()) {
			try {
				this.proxyPort = Integer.parseInt(proxyPortStr.trim());
			} catch(NumberFormatException e) {
				throw new TSPlanActivityExecutionException("Failed to parse expected numerical value for config option '"+CFG_OPT_PROXY_PORT+"' for activity '"+getName()+"'");
			}
		}
		
		this.proxyUser = (String)cfg.getOption(CFG_OPT_PROXY_USER);		
		this.proxyPassword = (String)cfg.getOption(CFG_OPT_PROXY_PASSWORD);
		
		if(logger.isDebugEnabled())
			logger.debug("activity[name="+getName()+", id="+getId()+", proxyUrl="+proxyUrl+", proxyPort="+proxyPort+", proxyUser="+proxyUser+"]");

		/////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////////
		// fetch request parameters

		// unfortunately we must step through the whole set of keys ... 
		for(String key : cfg.getOptions().keySet()) {
			if(key.startsWith(REQUEST_PARAM_OPTION_PREFIX)) {
				String value = (String)cfg.getOption(key);
				String requestParameterName = key.substring(REQUEST_PARAM_OPTION_PREFIX.length(), key.length());				
				httpParameters.setParameter(requestParameterName, value);				
				if(logger.isDebugEnabled())
					logger.debug("activity[name="+getName()+", id="+getId()+", requestParameter="+requestParameterName+", value="+value+"]");
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////////
		// configure scheme registry and initialize http client
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		
		if(logger.isDebugEnabled())
			logger.debug("activity[name="+getName()+", id="+getId()+", registeredSchemes={http, https}]");
		
		ThreadSafeClientConnManager threadSafeClientConnectionManager = new ThreadSafeClientConnManager(schemeRegistry);
		threadSafeClientConnectionManager.setMaxTotal(maxConnections);
		threadSafeClientConnectionManager.setDefaultMaxPerRoute(maxConnections);
		this.httpClient = new DefaultHttpClient(threadSafeClientConnectionManager);

		if(logger.isDebugEnabled())
			logger.debug("activity[name="+getName()+", id="+getId()+", threadSafeClientConnectionManager=initialized]");
		
	}

	
	
	
	
	
	
	
}

