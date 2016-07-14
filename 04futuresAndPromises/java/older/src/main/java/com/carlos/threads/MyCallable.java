/**
 * Created by carlos on 09/07/16.
 */
package com.carlos.threads;

import java.util.concurrent.Callable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;


public class MyCallable implements Callable<String> {

        private static final String USER_AGENT = "Mozilla/5.0";
        public static String sendGet(String url) throws Exception {
            URL obj = new URL(url);
		    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		    con.setRequestMethod("GET");
		    con.setRequestProperty("User-Agent", USER_AGENT);
		    int responseCode = con.getResponseCode();
		    BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		    String inputLine;
		    StringBuffer response = new StringBuffer();
		    while ((inputLine = in.readLine()) != null) {
			    response.append(inputLine);
		    }
		    in.close();
            return response.toString();
	}


	private String url;
	public MyCallable(String url){ this.url=url; }
	@Override
	public String call() throws Exception {
        String response = sendGet(url);
        return Thread.currentThread().getName();
	}
}