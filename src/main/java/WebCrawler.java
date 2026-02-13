import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WebCrawler implements Runnable {
    private static final int MAX_DEPTH = 3;
    private Thread thread;
    private String first_link;
    private Set<String> visitedLinks = new HashSet<>();
    private int ID;

    public WebCrawler(String link, int num) {
        System.out.println("Webcrawler created");
        first_link = link;
        ID = num;

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        crawl(1, first_link);
    }

    private void crawl(int level, String url){
        if(level <= MAX_DEPTH) {
            Document doc = request(url);

            if(doc != null){
                for(Element link : doc.select("a[href]")){
                    String next_link = link.absUrl("href");

                    if(!visitedLinks.contains(next_link)){
                        crawl(level + 1, next_link);
                    }
                }
            }
        }
    }

    private Document request(String url){
        try {
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            if(con.response().statusCode() == 200){
                System.out.println("\n**Bot ID:" + ID + " Received Webpage at " + url);

                String title = doc.title();
                System.out.println(title);
                visitedLinks.add(url);

                return doc;
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    public Thread getThread() {
        return thread;
    }
}
