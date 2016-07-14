/**
 * Created by carlos on 09/07/16.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.concurrent.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class FutureTaskExample {

    private static final String USER_AGENT = "Mozilla/5.0";
    public static String sendGet(String url) {
        try {
            URL obj = new URL("http://localhost:8001/"+url);
            System.out.println("requesting http://localhost:8001/"+url);
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
            System.out.println(response);
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception{

        final Comparator<Future> comp = (f1, f2) -> {
	    System.out.println("comparing "+f1+" and "+f2);
            try {
                return Float.compare(
                        Float.parseFloat(f1.get().toString().split(" ")[3]), Float.parseFloat(f2.get().toString().split(" ")[3])
                );
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(5);

        Stream<Future<String>> futuresStream = Arrays.stream(new String[]{
                "EUR", "GBP", "CAD", "AUD", "SGD"
        }).map(x -> executor.submit(() -> { return sendGet(x); }));

	//terminal operations (such as average , sum , min , max , and count ) 
        Future<String> min = futuresStream.parallel().min(comp).get();
        System.out.println("\n\n\nthe minimum change against the dollar is: "+min.get());

        System.out.println("Done!!");
        executor.shutdown();
    }
}
