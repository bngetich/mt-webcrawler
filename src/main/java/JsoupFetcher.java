import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class JsoupFetcher implements Fetcher {

    @Override
    public CompletableFuture<Page> fetch(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String html = Jsoup.connect(url).get().html();
                return new Page(url, html);
            } catch (IOException e) {
                return null;
            }
        });
    }
}