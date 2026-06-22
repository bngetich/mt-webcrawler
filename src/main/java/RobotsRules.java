import java.util.List;

public class RobotsRules {
    private final String host;
    private final List<String> disallowedPaths;
    private final Long crawlDelayMs;
    private final long fetchedAtMillis;

    public RobotsRules(String host,
                       List<String> disallowedPaths,
                       Long crawlDelayMs,
                       long fetchedAtMillis) {
        this.host = host;
        this.disallowedPaths = List.copyOf(disallowedPaths);
        this.crawlDelayMs = crawlDelayMs;
        this.fetchedAtMillis = fetchedAtMillis;
    }

    public boolean isAllowed(String path) {
        for (String disallowedPath : disallowedPaths) {
            if (path.startsWith(disallowedPath)) {
                return false;
            }
        }

        return true;
    }

    public long getCrawlDelayMsOrDefault(long defaultDelayMs) {
        if (crawlDelayMs == null) {
            return defaultDelayMs;
        }

        return crawlDelayMs;
    }

    public String getHost() {
        return host;
    }

    public List<String> getDisallowedPaths() {
        return disallowedPaths;
    }

    public Long getCrawlDelayMs() {
        return crawlDelayMs;
    }

    public long getFetchedAtMillis() {
        return fetchedAtMillis;
    }
}