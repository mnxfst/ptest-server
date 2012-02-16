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

package com.mnxfst.testing.server.handler;

import java.nio.charset.Charset;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.junit.Test;

/**
 * Test case for {@link TSHttpRequestHandler}
 * @author ckreutzfeldt
 * @since 15.02.2012
 */
public class TestTSHttpRequestHandler {

	private static String sampleConfigXML = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ptestplan><name>sample test plan name</name><description>sample description</description>"+
	"<creationDate>2011-12-20</creationDate><createdBy>mnxfst</createdBy><initActivity>randomRequestIdGenerator</initActivity><globalConfiguration><configuration name=\"rndCtxValGenCfg\"><key>value</key></configuration></globalConfiguration>"+
	"<activities><!-- random value generator activity --><activity id=\"randomCtxValueGenerator\" name=\"randomRequestIdGenerator\"><description>Generate random variable values and store them under the given variable</description>"+
	"<class>com.mnxfst.testing.activities.context.random.RandomCtxVarGenActivity</class><configuration><generate.variable.requestId.class>com.mnxfst.testing.activities.context.random.RandomUUIDValueGenerator</generate.variable.requestId.class>"+
	"<generate.variable.requestId.uuidType>TIME</generate.variable.requestId.uuidType><generate.variable.address.class>com.mnxfst.testing.activities.context.random.RandomAddressGenerator</generate.variable.address.class>"+
	"</configuration><nextActivity>finalTimestamp</nextActivity></activity><!-- set end timestamp --><activity id=\"timestamp\" name=\"finalTimestamp\"><description>Sets an initial timestamp stored under the given variable</description>"+
	"<class>com.mnxfst.testing.activities.timer.TimestampActivity</class><contextExportVars><timestamp>finalTimestamp</timestamp></contextExportVars><nextActivity>finish</nextActivity></activity>"+
	"</activities></ptestplan>");
	
	@Test
	public void testPostParsing() {
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/runme");		
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer("me=you&testplan="+sampleConfigXML, Charset.defaultCharset());
		request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
		request.setContent(buffer);
		
		
		
		QueryStringDecoder decoder = new QueryStringDecoder("?" +
			    request.getContent().toString(CharsetUtil.UTF_8));
		
		System.out.println(decoder.getParameters());
		
		for(String k : decoder.getParameters().keySet()) {
			List<String> v = decoder.getParameters().get(k);
			System.out.println(k + ":" + v.get(0));
		}

	}
	
}
