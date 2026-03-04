import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public interface Fetcher {

    public Document fetch(String url) throws Exception;
}
