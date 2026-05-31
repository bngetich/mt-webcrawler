import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.List;

public class PartitionedRedisFrontier implements Frontier {

    private final JedisPool pool;
    private final PartitionRouter router;
    private final int assignedPartition;

    public PartitionedRedisFrontier(int numPartitions, int assignedPartition) {
        this.pool = new JedisPool("localhost", 6379);
        this.router = new PartitionRouter(numPartitions);
        this.assignedPartition = assignedPartition;
    }

    @Override
    public void addUrl(String url) {
        String host = getHost(url);
        if (host == null) return;

        int partition = router.getPartition(host);

        String schedulerKey = getSchedulerKey(partition);
        String hostQueueKey = getHostQueueKey(partition, host);

        try (Jedis jedis = pool.getResource()) {
            jedis.lpush(hostQueueKey, url);

            Double score = jedis.zscore(schedulerKey, host);

            if(score == null) {
                jedis.zadd(schedulerKey, System.currentTimeMillis(), host);
            }
        }
    }

    @Override
    public String getNextUrl() {
        String key = getPartitionKey(assignedPartition);

        try (Jedis jedis = pool.getResource()) {
            List<String> result = jedis.brpop(0, key);
            return result.get(1);
        }
    }

    private String getPartitionKey(int partition){
        return "crawler:frontier:partition:" + partition;
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
