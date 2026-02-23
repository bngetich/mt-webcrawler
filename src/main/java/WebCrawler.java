import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class WebCrawler implements Runnable {
    private final BlockingQueue<String> frontier;
    private final Set<String> visited;
    private int workerId;

    private static final int MAX_DEPTH = 3;

    public WebCrawler(
            BlockingQueue<String> frontier,
            Set<String> visited,
            int workderId) {

        this.frontier = frontier;
        this.visited = visited;
        this.workerId = workderId;
    }

    @Override
    public void run() {

        while (true) {
            try {
                // take() blocks until work exists
                String url = frontier.take();

                // dedupe (thread safe set)
                if(!visited.add(url)){
                    continue;
                }

                process(url);

            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }


    }

    private void process(String url) {

        Document doc = request(url);

        if (doc == null) return;

        for (Element link : doc.select("a[href]")) {

            String nextLink = link.absUrl("href");

            if (!nextLink.isEmpty() && !visited.contains(nextLink)) {
                frontier.offer(nextLink); // PRODUCER step
            }
        }
    }

    private Document request(String url) {
        try {
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            if (con.response().statusCode() == 200) {

                System.out.println(
                        "\nWorker " + workerId +
                                " fetched: " + url);

                System.out.println("Title: " + doc.title());

                return doc;
            }

        } catch (IOException e) {
            return null;
        }

        return null;
    }
}
