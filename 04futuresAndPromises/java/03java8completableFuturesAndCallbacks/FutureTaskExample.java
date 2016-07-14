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

import static java.lang.Thread.sleep;

public class FutureTaskExample {

    public static void main(String[] args) throws Exception{

        final CompletableFuture<String> retrieveName = CompletableFuture.supplyAsync(() -> {
            try { 
		sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Promise finally returns something! "+(new java.util.Random()).nextInt();
        });
	//callback, we don't stop here, we continue
        retrieveName.thenAccept(result -> System.out.println("task finished "+result));


        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Tread " + threadName+ " blocked waiting for the completableFuture");
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
        System.out.println("main thread blocked...");
        //Thread.sleep(4000);
        System.out.println("Done!!"+retrieveName.get());
    }

}
