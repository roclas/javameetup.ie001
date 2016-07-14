package com.carlos.threads; /**
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

import static java.lang.Thread.sleep;

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


    private static Callable<String> createTask(String url){
        return (() -> { return sendGet(url); });
    }


    public static void main(String[] args) throws Exception{

        // 1a - build the task;
        final CompletableFuture<String> retrieveName = CompletableFuture.supplyAsync(() -> {
            try { sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Promise";
        });

        // 1b - define task result processing
        retrieveName.thenAccept(it -> System.out.println("task finished "+it));



        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Tread " + threadName+ " blocked");
            try {
                System.out.println("Tread " + threadName+ " finished ->"+retrieveName.get());
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        };
        //task.run();
        Thread th1= new Thread(task);
        Thread th2= new Thread(task);
        Thread th3= new Thread(task);
        th1.start();
        th2.start();
        th3.start();


        System.out.println("waiting...");
        Thread.sleep(4000);
        System.out.println("finished waiting!");

       /*
        // 2 - start the task
        startThread(() -> {
        */
       /*
        });
        */










        final Comparator<Future> comp = (f1, f2) -> {
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
        }).map(x -> executor.submit(createTask(x)));


        Future f1= executor.submit(createTask("GBP"));
        //CompletableFuture completableFuture = CompletableFuture.supplyAsync( ( ) -> { return "100"; } );
        //Future f1= executor.submit(createTask("GBP"));
        //String s1 = f1.get().toString();
        //cf1.isDone();

        Stream<CompletableFuture<String>> completableFutureStream = Arrays.stream(new String[]{
                "EUR", "GBP", "CAD", "AUD", "SGD"
        }).map(x -> CompletableFuture.supplyAsync(() -> {
            String response = sendGet(x);

            return response;
        })).parallel();


        //Optional<String> min = Arrays.stream(new String[]{"the GBP costs 1.1928186490666122", "the GBP costs 1.1928186490666122"}).min(comp);
        Future<String> min = futuresStream.parallel().min(comp).get();
        System.out.println("the minimum change against the dollar is: "+min.get());


        System.out.println("Done!!");
        executor.shutdown();
    }

}
