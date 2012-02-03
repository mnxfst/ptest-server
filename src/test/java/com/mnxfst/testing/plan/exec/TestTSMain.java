package com.mnxfst.testing.plan.exec;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Test;

public class TestTSMain {

	
	@Test
	public void test() throws URISyntaxException, ClientProtocolException, IOException {
		
//		DefaultHttpClient client = new DefaultHttpClient();
//		HttpPost post = new HttpPost("/?test=123");
//		
//		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
//		nvp.add(new BasicNameValuePair("key1", "value1"));
//		post.setEntity(new UrlEncodedFormEntity(nvp));
//		
//		HttpHost h = new HttpHost("localhost", 9090);
//		InputStream s = client.execute(h, post).getEntity().getContent();
//		int c = -1;
//		while((c = s.read()) != -1)
//			System.out.print((char)c);
//		

	}
}
