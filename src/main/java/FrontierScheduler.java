import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FrontierScheduler implements Frontier {

    private final Frontier frontier;
    private final Map<String, Long> lastAccess = new ConcurrentHashMap<>();
    private final long delayMs = 10000;

    public FrontierScheduler(Frontier frontier){
        this.frontier = frontier;
    }


    @Override
    public void addUrl(String url) {
        frontier.addUrl(url);
    }

    @Override
    public String getNextUrl() throws InterruptedException {
        while(true){

            System.out.println("Scheduler deciding...");

            String url = frontier.getNextUrl();

            if(url == null) continue;

            String host = getHost(url);
            long now = System.currentTimeMillis();

            if(host != null){
                Long lastTime = lastAccess.get(host);

                if(lastTime != null && now - lastTime < delayMs) {
                    // Not allowed yet -> requeue and try later
                    frontier.addUrl(url);
                    Thread.sleep(10000);
                    continue;
                }

                lastAccess.put(host, now);
            }

            return url;
        }

    }

    private String getHost(String url) {
        try {
            return new java.net.URI(url).getHost();
        } catch (Exception e){
            return null;
        }
    }
}
