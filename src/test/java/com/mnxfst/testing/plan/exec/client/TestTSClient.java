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

package com.mnxfst.testing.plan.exec.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.mnxfst.testing.exception.TSClientConfigurationException;
import com.mnxfst.testing.exception.TSClientExecutionException;
import com.mnxfst.testing.plan.exec.TSPlanRecurrenceType;

/**
 * Test case for {@link TSClient}
 * @author mnxfst
 * @since 10.02.2012
 */
public class TestTSClient {

	@Test
	public void testeExecuteClient() {
		
		TSClient client = new TSClient();
		client.executeClient(null);
		
		client.executeClient(new String[]{"-execute", "-collect"});
	}
	
	@Test
	public void testExtractLongValue() throws ClientProtocolException, IOException, SAXException, ParserConfigurationException, ParseException, TSClientConfigurationException {

		TSClient client = new TSClient();		
		Options options = client.getTSClientCommandLineOptions();
				
		try {
			client.extractLongValue(client.parseCommandline(options, null), TSClient.CMD_OPT_THREADS, TSClient.CMD_OPT_THREADS_SHORT);
			Assert.fail("Invalid command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractLongValue(client.parseCommandline(options, new String[0]), TSClient.CMD_OPT_THREADS, TSClient.CMD_OPT_THREADS_SHORT);
			Assert.fail("Empty command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractLongValue(client.parseCommandline(options, new String[]{"no", "valid", "value"}), TSClient.CMD_OPT_THREADS, TSClient.CMD_OPT_THREADS_SHORT);
			Assert.fail("Command-line contains no required value");
		} catch(TSClientConfigurationException e) {
			//
		}
						
		String[] args = new String[]{"-threads", "2"};
		Assert.assertEquals("The number of request threads must be 2", 2, client.extractLongValue(client.parseCommandline(options, args), TSClient.CMD_OPT_THREADS, TSClient.CMD_OPT_THREADS_SHORT));
		args = new String[]{"-t", "2"};
		Assert.assertEquals("The number of request threads must be 2", 2, client.extractLongValue(client.parseCommandline(options, args), TSClient.CMD_OPT_THREADS, TSClient.CMD_OPT_THREADS_SHORT));
		
		args = new String[]{"-recurrences", "3"};
		Assert.assertEquals("The number of recurrences must be 3", 3, client.extractLongValue(client.parseCommandline(options, args), TSClient.CMD_OPT_RECURRENCES, TSClient.CMD_OPT_RECURRENCES_SHORT));
		args = new String[]{"-r", "3"};
		Assert.assertEquals("The number of recurrences must be 3", 3, client.extractLongValue(client.parseCommandline(options, args), TSClient.CMD_OPT_RECURRENCES, TSClient.CMD_OPT_RECURRENCES_SHORT));		

		args = new String[]{"-ptestPort", "9090"};
		Assert.assertEquals("The port must be 9090", 9090, client.extractLongValue(client.parseCommandline(options, args), TSClient.CMD_OPT_PTEST_SERVER_PORT, TSClient.CMD_OPT_PTEST_SERVER_PORT_SHORT));
		args = new String[]{"-p", "9090"};
		Assert.assertEquals("The port must be 9090", 9090, client.extractLongValue(client.parseCommandline(options, args), TSClient.CMD_OPT_PTEST_SERVER_PORT, TSClient.CMD_OPT_PTEST_SERVER_PORT_SHORT));
	}
	
	@Test
	public void testExtractRecurrenceType() throws ParseException, TSClientConfigurationException {
		TSClient client = new TSClient();		
		Options options = client.getTSClientCommandLineOptions();
				
		try {
			client.extractRecurrenceType(client.parseCommandline(options, null));
			Assert.fail("Invalid command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractRecurrenceType(client.parseCommandline(options, new String[0]));
			Assert.fail("Empty command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractRecurrenceType(client.parseCommandline(options, new String[]{"no", "valid", "value"}));
			Assert.fail("Command-line contains no required value");
		} catch(TSClientConfigurationException e) {
			//
		}
				
		try {
			client.extractRecurrenceType(client.parseCommandline(options, null));
			Assert.fail("Invalid command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractRecurrenceType(client.parseCommandline(options, new String[0]));
			Assert.fail("Empty command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractRecurrenceType(client.parseCommandline(options, new String[]{"no", "valid", "value"}));
			Assert.fail("Command-line contains no required value");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		String[] args = new String[]{"-recurrenceType", "times"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.TIMES, TSPlanRecurrenceType.TIMES, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "times"})));
		args = new String[]{"-recurrenceType", "millis"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.MILLIS, TSPlanRecurrenceType.MILLIS, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "millis"})));
		args = new String[]{"-recurrenceType", "seconds"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.SECONDS, TSPlanRecurrenceType.SECONDS, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "seconds"})));
		args = new String[]{"-recurrenceType", "minutes"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.MINUTES, TSPlanRecurrenceType.MINUTES, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "minutes"})));
		args = new String[]{"-recurrenceType", "hours"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.HOURS, TSPlanRecurrenceType.HOURS, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "hours"})));
		args = new String[]{"-recurrenceType", "days"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.DAYS, TSPlanRecurrenceType.DAYS, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "days"})));

		args = new String[]{"-rt", "times"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.TIMES, TSPlanRecurrenceType.TIMES, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "times"})));
		args = new String[]{"-rt", "millis"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.MILLIS, TSPlanRecurrenceType.MILLIS, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "millis"})));
		args = new String[]{"-rt", "seconds"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.SECONDS, TSPlanRecurrenceType.SECONDS, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "seconds"})));
		args = new String[]{"-rt", "minutes"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.MINUTES, TSPlanRecurrenceType.MINUTES, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "minutes"})));
		args = new String[]{"-rt", "hours"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.HOURS, TSPlanRecurrenceType.HOURS, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "hours"})));
		args = new String[]{"-rt", "days"};
		Assert.assertEquals("The type must be equal to " + TSPlanRecurrenceType.DAYS, TSPlanRecurrenceType.DAYS, client.extractRecurrenceType(client.parseCommandline(options, new String[]{"-recurrenceType", "days"})));
	}
	
	@Test
	public void testExtractString() throws TSClientConfigurationException, ParseException {

		TSClient client = new TSClient();		
		Options options = client.getTSClientCommandLineOptions();
				
		try {
			client.extractStringValue(client.parseCommandline(options, null), TSClient.CMD_OPT_TESTPLAN, TSClient.CMD_OPT_TESTPLAN_SHORT);
			Assert.fail("Invalid command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractStringValue(client.parseCommandline(options, new String[0]), TSClient.CMD_OPT_TESTPLAN, TSClient.CMD_OPT_TESTPLAN_SHORT);
			Assert.fail("Empty command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractStringValue(client.parseCommandline(options, new String[]{"no", "valid", "value"}), TSClient.CMD_OPT_TESTPLAN, TSClient.CMD_OPT_TESTPLAN_SHORT);
			Assert.fail("Command-line contains no required value");
		} catch(TSClientConfigurationException e) {
			//
		}
						
		String[] args = new String[]{"-testPlan", "plan123"};
		Assert.assertEquals("The name of the testplan must be plan123", "plan123", client.extractStringValue(client.parseCommandline(options, args), TSClient.CMD_OPT_TESTPLAN, TSClient.CMD_OPT_TESTPLAN_SHORT));
		args = new String[]{"-tp", "plan123"};
		Assert.assertEquals("The name of the testplan must be plan123", "plan123", client.extractStringValue(client.parseCommandline(options, args), TSClient.CMD_OPT_TESTPLAN, TSClient.CMD_OPT_TESTPLAN_SHORT));
		
		args = new String[]{"-addPropsFile", "file123"};
		Assert.assertEquals("The name of the file must be file123", "file123", client.extractStringValue(client.parseCommandline(options, args), TSClient.CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE, TSClient.CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE_SHORT));
		args = new String[]{"-apfile", "file123"};
		Assert.assertEquals("The name of the file must be file123", "file123", client.extractStringValue(client.parseCommandline(options, args), TSClient.CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE, TSClient.CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE_SHORT));
		
	}

	@Test
	public void testExtractHosts() throws TSClientConfigurationException, ParseException {
		TSClient client = new TSClient();		
		Options options = client.getTSClientCommandLineOptions();
				
		try {
			client.extractStringList(client.parseCommandline(options, null), TSClient.CMD_OPT_PTEST_SERVER_HOSTS, TSClient.CMD_OPT_PTEST_SERVER_HOSTS_SHORT);
			Assert.fail("Invalid command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractStringList(client.parseCommandline(options, new String[0]), TSClient.CMD_OPT_PTEST_SERVER_HOSTS, TSClient.CMD_OPT_PTEST_SERVER_HOSTS_SHORT);
			Assert.fail("Empty command-line");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractStringList(client.parseCommandline(options, new String[]{"no", "valid", "value"}), TSClient.CMD_OPT_PTEST_SERVER_HOSTS, TSClient.CMD_OPT_PTEST_SERVER_HOSTS_SHORT);
			Assert.fail("Command-line contains no required value");
		} catch(TSClientConfigurationException e) {
			//
		}
						
		String[] args = new String[]{"-ptestHosts", "host1, host2,  host3"};
		String[] hosts = client.extractStringList(client.parseCommandline(options, args), TSClient.CMD_OPT_PTEST_SERVER_HOSTS, TSClient.CMD_OPT_PTEST_SERVER_HOSTS_SHORT);
		Assert.assertNotNull("The result must not be null", hosts);
		Assert.assertEquals("The result size must be 3", 3, hosts.length);
		Assert.assertTrue("The host must contained in list", Arrays.asList(hosts).contains("host1"));
		Assert.assertTrue("The host must contained in list", Arrays.asList(hosts).contains("host2"));
		Assert.assertTrue("The host must contained in list", Arrays.asList(hosts).contains("host3"));
		
		args = new String[]{"-h", "host1, host2,  host3"};
		hosts = client.extractStringList(client.parseCommandline(options, args), TSClient.CMD_OPT_PTEST_SERVER_HOSTS, TSClient.CMD_OPT_PTEST_SERVER_HOSTS_SHORT);
		Assert.assertNotNull("The result must not be null", hosts);
		Assert.assertEquals("The result size must be 3", 3, hosts.length);
		Assert.assertTrue("The host must contained in list", Arrays.asList(hosts).contains("host1"));
		Assert.assertTrue("The host must contained in list", Arrays.asList(hosts).contains("host2"));
		Assert.assertTrue("The host must contained in list", Arrays.asList(hosts).contains("host3"));		
	}

	@Test
	public void testExtractAdditionalProperties() throws TSClientConfigurationException, ParseException {

		TSClient client = new TSClient();		
		Options options = client.getTSClientCommandLineOptions();
		
		try {
			client.extractAdditionalProperties(null);
			Assert.fail("No such file");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractAdditionalProperties("");
			Assert.fail("No such file");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {
			client.extractAdditionalProperties(" ");
			Assert.fail("No such file");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {		
			client.extractAdditionalProperties("src/test/resource/noSuchFile.properties");
			Assert.fail("No such file");
		} catch(TSClientConfigurationException e) {
			//
		}
		
		try {		
			client.extractAdditionalProperties("src/test/resources/sampleTestPlan.xml");
		} catch(TSClientConfigurationException e) {
			e.printStackTrace();
		}
		
		Properties props = client.extractAdditionalProperties("src/test/resources/tsclient.properties");
		Assert.assertNotNull("The result must not be null", props);
		Assert.assertEquals("The size of the properties set must be 7", 7, props.size());
		Assert.assertEquals("The property value must be equal", "001", props.get("scenarioId"));
		Assert.assertEquals("The property value must be equal", "001", props.get("productId"));
		Assert.assertEquals("The property value must be equal", "0001", props.get("runId"));
		Assert.assertEquals("The property value must be equal", "12", props.get("waitTime"));
		Assert.assertEquals("The property value must be equal", "vhost0103", props.get("localhostName"));
		Assert.assertEquals("The property value must be equal", "TCO", props.get("measuringPointOutId"));
		Assert.assertEquals("The property value must be equal", "TCI", props.get("measuringPointInId"));
		
//		
//		
//		String[] args = new String[]{"-addPropsFile", "file123"};
//		Assert.assertEquals("The name of the file must be file123", "file123", client.extractStringValue(client.parseCommandline(options, args), TSClient.CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE, TSClient.CMD_OPT_PTEST_SERVER_ADDITIONAL_PROPERTIES_FILE_SHORT));
//		
	}


}
