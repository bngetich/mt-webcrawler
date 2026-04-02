import java.util.concurrent.*;

public class Crawler {

    private final ExecutorService executor;
    private final Frontier frontier;
    private final VisitedTracker visitedTracker;
    private final Fetcher fetcher;
    private final Parser parser;
    private final Storage storage;
    private final int threads;

    public Crawler(int threads,
                   Frontier frontier,
                   VisitedTracker visitedTracker) {
        this.executor = Executors.newFixedThreadPool(threads);
        this.frontier = frontier;
        this.visitedTracker = visitedTracker;
        this.fetcher = ComponentFactory.createFetcher();
        this.parser = ComponentFactory.createParser();
        this.storage = ComponentFactory.createStorage();
        this.threads = threads;

    }

    public void start() {
        for (int i = 0; i < threads; i++) {
            executor.submit(
                new WorkerTask(
                    frontier,
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