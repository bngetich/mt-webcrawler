import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Crawler {

    private final ExecutorService processExecutor;
    private final ExecutorService dispatcherExecutor;

    private final Frontier frontier;
    private final VisitedTracker visitedTracker;
    private final Fetcher fetcher;
    private final Parser parser;
    private final Storage storage;
    private final BlockingQueue<Page> fetchedPages;

    private final int threads;

    public Crawler(int threads,
                   Frontier frontier,
                   VisitedTracker visitedTracker) {
        this.processExecutor = Executors.newFixedThreadPool(threads);
        this.dispatcherExecutor = Executors.newSingleThreadExecutor();

        this.frontier = frontier;
        this.visitedTracker = visitedTracker;
        this.fetcher = ComponentFactory.createFetcher();
        this.parser = ComponentFactory.createParser();
        this.storage = ComponentFactory.createStorage();
        this.fetchedPages = new LinkedBlockingQueue<>();
        this.threads = threads;
    }

    public void start() {
        // CPU workers
        for (int i = 0; i < threads; i++) {
            processExecutor.submit(
                    new WorkerTask(
                            fetchedPages,
                            parser,
                            frontier,
                            storage
                    )
            );
        }

        // Fetch dispatcher (I/O side)
        dispatcherExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String url = frontier.getNextUrl();

                    if (url == null || url.isBlank()) {
                        continue;
                    }

                    fetcher.fetch(url)
                            .thenAccept(page -> {
                                if (page == null) {
                                    return;
                                }

                                if (!visitedTracker.markVisited(url)) {
                                    return;
                                }

                                fetchedPages.offer(page);
                            })
                            .exceptionally(ex -> {
                                ex.printStackTrace();

                                if (frontier instanceof PartitionedRedisFrontier redisFrontier) {
                                    redisFrontier.addRetry(url, 1);
                                }
                                return null;
                            });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        dispatcherExecutor.shutdownNow();
        processExecutor.shutdownNow();
    }
}