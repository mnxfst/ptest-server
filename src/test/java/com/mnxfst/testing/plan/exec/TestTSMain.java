package com.mnxfst.testing.plan.exec;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Test;

public class TestTSMain {

	
	@Test
	public void test() throws URISyntaxException, ClientProtocolException, IOException {
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost();
		post.setURI(new URI("/?test=121"));
		HttpParams p = new BasicHttpParams();
		p.setParameter("test", "<test></test>");
		HttpHost host = new HttpHost("localhost", 9090);
		post.setParams(p);
		hier gehts nicht weiter
		InputStream s = client.execute(host, post).getEntity().getContent();
		int c = -1;
		while((c = s.read()) != -1)
			System.out.print((char)c);
		

	}
}
