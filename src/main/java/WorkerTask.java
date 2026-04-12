import org.jsoup.nodes.Document;
import java.util.List;

public class WorkerTask implements Runnable {

    private final Frontier frontier;
    private final VisitedTracker visited;
    private final Fetcher fetcher;
    private final Parser parser;
    private final Storage storage;

    public WorkerTask(
            Frontier frontier,
            VisitedTracker visited,
            Fetcher fetcher,
            Parser parser,
            Storage storage) {

        this.frontier = frontier;
        this.visited = visited;
        this.fetcher = fetcher;
        this.parser = parser;
        this.storage = storage;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String url = frontier.getNextUrl();  // blocks until a url exists OR interrupt happens

                if (!visited.markVisited(url)) {
                    continue;
                }

                Document doc = fetcher.fetch(url);
                if (doc == null) continue;

                System.out.println(
                        "Crawler@" + System.identityHashCode(this) +
                                " Thread=" + Thread.currentThread().getName() +
                                " URL=" + url
                );

                List<String> links = parser.extractLinks(doc);
                String text = parser.extractText(doc);

                storage.save(url, text);

                for (String link : links) {
                    frontier.addUrl(link);
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