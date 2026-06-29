import java.util.concurrent.atomic.AtomicLong;

public class CrawlerMetrics {

    private final AtomicLong pagesFetched = new AtomicLong();
    private final AtomicLong fetchFailures = new AtomicLong();
    private final AtomicLong retriesScheduled = new AtomicLong();
    private final AtomicLong deadLettered = new AtomicLong();
    private final AtomicLong robotsBlocked = new AtomicLong();

    public void incrementPagesFetched() {
        pagesFetched.incrementAndGet();
    }

    public void incrementFetchFailures() {
        fetchFailures.incrementAndGet();
    }

    public void incrementRetriesScheduled() {
        retriesScheduled.incrementAndGet();
    }

    public void incrementDeadLettered() {
        deadLettered.incrementAndGet();
    }
    public void incrementRobotsBlocked() {
        robotsBlocked.incrementAndGet();
    }

    public long getPagesFetched() {
        return pagesFetched.get();
    }

    public long getFetchFailures() {
        return fetchFailures.get();
    }

    public long getRetriesScheduled() {
        return retriesScheduled.get();
    }

    public long getDeadLettered() {
        return deadLettered.get();
    }
    public long getRobotsBlocked() {
        return robotsBlocked.get();
    }

    public String toPrometheusFormat(long frontierSize, long fetchedPagesQueueSize) {
        return ""
                + "# TYPE crawler_pages_fetched_total counter\n"
                + "crawler_pages_fetched_total " + getPagesFetched() + "\n"
                + "# TYPE crawler_robots_blocked_total counter\n"
                + "crawler_robots_blocked_total " + getRobotsBlocked() + "\n"
                + "# TYPE crawler_fetch_failures_total counter\n"
                + "crawler_fetch_failures_total " + getFetchFailures() + "\n"
                + "# TYPE crawler_retries_scheduled_total counter\n"
                + "crawler_retries_scheduled_total " + getRetriesScheduled() + "\n"
                + "# TYPE crawler_dead_lettered_total counter\n"
                + "crawler_dead_lettered_total " + getDeadLettered() + "\n"
                + "# TYPE crawler_frontier_size gauge\n"
                + "crawler_frontier_size " + frontierSize + "\n"
                + "# TYPE crawler_fetched_pages_queue_size gauge\n"
                + "crawler_fetched_pages_queue_size " + fetchedPagesQueueSize + "\n";
    }
}
