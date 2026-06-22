import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Crawler {
    private static final int MAX_RETRIES = 3;

    private final ExecutorService processExecutor;
    private final ExecutorService dispatcherExecutor;
    private final ScheduledExecutorService metricsExecutor;

    private final Frontier frontier;
    private final VisitedTracker visitedTracker;
    private final Fetcher fetcher;
    private final Parser parser;
    private final Storage storage;
    private final BlockingQueue<Page> fetchedPages;

    private final RobotsService robotsService;

    private final CrawlerMetrics metrics;

    private final int threads;

    public Crawler(int threads,
                   Frontier frontier,
                   VisitedTracker visitedTracker,
                   RobotsService robotsService
    ) {
        this.processExecutor = Executors.newFixedThreadPool(threads);
        this.dispatcherExecutor = Executors.newFixedThreadPool(2);
        this.metricsExecutor = Executors.newSingleThreadScheduledExecutor();

        this.frontier = frontier;
        this.visitedTracker = visitedTracker;
        this.robotsService = robotsService;
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

                    if(!robotsService.isAllowed(url)) {
                        continue;
                    }

                    fetcher.fetch(url)
                            .thenAccept(page -> {
                                if (!visitedTracker.markVisited(url)) {
                                    return;
                                }

                                fetchedPages.offer(page);
                                metrics.incrementPagesFetched();

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

        metricsExecutor.scheduleAtFixedRate(
                this::reportMetrics,
                30,
                30,
                TimeUnit.SECONDS
        );
    }


    public void stop() {
        metricsExecutor.shutdownNow();
        dispatcherExecutor.shutdownNow();
        processExecutor.shutdownNow();
    }

    private void reportMetrics() {
        try {
            System.out.println(
                    "\n=== CRAWLER METRICS ==="
            );

            System.out.println(
                    "Pages fetched: " +
                            metrics.getPagesFetched()
            );

            System.out.println(
                    "Fetch failures: " +
                            metrics.getFetchFailures()
            );

            System.out.println(
                    "Retries scheduled: " +
                            metrics.getRetriesScheduled()
            );

            System.out.println(
                    "Dead-lettered: " +
                            metrics.getDeadLettered()
            );

            System.out.println(
                    "Frontier size: " +
                            frontier.size()
            );

            System.out.println(
                    "=======================\n"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFetchFailure(CrawlTask task) {
        if (!(frontier instanceof PartitionedRedisFrontier redisFrontier)) {
            return;
        }

        metrics.incrementFetchFailures();

        if (task.getRetryCount() >= MAX_RETRIES) {
            redisFrontier.addToDlq(task);
            metrics.incrementDeadLettered();
        } else {
            redisFrontier.addRetry(
                    new CrawlTask(
                            task.getUrl(),
                            task.getRetryCount() + 1
                    )
            );

            metrics.incrementRetriesScheduled();
        }
    }
}
