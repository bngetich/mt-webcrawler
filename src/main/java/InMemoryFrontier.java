import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InMemoryFrontier implements  Frontier{
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    @Override
    public void addUrl(String url) {
        queue.offer(url);
    }

    @Override
    public String getNextUrl() throws InterruptedException {
        return queue.take();
    }
}
