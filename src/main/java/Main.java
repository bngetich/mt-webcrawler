import java.util.Set;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {

        // Shared frontier queue (core crawler concept)
        BlockingQueue<String> frontier =
                new LinkedBlockingQueue<>();

        // Thread-safe visited set
        Set<String> visited =
                ConcurrentHashMap.newKeySet();

        // Seed URLs
        frontier.offer("https://abcnews.go.com");
        frontier.offer("https://npr.org");
        frontier.offer("https://nytimes.com");

        int workers = 4;

        ExecutorService pool =
                Executors.newFixedThreadPool(workers);

        // start workers
        for (int i = 0; i < workers; i++) {
            pool.submit(new WebCrawler(frontier, visited, i));
        }

        // demo run time (stop after 60s)
        try {
            Thread.sleep(60000);
        } catch (InterruptedException ignored) {}

        pool.shutdownNow();

        try {
            boolean stopped = pool.awaitTermination(10, TimeUnit.SECONDS);
            if (!stopped){
                System.out.println("Workers did not stop in time.");
            }
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

        System.out.println("\nCrawling finished.");
        System.out.println("Visited pages: " + visited.size());
    }
}