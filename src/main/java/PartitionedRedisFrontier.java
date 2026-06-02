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

            Double score = jedis.zscore(schedulerKey, host);

            if (score == null) {
                jedis.zadd(
                        schedulerKey,
                        System.currentTimeMillis(),
                        host
                );
            }
        }
    }

    @Override
    public String getNextUrl() throws InterruptedException {
        String schedulerKey = getSchedulerKey(assignedPartition);

        while (true) {
            try (Jedis jedis = pool.getResource()) {
                List<Tuple> entries = jedis.zpopmin(schedulerKey, 1);

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

                    jedis.zadd(
                            schedulerKey,
                            nextAvailableTime,
                            host
                    );

                    Thread.sleep(waitTime);
                    continue;
                }

                String hostQueueKey = getHostQueueKey(assignedPartition, host);

                String url = jedis.rpop(hostQueueKey);

                if (url == null) {
                    continue;
                }

                Long remaining = jedis.llen(hostQueueKey);

                if (remaining > 0) {
                    jedis.zadd(
                            schedulerKey,
                            now + delayMs,
                            host
                    );
                }

                return url;
            }
        }
    }

    public void addRetry(String url, int retryCount){
        long delayMs = 1000L * (long) Math.pow(2, retryCount);
        long nextRetryTime = System.currentTimeMillis() + delayMs;

        try (Jedis jedis = pool.getResource()) {
            jedis.zadd("crawler:retry", nextRetryTime, url + "|" + retryCount);
        }
    }

    public void addToDlq(String url) {
        try (Jedis jedis = pool.getResource()){
            jedis.lpush("crawler:dlq", url);
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
