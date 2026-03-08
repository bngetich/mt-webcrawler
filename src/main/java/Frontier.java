public interface Frontier {
    void addUrl(String url);

    String getNextUrl() throws InterruptedException;
}
