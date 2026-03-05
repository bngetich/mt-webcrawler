import java.util.concurrent.*;

public class Crawler {

    private final ExecutorService executor;
    private final BlockingQueue<String> urlQueue;
    private final VisitedTracker visitedTracker;
    private final Fetcher fetcher;
    private final Parser parser;
    private final Storage storage;
    private final int threads;

    public Crawler(int threads, String seedUrl) {
        this.executor = Executors.newFixedThreadPool(threads);
        this.urlQueue = new LinkedBlockingQueue<>();
        this.visitedTracker = new VisitedTracker();
        this.fetcher = ComponentFactory.createFetcher();
        this.parser = ComponentFactory.createParser();
        this.storage = ComponentFactory.createStorage();
        this.threads = threads;

        urlQueue.offer(seedUrl);
    }

    public void start() {
        for (int i = 0; i < threads; i++) {
            executor.submit(
                new WorkerTask(
                    urlQueue,
                    visitedTracker,
                    fetcher,
                    parser,
                    storage
                )
            );
        }
    }

    public void stop() {
        executor.shutdownNow();
    }
}