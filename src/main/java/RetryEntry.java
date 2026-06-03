public class RetryEntry {

    private final String url;
    private final int retryCount;

    public RetryEntry(
            String url,
            int retryCount) {

        this.url = url;
        this.retryCount = retryCount;
    }

    public String getUrl() {
        return url;
    }

    public int getRetryCount() {
        return retryCount;
    }
}