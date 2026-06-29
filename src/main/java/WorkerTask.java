import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class WorkerTask implements Runnable {

    private static final long PARSER_DELAY_MS = Long.parseLong(
            System.getProperty("parser.delay.ms", "0")
    );

    private final BlockingQueue<Page> fetchedPages;
    private final Parser parser;
    private final Frontier frontier;
    private final Storage storage;

    public WorkerTask(
            BlockingQueue<Page> fetchedPages,
            Parser parser,
            Frontier frontier,
            Storage storage) {

        this.fetchedPages = fetchedPages;
        this.parser = parser;
        this.frontier = frontier;
        this.storage = storage;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Page page = fetchedPages.take();

                if (PARSER_DELAY_MS > 0) {
                    Thread.sleep(PARSER_DELAY_MS);
                }

                Document doc = Jsoup.parse(page.getContent(), page.getUrl());
                List<String> links = parser.extractLinks(doc);
                String text = parser.extractText(doc);

                storage.save(page.getUrl(), text);

                for(String link : links){
                    frontier.addTask(new CrawlTask(link, 0));
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
