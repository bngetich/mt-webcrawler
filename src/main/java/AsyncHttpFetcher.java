import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

public class AsyncHttpFetcher implements Fetcher {

    private final HttpClient client = HttpClient.newHttpClient();

    private final Semaphore inflight = new Semaphore(20);

    @Override
    public CompletableFuture<Page> fetch(String url) {

        try {
            inflight.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(null);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> new Page(url, resp.body()))
                .exceptionally(ex -> null)
                .whenComplete((r, t) -> inflight.release());

    }
}