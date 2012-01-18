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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

import junit.framework.Assert;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.mnxfst.testing.exception.TSPlanActivityExecutionException;
import com.mnxfst.testing.plan.config.TSPlanConfigOption;

/**
 * Test case for {@link HTTPRequestActivity} 
 * @author mnxfst
 * @since 13.01.2012
 */
public class TestHTTPRequestActivity {

	@Test
	public void testPostInit() throws TSPlanActivityExecutionException {
		
		HTTPRequestActivity activity = new HTTPRequestActivity();
		try {
			activity.initialize(null);
			Assert.fail("Config options missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		TSPlanConfigOption cfg = new TSPlanConfigOption();
		try {
			activity.initialize(cfg);
			Assert.fail("Config option 'scheme' missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
			
		cfg.addOption("scheme", "http");
		try {
			activity.initialize(cfg);
			Assert.fail("Config option 'host' missing");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfg.addOption("host", "localhost");
		cfg.addOption("port", "dd");
		try {
			activity.initialize(cfg);
			Assert.fail("Config option 'port' holds an invalid value");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfg.addOption("port", "8080");
		cfg.addOption("maxConnections", "");
		cfg.addOption("responseContent", "test");
		activity.initialize(cfg);

		cfg.addOption("maxConnections", "dd");
		try {
			activity.initialize(cfg);
			Assert.fail("Config option 'maxConnections' holds an invalid value");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		cfg.addOption("maxConnections", "300");
			
		
		cfg.addOption("proxyPort", "");
		activity.initialize(cfg);
		
		cfg.addOption("proxyPort", "dd");
		try {
			activity.initialize(cfg);
			Assert.fail("Config option 'proxyPort' holds an invalid value");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfg.addOption("userAgent", "junit-test");
		cfg.addOption("httpProtocolVersion", "111");
		try {
			activity.initialize(cfg);
			Assert.fail("Invalid http protocol version");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		
		cfg.addOption("httpProtocolVersion", "1.1");
		cfg.addOption("httpRequestProcessors", "org.apache.http.protsocol.RequestContent,org.apache.http.protocol.RequestTargetHost,org.apache.http.protocol.RequestConnControl,org.apache.http.protocol.RequestUserAgent,org.apache.http.protocol.RequestExpectContinue");
		try {
			activity.initialize(cfg);
			Assert.fail("Invalid processor");
		} catch(TSPlanActivityExecutionException e) {
			//
		}

		cfg.addOption("httpRequestProcessors", "org.apache.http.protocol.RequestContent,org.apache.http.protocol.RequestTargetHost,org.apache.http.protocol.RequestConnControl,org.apache.http.protocol.RequestUserAgent,org.apache.http.protocol.RequestExpectContinue");
		
		cfg.addOption("method", "GETT");
		try {
			activity.initialize(cfg);
			Assert.fail("Invalid method");
		} catch(TSPlanActivityExecutionException e) {
			//
		}
		cfg.addOption("method", "POST");

		cfg.addOption("proxyUrl", "proxy.mnxfst.com");
		cfg.addOption("proxyPort", "1234");
		cfg.addOption("proxyUser", "user");
		cfg.addOption("proxyPassword", "test");
		cfg.addOption("request.param.key1", "value1");
		cfg.addOption("request.param.key2", "value2");
		activity.initialize(cfg);
		
	}
	
	
	public void testExecuteHTTPRequest() throws HttpException, IOException {
		
		HttpParams params = new SyncBasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, "UTF-8");
	    HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
	    HttpProtocolParams.setUseExpectContinue(params, false);
	    
	    HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
	    HttpContext context = new BasicHttpContext(null);
	    HttpHost host = new HttpHost("www.heise.de", 80);

        DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
        ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();

        context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);
        
 try {
            
            HttpEntity[] requestBodies = {
                    new StringEntity(
                            "This is the first test request", "UTF-8"),
                    new ByteArrayEntity(
                            "This is the second test request".getBytes("UTF-8")),
                    new InputStreamEntity(
                            new ByteArrayInputStream(
                                    "This is the third test request (will be chunked)"
                                    .getBytes("UTF-8")), -1)
            };

            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpRequestInterceptor[] {
                    // Required protocol interceptors
                    new RequestContent(),
                    new RequestTargetHost(),
                    // Recommended protocol interceptors
                    new RequestConnControl(),
                    new RequestUserAgent(),
                    new RequestExpectContinue()});
            
            
            for (int i = 0; i < requestBodies.length; i++) {
                if (!conn.isOpen()) {
                    Socket socket = new Socket(host.getHostName(), host.getPort());
                    conn.bind(socket, params);
                }
                BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", 
                        "/");
                request.setEntity(requestBodies[i]);
                System.out.println(">> Request URI: " + request.getRequestLine().getUri());

                request.setParams(params);
                httpexecutor.preProcess(request, httpproc, context);
                HttpResponse response = httpexecutor.execute(request, conn, context);
                response.setParams(params);
                httpexecutor.postProcess(response, httpproc, context);
                
                System.out.println("<< Response: " + response.getStatusLine());
                System.out.println(EntityUtils.toString(response.getEntity()));
                System.out.println("==============");
                if (!connStrategy.keepAlive(response, context)) {
                    conn.close();
                } else {
                    System.out.println("Connection kept alive...");
                }
            }
        } finally {
            conn.close();
        }        
    
	}
	
}
