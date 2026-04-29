import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HostBasedFrontier implements Frontier {

    private final Map<String, Queue<String>> hostQueues = new ConcurrentHashMap<>();
    private final Map<String, Long>  lastAccess = new ConcurrentHashMap<>();

    private final long delayMs = 1000;

    @Override
    public void addUrl(String url){
        String host = getHost(url);

        if(host == null) return;

        hostQueues
                .computeIfAbsent(host, h -> new ConcurrentLinkedQueue<>())
                .offer(url);
    }

    @Override
    public String getNextUrl() throws InterruptedException {
        while (true) {
            for(Map.Entry<String, Queue<String>> entry : hostQueues.entrySet()){

                String host = entry.getKey();
                Queue<String> queue = entry.getValue();

                long now = System.currentTimeMillis();
                Long last = lastAccess.get(host);

                if(last != null && now - last < delayMs) {
                    continue;
                }

                String url = queue.poll();

                if(url != null){
                    lastAccess.put(host, now);
                    return url;
                }
            }

            // nothing ready → avoid spinning
            Thread.sleep(50);
        }
    }
    private String getHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
