import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HostBasedFrontier implements Frontier {

    private final Map<String, Queue<String>> hostQueues = new ConcurrentHashMap<>();
    private final PriorityQueue<HostEntry> heap = new PriorityQueue<>();

    private final Set<String> inHeap = ConcurrentHashMap.newKeySet();

    private final long delayMs = 1000;

    @Override
    public synchronized void addUrl(String url){
        String host = getHost(url);

        if(host == null) return;

        hostQueues
                .computeIfAbsent(host, h -> new ConcurrentLinkedQueue<>())
                .offer(url);

        if(!inHeap.contains(host)){
            heap.offer(new HostEntry(host, System.currentTimeMillis()));
            inHeap.add(host);
        }

        notifyAll();
    }

    @Override
    public synchronized String getNextUrl() throws InterruptedException {
        while (true) {

            if(heap.isEmpty()) {
              wait();
              continue;
            }

            HostEntry entry = heap.peek();
            long now = System.currentTimeMillis();

            if(entry.nextAvailableTime > now){
                long waitTime = entry.nextAvailableTime - now;
                wait(waitTime);
                continue;
            }

            heap.poll();

            Queue<String> queue = hostQueues.get(entry.host);
            String url = queue.poll();

            if(url == null){
                inHeap.remove(entry.host);
                continue;
            }

            entry.nextAvailableTime = System.currentTimeMillis() + delayMs;
            heap.offer(entry);

            return url;
        }
    }
    private String getHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static class HostEntry implements Comparable<HostEntry> {
        String host;
        long nextAvailableTime;

        public HostEntry(String host, long nextAvailableTime){
            this.host = host;
            this.nextAvailableTime = nextAvailableTime;
        }


        @Override
        public int compareTo(HostEntry other) {
            return Long.compare(this.nextAvailableTime, other.nextAvailableTime);
        }
    }
}
