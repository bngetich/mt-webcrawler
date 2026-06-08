import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Crawler {
    private static final int MAX_RETRIES = 3;

    private final ExecutorService processExecutor;
    private final ExecutorService dispatcherExecutor;

    private final Frontier frontier;
    private final VisitedTracker visitedTracker;
    private final Fetcher fetcher;
    private final Parser parser;
    private final Storage storage;
    private final BlockingQueue<Page> fetchedPages;

    private final CrawlerMetrics metrics;

    private final int threads;

    public Crawler(int threads,
                   Frontier frontier,
                   VisitedTracker visitedTracker) {
        this.processExecutor = Executors.newFixedThreadPool(threads);
        this.dispatcherExecutor = Executors.newFixedThreadPool(3);

        this.frontier = frontier;
        this.visitedTracker = visitedTracker;
        this.fetcher = ComponentFactory.createFetcher();
        this.parser = ComponentFactory.createParser();
        this.storage = ComponentFactory.createStorage();
        this.fetchedPages = new LinkedBlockingQueue<>();
        this.metrics = new CrawlerMetrics();
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
                    CrawlTask task = frontier.getNextTask();
                    String url = task.getUrl();

                    if (url == null || url.isBlank()) {
                        continue;
                    }

                    fetcher.fetch(url)
                            .thenAccept(page -> {
                                if (page == null) {
                                    handleFetchFailure(task);
                                    return;
                                }

                                if (!visitedTracker.markVisited(url)) {
                                    return;
                                }

                                fetchedPages.offer(page);
                                metrics.incrementFetched();

                            })
                            .exceptionally(ex -> {
                                ex.printStackTrace();
                                handleFetchFailure(task);
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

        // Retry dispatcher
        dispatcherExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if(frontier instanceof PartitionedRedisFrontier redisFrontier) {
                        CrawlTask retry = redisFrontier.getReadyRetry();

                        if(retry != null){
                            frontier.addTask(retry);
                        }
                    }

                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Metrics reporter
        /* Metrics reporting is not dispatching work.
         * Consider using a ScheduledExecutorService for periodic reporting.
         */
        dispatcherExecutor.submit(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    Thread.sleep(30000);

                    System.out.println(
                            "\n=== CRAWLER METRICS ==="
                    );

                    System.out.println(
                            "Fetched: " +
                                    metrics.getPagesFetched()
                    );

                    System.out.println(
                            "Failed: " +
                                    metrics.getPagesFailed()
                    );

                    System.out.println(
                            "Retries: " +
                                    metrics.getRetriesScheduled()
                    );

                    System.out.println(
                            "DLQ: " +
                                    metrics.getDlqCount()
                    );

                    System.out.println(
                            "=======================\n"
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }


    public void stop() {
        dispatcherExecutor.shutdownNow();
        processExecutor.shutdownNow();
    }

    private void handleFetchFailure(CrawlTask task) {
        if (!(frontier instanceof PartitionedRedisFrontier redisFrontier)) {
            return;
        }

        metrics.incrementFailed();

        if (task.getRetryCount() >= MAX_RETRIES) {
            redisFrontier.addToDlq(task);
            metrics.incrementDlq();
        } else {
            redisFrontier.addRetry(
                    new CrawlTask(
                            task.getUrl(),
                            task.getRetryCount() + 1
                    )
            );

            metrics.incrementRetries();
        }
    }
}
