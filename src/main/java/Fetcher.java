import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Fetcher {

    public Document fetch(String url){
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            return null;
        }
    }
}
