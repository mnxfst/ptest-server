package com.mnxfst.testing.plan.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.mnxfst.testing.exception.TSPlanConfigurationFormatException;

/**
 * Test case for {@link TSPlanConfigOptionsBuilder}
 * @author mnxfst
 * @since 20.12.2011
 */
public class TestTSPlanConfigOptionsBuilder {

	@Test
	public void testParseSingleOption() throws TSPlanConfigurationFormatException, SAXException, IOException, ParserConfigurationException {
		TSPlanConfigOptionsBuilder parser = new TSPlanConfigOptionsBuilder();
		
		try {
			parser.parseConfigurationNode(null, null);
			Assert.fail("Null input must lead to exception");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}

		ByteArrayInputStream bin = new ByteArrayInputStream(new String("<configuration><url>localhost</url>"+
				"<port>9160</port><user>testUser</user><password>{SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd</password>"+
				"<keyspace>stats</keyspace></configuration>").getBytes());
		Document configDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			parser.parseConfigurationNode(configDoc.getFirstChild(), null);
			Assert.fail("No name attribute contained");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		parser.parseConfigurationNode(configDoc.getFirstChild(), "default");

		bin = new ByteArrayInputStream(new String("<configuration name=\"\"><url>localhost</url>"+
				"<port>9160</port><user>testUser</user><password>{SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd</password>"+
				"<keyspace>stats</keyspace></configuration>").getBytes());
		configDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			parser.parseConfigurationNode(configDoc.getFirstChild(), null);
			Assert.fail("No name attribute contained");
		} catch(TSPlanConfigurationFormatException e) {		
		}
		try {
			parser.parseConfigurationNode(configDoc.getFirstChild(), "default");
			Assert.fail("Default provided but the xml contains a name attribute having an empty value");
		} catch(TSPlanConfigurationFormatException e) {
				
		}

		bin = new ByteArrayInputStream(new String("<configuration name=\"configurationStats\"></configuration>").getBytes());
		configDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			parser.parseConfigurationNode(configDoc.getFirstChild(), null);
			Assert.fail("No child nodes contained");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}

		bin = new ByteArrayInputStream(new String("<configuration name=\"configurationStats\"><url>localhost</url>"+
				"<port>9160</port><user>testUser</user><password>{SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd</password>"+
				"<keyspace>stats</keyspace><empty/></configuration>").getBytes());
		configDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		TSPlanConfigOption singleOption = parser.parseConfigurationNode(configDoc.getFirstChild(), null);
		
		Assert.assertNotNull("The result must not be null", singleOption);
		Assert.assertEquals("The options must be named 'configurationStats'", "configurationStats", singleOption.getName());
		Assert.assertEquals("The url must be localhost", "localhost", singleOption.getOption("url"));
		Assert.assertEquals("The port must be 9160", "9160", singleOption.getOption("port"));
		Assert.assertEquals("The user must be testUser", "testUser", singleOption.getOption("user"));	
		Assert.assertEquals("The password must be {SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd", "{SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd", singleOption.getOption("password"));
		Assert.assertEquals("The keyspace must be stats", "stats", singleOption.getOption("keyspace"));
		Assert.assertNull("The empty attribute value must be null", singleOption.getOption("empty"));
		Assert.assertEquals("The size of the options set must be 6", 6, singleOption.getOptions().size());
	}
	
	@Test
	public void testParse() throws SAXException, IOException, ParserConfigurationException, TSPlanConfigurationFormatException {

		TSPlanConfigOptionsBuilder parser = new TSPlanConfigOptionsBuilder();		
		
		ByteArrayInputStream bin = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test></test>".getBytes());
		Document xmlHeaderOnly = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		try {
			parser.parseConfigurationNode(xmlHeaderOnly, null);
			Assert.fail("Unexpected root node");
		} catch(TSPlanConfigurationFormatException e) {
			//
		}
		
		bin = new ByteArrayInputStream(new String("<configuration><configuration name=\"configurationStats\"><url>localhost</url>"+
										"<port>9160</port><user>testUser</user><password>{SHA-1}8bb6118f8fd6935ad0876a3be34a717d32708ffd</password>"+
										"<keyspace>stats</keyspace></configuration><mail name=\"sampleMail\"><address>test@home.com</address></mail></configuration>").getBytes());
		Document configDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bin);
		
//		TODO
//		Map<String, TSPlanConfigOption> options = parser.parseConfigurationNode(configDoc.getFirstChild(), null);		
//		Assert.assertNotNull("The result map must not be null", options);
//		Assert.assertEquals("The result map must contain 2 elements", 2, options.size());
//		
//		Assert.assertNotNull("The options must contain an element for configurationStats", options.get("configurationStats"));
//		Assert.assertNotNull("The options must contain an element for sampleMail", options.get("sampleMail"));
//		Assert.assertNull("The options must not contain an element for emptyOptions", options.get("emptyOptions"));
	}
	
}
