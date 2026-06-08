import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InMemoryFrontier implements  Frontier{
    private final BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();

    @Override
    public void addTask(CrawlTask task) {
        queue.offer(task);
    }

    @Override
    public CrawlTask getNextTask() throws InterruptedException {
        return queue.take();
    }

    @Override
    public long size() {
        return queue.size();
    }
}
