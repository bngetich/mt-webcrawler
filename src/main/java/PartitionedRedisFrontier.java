import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

import java.net.URI;
import java.util.List;

public class PartitionedRedisFrontier implements Frontier {

    private final JedisPool pool;
    private final PartitionRouter router;
    private final int assignedPartition;
    private final long delayMs = 1000;

    public PartitionedRedisFrontier(int numPartitions, int assignedPartition) {
        this.pool = new JedisPool("localhost", 6379);
        this.router = new PartitionRouter(numPartitions);
        this.assignedPartition = assignedPartition;
    }

    @Override
    public void addUrl(String url) {
        String host = getHost(url);

        if (host == null) {
            return;
        }

        int partition = router.getPartition(host);

        String schedulerKey = getSchedulerKey(partition);
        String hostQueueKey = getHostQueueKey(partition, host);

        try (Jedis jedis = pool.getResource()) {
            jedis.lpush(hostQueueKey, url);

            System.out.println(
                    "[ADD]        host=" + host +
                            " partition=" + partition +
                            " url=" + url
            );

            Double score = jedis.zscore(schedulerKey, host);

            if (score == null) {
                jedis.zadd(
                        schedulerKey,
                        System.currentTimeMillis(),
                        host
                );

                System.out.println(
                        "[SCHEDULE]   host=" + host +
                                " partition=" + partition
                );
            }
        }
    }

    @Override
    public String getNextUrl() throws InterruptedException {
        String schedulerKey = getSchedulerKey(assignedPartition);

        while (true) {
            try (Jedis jedis = pool.getResource()) {
                List<Tuple> entries = jedis.zrangeWithScores(schedulerKey, 0, 0);

                if (entries.isEmpty()) {
                    Thread.sleep(100);
                    continue;
                }

                Tuple entry = entries.get(0);

                String host = entry.getElement();
                long nextAvailableTime = (long) entry.getScore();
                long now = System.currentTimeMillis();

                if (nextAvailableTime > now) {
                    long waitTime = nextAvailableTime - now;

                    System.out.println(
                            "[WAIT]       partition=" + assignedPartition +
                                    " host=" + host +
                                    " waitMs=" + waitTime
                    );

                    Thread.sleep(waitTime);
                    continue;
                }

                jedis.zrem(schedulerKey, host);

                String hostQueueKey = getHostQueueKey(assignedPartition, host);

                String url = jedis.rpop(hostQueueKey);

                if (url == null) {
                    continue;
                }

                System.out.println(
                        "[DISPATCH]   partition=" + assignedPartition +
                                " host=" + host +
                                " url=" + url
                );

                Long remaining = jedis.llen(hostQueueKey);

                if (remaining > 0) {
                    jedis.zadd(
                            schedulerKey,
                            now + delayMs,
                            host
                    );

                    System.out.println(
                            "[RESCHEDULE] partition=" + assignedPartition +
                                    " host=" + host +
                                    " nextAvailable=" + (now + delayMs) +
                                    " remainingUrls=" + remaining
                    );
                }

                return url;
            }
        }
    }

    private String getSchedulerKey(int partition) {
        return "crawler:scheduler:partition:" + partition;
    }

    private String getHostQueueKey(int partition, String host) {
        return "crawler:partition:" + partition + ":host:" + host;
    }

    private String getHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }
}
