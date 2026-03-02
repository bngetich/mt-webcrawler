import org.jsoup.nodes.Document;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class WorkerTask implements Runnable {

    private final BlockingQueue<String> queue;
    private final VisitedTracker visited;
    private final Fetcher fetcher;
    private final Parser parser;
    private final Storage storage;

    public WorkerTask(
            BlockingQueue<String> queue,
            VisitedTracker visited,
            Fetcher fetcher,
            Parser parser,
            Storage storage) {

        this.queue = queue;
        this.visited = visited;
        this.fetcher = fetcher;
        this.parser = parser;
        this.storage = storage;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String url = queue.take();  // blocks until a url exists OR interrupt happens

                if (!visited.markVisited(url)) {
                    continue;
                }

                Document doc = fetcher.fetch(url);
                if (doc == null) continue;

                List<String> links = parser.extractLinks(doc);
                String text = parser.extractText(doc);

                storage.save(url, text);

                for (String link : links) {
                    queue.offer(link);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore flag
                break; // IMPORTANT: exit the worker
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}