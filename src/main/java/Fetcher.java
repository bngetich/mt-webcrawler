import org.jsoup.nodes.Document;

public interface Fetcher {

    public Document fetch(String url) throws Exception;
}
