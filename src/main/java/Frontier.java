public interface Frontier {
    void addTask(CrawlTask task);

    CrawlTask getNextTask() throws InterruptedException;
}
