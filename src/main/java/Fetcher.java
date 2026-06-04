import java.util.concurrent.CompletableFuture;

public interface Fetcher {

    public CompletableFuture<Page> fetch(String url);
}
