/**
 * Created by carlos on 09/07/16.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;
import java.util.Arrays;

public class FutureTaskExample {

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

    private static Callable<String> createTask(String url){
            return (() -> {
                try {
                    return sendGet("http://localhost:8001/"+url);
                } catch (Exception e) {
                    return null;
                }
            });
    }


    public static void main(String[] args) throws Exception{

        ExecutorService executor = Executors.newFixedThreadPool(5);

        Future<String>[] myFutureArray= new Future[]{
                executor.submit(createTask("EUR")),
                executor.submit(createTask("GBP")),
                executor.submit(createTask("CAD")),
                executor.submit(createTask("AUD")),
                executor.submit(createTask("SGD"))
        };
        for (Future<String> f : myFutureArray) {
            System.out.println("submited task "+f);
        }

        Float min=999.0f;
        String finalresult="";
        for (Future<String> f : myFutureArray) {
            String result = f.get();
            System.out.println("future done! :" +result);
	    Float candidate=Float.parseFloat(result.split(" ")[3]);
            if(candidate<min){
            	min=candidate;
            	finalresult=result;
            }
        }
	System.out.println("\n\n\nthe minimum exchange for the dollar is: "+finalresult);
        System.out.println("Done!!");
	executor.shutdown();
	}
}
