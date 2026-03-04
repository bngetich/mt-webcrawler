import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class JsoupFetcher implements Fetcher {

    @Override
    public Document fetch(String url){
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            return null;
        }
    }
}
