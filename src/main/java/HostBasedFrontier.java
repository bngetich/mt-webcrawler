import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HostBasedFrontier implements Frontier {

    private final Map<String, Queue<CrawlTask>> hostQueues = new ConcurrentHashMap<>();
    private final PriorityQueue<HostEntry> heap = new PriorityQueue<>();
    private final Set<String> inHeap = ConcurrentHashMap.newKeySet();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition delayReady = lock.newCondition();


    private final long delayMs = 1000;

    @Override
    public void addTask(CrawlTask task)  {
        String url = task.getUrl();
        String host = getHost(url);

        if(host == null) return;

        lock.lock();
        try {
            hostQueues
                    .computeIfAbsent(host, h -> new ConcurrentLinkedQueue<>())
                    .offer(task);

            if (!inHeap.contains(host)) {
                heap.offer(new HostEntry(host, System.currentTimeMillis()));
                inHeap.add(host);
            }

            notEmpty.signal();

        } finally {
            lock.unlock();
        }
    }

    @Override
    public CrawlTask getNextTask() throws InterruptedException {
        lock.lock();
        try {
            while (true) {

                while (heap.isEmpty()) {
                    notEmpty.await();
                }

                HostEntry entry = heap.peek();
                long now = System.currentTimeMillis();

                if (entry.nextAvailableTime > now) {
                    long waitTime = entry.nextAvailableTime - now;
                    delayReady.awaitNanos(waitTime * 1_000_000);
                    continue;
                }

                heap.poll();

                Queue<CrawlTask> queue = hostQueues.get(entry.host);
                CrawlTask task = queue.poll();

                if (task == null) {
                    inHeap.remove(entry.host);
                    continue;
                }

                entry.nextAvailableTime = System.currentTimeMillis() + delayMs;
                heap.offer(entry);

                delayReady.signal();

                return task;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long size() {
        lock.lock();
        try {
            return hostQueues.values()
                    .stream()
                    .mapToLong(Queue::size)
                    .sum();
        } finally {
            lock.unlock();
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
