import java.util.concurrent.atomic.AtomicLong;

public class CrawlerMetrics {

    private final AtomicLong pagesFetched = new AtomicLong();
    private final AtomicLong pagesFailed = new AtomicLong();
    private final AtomicLong retriesScheduled = new AtomicLong();
    private final AtomicLong dlqCount = new AtomicLong();

    public void incrementFetched() {
        pagesFetched.incrementAndGet();
    }

    public void incrementFailed() {
        pagesFailed.incrementAndGet();
    }

    public void incrementRetries() {
        retriesScheduled.incrementAndGet();
    }

    public void incrementDlq() {
        dlqCount.incrementAndGet();
    }

    public long getPagesFetched() {
        return pagesFetched.get();
    }

    public long getPagesFailed() {
        return pagesFailed.get();
    }

    public long getRetriesScheduled() {
        return retriesScheduled.get();
    }

    public long getDlqCount() {
        return dlqCount.get();
    }
}