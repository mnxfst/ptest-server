package com.mnxfst.testing.plan.exec.client;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

public class TSSaturationTestClient {

	public static void main(String[] args) throws ClientProtocolException, IOException, SAXException, ParserConfigurationException, ParseException {
		
		Options options = new Options();
		options.addOption("execute", false, "Executes a test plan on the ptest-server instance(s)");
		options.addOption("collect", false, "Collects the results from the ptest-server instance(s)");
		options.addOption("t", "threads", true, "Number of threads used for executing the test case");
		options.addOption("r", "recurrences", true, "Number of tesplan recurrences");
		options.addOption("rt", "recurrenceType", true, "Recurrence type (TIMES, MILLIS, SECONDS, MINUTES, HOURS, DAYS)");
		options.addOption("p", "testPlan", true, "Names the test plan to execute");
		options.addOption("ri", true, "Response identifier used by the ptest-server to store results");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		if(cmd.hasOption("execute")) {
			System.out.println("Execute");
			// TODO fetch all values and send them to the server
		} else if(cmd.hasOption("collect")) {
			System.out.println("Collect");
		} else {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( TSSaturationTestClient.class.getName(), options );
		}
		
		/*
		
		
				
		HttpGet getMethod = new HttpGet("/?execute=1&threads=4&recurrences=1&recurrencetype=times&testplan=AddressIntTestPlan.xml&scenarioId=sec1");
		HttpHost host = new HttpHost("localhost", 9090);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(host, getMethod);
		InputStream s = response.getEntity().getContent();

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(s);
		s.close();
		Node rootNode = doc.getFirstChild();
		System.out.println(rootNode.getNodeName());
		NodeList childs = rootNode.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++) {
			System.out.println(childs.item(i));
		}*/
		
	}
	
}
