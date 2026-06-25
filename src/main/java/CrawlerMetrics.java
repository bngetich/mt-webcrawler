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
}
